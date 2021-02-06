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
import kotlin.reflect.jvm.jvmErasure

open class KotlinFunctionWrapper(
    private val kFunction: KFunction<*>,
    private val mapperChain: ValueMapperChain? = null
) : VarArgFunction() {
    private val parameters: List<KParameter> = kFunction.parameters

    override fun onInvoke(args: Varargs?): Varargs {
        if (mapperChain == null) throw KotlinNullPointerException()
        if (args == null) return LuaValue.NIL
        if (args.narg() != parameters.size) throw ParameterNotMatchException("Required ${parameters.size} args,but ${args.narg()} was given.")

        val parametersMap = parameters.associateWith {
            try {
                mapperChain.mapToKValue(args.arg(it.index + 1), it.type.jvmErasure, mapperChain)
            } catch (e: LuaValueMapFailException) {
                throw ParameterNotMatchException("Parameter not match:${e.message}")
            }
        }
        val result = if (kFunction.isSuspend)
            runBlocking {
                kFunction.callSuspendBy(parametersMap)
            }
        else kFunction.callBy(parametersMap)
        return mapperChain.mapToLuaValueNullable(result, mapperChain) ?: LuaValue.NIL
    }
}

class ParameterNotMatchException(override val message: String?) : Exception(message)