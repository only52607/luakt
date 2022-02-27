package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.Varargs
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

/**
 * ClassName: ParameterBuilder
 * Description:
 * date: 2022/1/30 12:24
 * @author ooooonly
 * @version
 */
enum class ParameterBuilder {
    DEFAULT;

    fun buildParameterMap(
        parameters: List<KParameter>,
        args: Varargs,
        valueMapper: ValueMapper,
        receiver: Any? = null
    ): Map<KParameter, Any?> {
        return when (this) {
            DEFAULT -> buildDefaultParameterMap(parameters, args, valueMapper, receiver)
        }
    }

    private fun buildDefaultParameterMap(
        parameters: List<KParameter>,
        args: Varargs,
        valueMapper: ValueMapper,
        receiver: Any?
    ): Map<KParameter, Any?> {
        val argNum = args.narg()
        val parameterNum = parameters.size
        val receiverParameters =
            parameters.filter { it.kind == KParameter.Kind.EXTENSION_RECEIVER || it.kind == KParameter.Kind.INSTANCE }
        val valueParameters = parameters.filter { it.kind == KParameter.Kind.VALUE }
        val hasReceiverParameters = receiverParameters.isNotEmpty()
        if (receiverParameters.size > 1) throw ParameterNotMatchException("Only arguments that contain ONE instance or receiver are supported")
        if (hasReceiverParameters && argNum < 1) throw ParameterNotMatchException("Required an instance parameter, but the parameter list is empty")
        val receiverParametersMap = receiverParameters.associateWith {
            receiver ?: valueMapper.mapToKValueNullable(
                args.arg1(),
                it.type.jvmErasure
            )
        }
        val valueParametersMap = valueParameters.associateWith { valueParameter ->
            if (valueParameter.type.jvmErasure == Varargs::class) {
                if (valueParameter.index != parameterNum - 1) throw ParameterNotMatchException("When a function receives an argument of type Varargs, only that argument is allowed at the end of the argument list")
                return@associateWith args.subargs(valueParameter.index + 1)
            }
            return@associateWith valueMapper.mapToKValueNullable(
                args.arg(valueParameter.index + 1),
                valueParameter.type.jvmErasure
            )
        }
        return receiverParametersMap + valueParametersMap
    }
}

class ParameterNotMatchException(override val message: String?) : Exception(message)