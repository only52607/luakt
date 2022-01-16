package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.KReflectInfoBuilder
import com.github.only52607.luakt.userdata.SimpleKReflectInfoBuilder
import org.luaj.vm2.Varargs
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

open class LuaKotlinMemberFunction(
    kFunction: KFunction<*>,
    valueMapper: ValueMapper,
    kReflectInfoBuilder: KReflectInfoBuilder? = SimpleKReflectInfoBuilder,
    private val instanceOrReceiver: Any? = null,
    description: String? = null
) : LuaKotlinFunction(
    kFunction,
    valueMapper,
    kReflectInfoBuilder,
    description
) {
    override fun buildParametersMap(args: Varargs): Map<KParameter, Any?> {
        val parameters: List<KParameter> = kFunction.parameters
        var extensionReceiverParameter: KParameter? = null
        var instanceParameter: KParameter? = null
        val valueParameters = mutableListOf<KParameter>()
        val parametersMap = mutableMapOf<KParameter, Any?>()

        for (parameter in parameters) {
            when (parameter.kind) {
                KParameter.Kind.EXTENSION_RECEIVER -> extensionReceiverParameter = parameter
                KParameter.Kind.INSTANCE -> instanceParameter = parameter
                KParameter.Kind.VALUE -> valueParameters.add(parameter)
            }
        }

        var valueParametersIndex = when {
            instanceParameter != null -> {
                if (instanceOrReceiver != null) {
                    parametersMap[instanceParameter] = instanceOrReceiver
                    1
                } else {
                    parametersMap[instanceParameter] =
                        valueMapper.mapToKValueNullable(args.arg(1), instanceParameter.type.jvmErasure)
                    2
                }
            }
            extensionReceiverParameter != null -> {
                if (instanceOrReceiver != null) {
                    parametersMap[extensionReceiverParameter] = instanceOrReceiver
                    1
                } else {
                    parametersMap[extensionReceiverParameter] =
                        valueMapper.mapToKValueNullable(args.arg(1), extensionReceiverParameter.type.jvmErasure)
                    2
                }
            }
            else -> 1
        }

        for (parameter in valueParameters) {
            parametersMap[parameter] =
                valueMapper.mapToKValueNullable(args.arg(valueParametersIndex++), parameter.type.jvmErasure)
        }

        return parametersMap
    }
}
