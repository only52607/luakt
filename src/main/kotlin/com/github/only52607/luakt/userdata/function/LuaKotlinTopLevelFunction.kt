package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.KReflectInfoBuilder
import com.github.only52607.luakt.userdata.SimpleKReflectInfoBuilder
import org.luaj.vm2.Varargs
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

/**
 * ClassName: LuaKotlinTopLevelFunction
 * Description:
 * date: 2022/1/15 15:30
 * @author ooooonly
 * @version
 */
@Suppress("unused")
open class LuaKotlinTopLevelFunction(
    kFunction: KFunction<*>,
    valueMapper: ValueMapper,
    kReflectInfoBuilder: KReflectInfoBuilder? = SimpleKReflectInfoBuilder
) : LuaKotlinFunction(
    kFunction,
    valueMapper,
    kReflectInfoBuilder
) {
    override fun buildParametersMap(args: Varargs): Map<KParameter, Any?> {
        val parameters: List<KParameter> = kFunction.parameters
        val parametersMap = mutableMapOf<KParameter, Any?>()
        var parametersIndex = 1
        for (parameter in parameters) {
            parametersMap[parameter] =
                valueMapper.mapToKValueNullable(args.arg(parametersIndex++), parameter.type.jvmErasure)
        }
        return parametersMap
    }
}