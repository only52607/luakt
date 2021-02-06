package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.LuaValueMapFailException
import com.ooooonly.luakt.mapper.ValueMapperChain
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

open class KotlinFunctionWrapper(
    val kFunction: KFunction<*>,
    private val mapperChain: ValueMapperChain? = null
) : VarArgFunction() {

    private val parameters: List<KParameter> = kFunction.parameters

    override fun onInvoke(args: Varargs?): Varargs {
        if (mapperChain == null) throw KotlinNullPointerException()
        if (args == null) return LuaValue.NIL
        if (args.narg() != parameters.size) throw ParameterNotMatchException("Required ${parameters.size} args,but ${args.narg()} was given.")

        val parametersMap = parameters.associateWith {
            try {
                mapperChain.mapToKValueInChain(args.arg(it.index + 1), it.type.jvmErasure)
            } catch (e: LuaValueMapFailException) {
                throw ParameterNotMatchException("Parameter not match:${e.message}")
            }
        }
        kFunction.isAccessible = true
        val result = try {
            if (kFunction.isSuspend)
                runBlocking {
                    kFunction.callSuspendBy(parametersMap)
                }
            else kFunction.callBy(parametersMap)
        } catch (e: IllegalArgumentException) {
            throw ParameterNotMatchException("Parameter not match:${e.message}")
        }
        return mapperChain.mapToLuaValueNullableInChain(result)
    }
}

class ParameterNotMatchException(override val message: String?) : Exception(message)