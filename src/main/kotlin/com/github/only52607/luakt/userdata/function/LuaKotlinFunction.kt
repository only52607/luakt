package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.utils.asLuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
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
    valueMapper: ValueMapper,
    private val instanceOrReceiver: Any? = null,
    private val parameterBuilder: ParameterBuilder = ParameterBuilder.DEFAULT,
    private val functionCaller: FunctionCaller = FunctionCaller.BLOCKING
) : VarArgFunction(), ValueMapper by valueMapper {
    override fun onInvoke(args: Varargs): Varargs {
        val parametersMap = parameterBuilder.buildParameterMap(
            parameters = kFunction.parameters,
            args = args,
            valueMapper = this,
            receiver = instanceOrReceiver
        )
        kFunction.isAccessible = true
        val result = try {
            functionCaller.callFunction(kFunction, parametersMap)
        } catch (e: IllegalArgumentException) {
            throw ParameterNotMatchException("Parameter not match: ${e.message}")
        }
        return result.asLuaValue()
    }

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