package com.github.only52607.luakt

import kotlinx.coroutines.runBlocking
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

class KotlinFunction private constructor(
    private val f: KFunction<*>
) : KotlinMember(f.valueParameters) {
    init {
        try {
            if (!f.isAccessible) f.isAccessible = true
        } catch (_: SecurityException) {
        }
    }

    override fun call(): LuaValue {
        return error("method cannot be called without instance")
    }

    override fun call(arg: LuaValue): LuaValue {
        return invokeMethod(arg.checkuserdata(), NONE)
    }

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        return invokeMethod(arg1.checkuserdata(), arg2)
    }

    override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
        return invokeMethod(arg1.checkuserdata(), varargsOf(arg2, arg3))
    }

    override operator fun invoke(args: Varargs): Varargs {
        return invokeMethod(args.checkuserdata(1), args.subargs(2))
    }

    fun invokeMethod(instance: Any, args: Varargs): LuaValue {
        // use singleton object or companion object as instance parameter if exists
        val self = if (instance !is KClass<*>) instance else when {
            instance.objectInstance != null -> instance.objectInstance
            instance.companionObjectInstance != null
                    && f.instanceParameter?.type?.isSupertypeOf(instance.companionObject!!.createType()) == true -> instance.companionObjectInstance

            else -> instance
        }
        val receiverParameter = f.instanceParameter ?: f.extensionReceiverParameter
        val receiverMap = if (receiverParameter == null) mapOf() else mapOf(Pair(receiverParameter, self))
        val a = convertArgsAsMap(args) + receiverMap
        return try {
            val ret = if (f.isSuspend) {
                runBlocking {
                    f.callSuspendBy(a)
                }
            } else {
                f.callBy(a)
            }
            CoerceKotlinToLua.coerce(ret)
        } catch (e: InvocationTargetException) {
            throw LuaError(e.targetException)
        } catch (e: Exception) {
            error("coercion error $e")
        }
    }

    class Overload(
        private val functions: Array<KotlinFunction>
    ) : LuaFunction() {
        override fun call(): LuaValue {
            return error("method cannot be called without instance")
        }

        override fun call(arg: LuaValue): LuaValue {
            return invokeBestMethod(arg.checkuserdata(), NONE)
        }

        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            return invokeBestMethod(arg1.checkuserdata(), arg2)
        }

        override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
            return invokeBestMethod(arg1.checkuserdata(), varargsOf(arg2, arg3))
        }

        override fun invoke(args: Varargs): Varargs {
            return invokeBestMethod(args.checkuserdata(1), args.subargs(2))
        }

        private fun invokeBestMethod(instance: Any, args: Varargs): LuaValue {
            var best: KotlinFunction? = null
            var score = CoerceLuaToKotlin.SCORE_UNCOERCIBLE
            for (i in functions.indices) {
                val s = functions[i].score(args)
                if (s < score) {
                    score = s
                    best = functions[i]
                    if (score == 0) break
                }
            }

            // invoke it
            return best?.invokeMethod(instance, args) ?: error("no coercible public method")
        }
    }

    companion object {
        private val fs = ConcurrentHashMap<KFunction<*>, KotlinFunction>()
        fun forFunction(f: KFunction<*>): KotlinFunction {
            return fs[f] ?: KotlinFunction(f).also { fs[f] = it }
        }

        fun forKFunctions(vararg f: KFunction<*>): LuaFunction {
            if (f.size == 1) return fs[f[0]] ?: KotlinFunction(f[0]).also { fs[f[0]] = it }
            val kfs = f.map { forFunction(it) }
            return if (kfs.size == 1) kfs[0] else forFunctions(kfs.toTypedArray())
        }

        fun forFunctions(f: Array<KotlinFunction>): LuaFunction {
            return Overload(f)
        }
    }
}
