package com.github.only52607.luakt

import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

internal class KotlinConstructor private constructor(
    private val constructor: KFunction<*>
) :KotlinMember(constructor.valueParameters) {
    override operator fun invoke(args: Varargs): Varargs {
        val a = convertArgsAsMap(args)
        return try {
            CoerceKotlinToLua.coerce(constructor.callBy(a))
        } catch (e: InvocationTargetException) {
            throw LuaError(e.targetException)
        } catch (e: Exception) {
            error("coercion error $e")
        }
    }

    class Overload(private val constructors: Array<KotlinConstructor>) : VarArgFunction() {
        override fun invoke(args: Varargs): Varargs {
            var best: KotlinConstructor? = null
            var score = CoerceLuaToKotlin.SCORE_UNCOERCIBLE
            for (i in constructors.indices) {
                val s = constructors[i].score(args)
                if (s < score) {
                    score = s
                    best = constructors[i]
                    if (score == 0) break
                }
            }

            // any match?
            if (best == null) error("no coercible public method")

            // invoke it
            return best!!.invoke(args)
        }
    }

    companion object {
        private val fs = ConcurrentHashMap<KFunction<*>, KotlinConstructor>()
        fun forConstructorFunction(f: KFunction<*>): KotlinConstructor {
            return fs[f] ?: KotlinConstructor(f).also { fs[f] = it }
        }

        fun forConstructorFunctions(f: Array<KotlinConstructor>): LuaFunction {
            return KotlinConstructor.Overload(f)
        }
    }
}
