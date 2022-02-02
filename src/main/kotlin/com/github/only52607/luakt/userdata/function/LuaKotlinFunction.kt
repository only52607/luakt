package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

/**
 * ClassName: LuaKotlinFunction
 * Description:
 * date: 2022/2/2 22:10
 * @author ooooonly
 * @version
 */
open class LuaKotlinFunction(
    open val kFunction: KFunction<*>,
    private val valueMapper: ValueMapper,
    private val instanceOrReceiver: Any? = null
) : VarArgFunction() {
    override fun onInvoke(args: Varargs): Varargs {
        val parametersMap = ParameterBuilder.buildParameterMap(
            parameters = kFunction.parameters,
            args = args,
            valueMapper = valueMapper,
            receiver = instanceOrReceiver
        )
        kFunction.isAccessible = true
        val result = try {
            callFunction(parametersMap)
        } catch (e: IllegalArgumentException) {
            throw ParameterNotMatchException("Parameter not match: ${e.message}")
        }
        return convertResult(result)
    }

    private fun callFunction(parametersMap: Map<KParameter, Any?>): Any? {
        return if (kFunction.isSuspend) {
            runBlocking {
                kFunction.callSuspendBy(parametersMap)
            }
        } else {
            kFunction.callBy(parametersMap)
        }
    }

    private fun convertResult(result: Any?): Varargs = valueMapper.mapToLuaValue(result)

    private fun buildKParameterInfo(kParameter: KParameter): String {
        return if (kParameter.name == null) kParameter.type.jvmErasure.simpleName ?: ""
        else "${kParameter.name}: ${kParameter.type.jvmErasure.simpleName}"
    }

    override fun toString(): String {
        return "${kFunction.name}(${
            kFunction.parameters.joinToString(
                separator = ",",
                transform = ::buildKParameterInfo
            )
        }) -> ${kFunction.returnType.jvmErasure.simpleName}"
    }
}