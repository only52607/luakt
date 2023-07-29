package com.github.only52607.luakt

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.math.min
import kotlin.reflect.KParameter

abstract class KotlinMember(
    private val params: List<KParameter>
) : VarArgFunction() {

    private val coercions: Map<KParameter, CoerceLuaToKotlin.Coercion> =
        params.associateWith { CoerceLuaToKotlin.getCoercion(it) }

    private fun score(args: Varargs, startArg: Int, startParam: Int): Int {
        val argsUsed = startArg > args.narg()
        val paramsUsed = startParam > (params.size - 1)
        if (argsUsed && paramsUsed) return 0
        if (argsUsed || paramsUsed) return CoerceLuaToKotlin.SCORE_UNCOERCIBLE

        val p = params[startParam]
        val a = args.arg(startArg)
        val co = coercions[p]!!

        if (p.isVararg) {
            return min(
                // Skip current param
                score(args, startArg, startParam + 1),

                // Use current param
                score(args, startArg + 1, startParam) + co.score(a),
            )
        }

        if (p.isOptional) {
            return min(
                // Skip current param
                score(args, startArg, startParam + 1),

                // Use current param
                score(args, startArg + 1, startParam + 1) + co.score(a),
            )
        }

        return (score(args, startArg + 1, startParam + 1) + co.score(a))
    }

    protected open fun convertArgsAsMap(args: Varargs): Map<KParameter, Any?> {
        val bestScore = CoerceLuaToKotlin.SCORE_UNCOERCIBLE
        var bestMap: Map<KParameter, MutableList<LuaValue>>? = null
        val tmpMap = mutableMapOf<KParameter, MutableList<LuaValue>>()

        fun push(key: KParameter, value: LuaValue) {
            val list = tmpMap[key] ?: mutableListOf()
            list.add(value)
            tmpMap[key] = list
        }

        fun pop(key: KParameter) {
            val list = tmpMap[key]!!
            if (list.size == 1) {
                tmpMap.remove(key)
            } else {
                list.removeLast()
            }
        }

        fun find(startArg: Int = 1, startParam: Int = 0, score: Int = 0) {
            val argsUsed = startArg > args.narg()
            val paramsUsed = startParam > params.size - 1
            if (argsUsed && paramsUsed) {
                if (score < bestScore) bestMap = tmpMap.toMap() // copy current map
                return
            }
            if (argsUsed || paramsUsed) return

            val p = params[startParam]
            val a = args.arg(startArg)
            val co = coercions[p]!!

            if (p.isVararg) {
                // Skip current param
                find(startArg, startParam + 1, score)
                // Use current param
                push(p, a)
                find(startArg + 1, startParam, score + co.score(a))
                pop(p)
                return
            }

            if (p.isOptional) {
                // Skip current param
                find(startArg, startParam + 1, score)

                // Use current param
                push(p, a)
                find(startArg + 1, startParam + 1, score + co.score(a))
                pop(p)

                return
            }

            push(p, a)
            find(startArg + 1, startParam + 1, score + co.score(a))
            pop(p)
        }
        find()

        return bestMap!!.mapValues {
            if (it.value.size == 1) coercions[it.key]!!.coerce(it.value[0])
            else coercions[it.key]!!.coerce(LuaValue.listOf(it.value.toTypedArray()))
        }
    }

    open fun score(args: Varargs): Int {
        return score(args, 1, 0)
    }

    /*

    private val hasVarargs = params.find { it.isVararg } != null

    private val fixSize = if (hasVarargs) params.size - 1 else params.size

    open fun score(args: Varargs): Int {
        val n = args.narg()
        var sc = if (n > fixSize) CoerceLuaToKotlin.SCORE_WRONG_TYPE * (n - fixSize) else 0
        var s = 1
        for (param in params) {
            val co = coercions[param]!!
            if (param.isVararg) {
                val len = n - (params.size - 1)
                val list = LuaValue.listOf((s until s + len).map { args.arg(it) }.toTypedArray())
                sc += co.score(list)
                s += len
                continue
            }
            sc += co.score(args.arg(s))
            s++
        }
        return sc
    }

    protected open fun convertArgsAsMap(args: Varargs): Map<KParameter, Any?> {
        // TODO: Optional KParameter
        val n = args.narg()
        if (hasVarargs) {
            if (n != params.size) throw LuaError("require ${params.size} args, but got $n")
        } else {
            if (n < params.size - 1) throw LuaError("require at least ${params.size - 1} args, but got $n")
        }
        val argsMap = mutableMapOf<KParameter, Any?>()
        var s = 1
        for (param in params) {
            val co = coercions[param]!!
            if (param.isVararg) {
                val len = n - (params.size - 1)
                val list = LuaValue.listOf((s until s + len).map { args.arg(it) }.toTypedArray())
                argsMap[param] = co.coerce(list)
                s += len
                continue
            }
            argsMap[param] = co.coerce(args.arg(s))
            s++
        }
        return argsMap
    }*/
}