package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.KReflectInfoBuilder
import com.github.only52607.luakt.userdata.SimpleKReflectInfoBuilder
import org.luaj.vm2.Varargs
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

/**
 * ClassName: LuaKotlinVarargFunction
 * Description:
 * date: 2022/1/15 16:02
 * @author ooooonly
 * @version
 */
class LuaKotlinVarargFunction(
    kFunction: KFunction<*>,
    valueMapper: ValueMapper,
    kReflectInfoBuilder: KReflectInfoBuilder? = SimpleKReflectInfoBuilder,
    description: String? = null,
    private val instance: Any? = null
) : LuaKotlinFunction(
    kFunction,
    valueMapper,
    kReflectInfoBuilder,
    description
) {

    private val valueParameters: List<KParameter> = kFunction.valueParameters

    override fun checkArgs(args: Varargs) {
        if (valueParameters.size != 1 || valueParameters.first().type.jvmErasure != Varargs::class) {
            throw ParameterNotMatchException("LuaKotlinVarargFunction required one Varargs parameter")
        }
    }

    override fun convertResult(result: Any?): Varargs {
        result ?: return NIL
        if (kFunction.returnType.jvmErasure.isSubclassOf(Varargs::class)) {
            return result as Varargs
        }
        throw ParameterNotMatchException("LuaKotlinVarargFunction required subclass of Varargs as return type")
    }

    override fun buildParametersMap(args: Varargs): Map<KParameter, Any?> {
        val parametersMap = mutableMapOf<KParameter, Any?>()
        parametersMap[valueParameters.first()] = args
        kFunction.instanceParameter?.let { parametersMap[it] = instance }
        return parametersMap
    }

}