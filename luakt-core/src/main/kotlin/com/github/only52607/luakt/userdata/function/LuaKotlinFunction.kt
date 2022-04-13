package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
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
    private val parameterBuilder: ParameterBuilder = ParameterBuilder.Default(valueMapper),
    private val functionCaller: FunctionCaller = FunctionCaller.Blocking
) : VarArgFunction(), ValueMapper by valueMapper {
    override fun onInvoke(args: Varargs): Varargs {
        val parametersMap = with(parameterBuilder) {
            kFunction.parameters.buildParameterMapByVarargs(args)
        }
        setAccessible()
        val result = try {
            functionCaller.callFunction(kFunction, parametersMap)
        } catch (e: IllegalArgumentException) {
            throw ParameterNotMatchException("Parameter not match: ${e.message}")
        }
        return mapToLuaValue(result)
    }

    private fun setAccessible() {
        kFunction.isAccessible = true
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