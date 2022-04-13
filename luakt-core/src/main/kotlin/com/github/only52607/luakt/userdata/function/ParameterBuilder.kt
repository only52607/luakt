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
sealed class ParameterBuilder {
    class ImplicitReceiver(valueMapper: ValueMapper, private val instance: Any, private val receiver: Any? = null) :
        ParameterBuilder(), ValueMapper by valueMapper {
        override fun List<KParameter>.buildParameterMapByVarargs(args: Varargs): Map<KParameter, Any?> {
            return associateWith {
                when (it.kind) {
                    KParameter.Kind.INSTANCE -> instance
                    KParameter.Kind.EXTENSION_RECEIVER -> receiver ?: instance
                    KParameter.Kind.VALUE -> {
                        if (it.type.jvmErasure == Varargs::class) {
                            if (it.index != size - 1) throw ParameterNotMatchException("When a function receives an argument of type Varargs, only that argument is allowed at the end of the argument list")
                            return@associateWith args.subargs(it.index + 1)
                        }
                        if (it.index + 1 > args.narg()) throw ParameterNotMatchException("Required arg at ${it.index + 1}, but received ${args.narg()} args")
                        return@associateWith mapToKValue(args.arg(it.index + 1), it.type.jvmErasure)
                    }
                }
            }
        }
    }

    class Default(
        valueMapper: ValueMapper
    ) : ParameterBuilder(), ValueMapper by valueMapper {
        override fun List<KParameter>.buildParameterMapByVarargs(
            args: Varargs
        ): Map<KParameter, Any?> {
            return associateWith {
                if (it.index + 1 > args.narg()) throw ParameterNotMatchException("Required arg at ${it.index + 1}, but received ${args.narg()} args")
                if (it.type.jvmErasure == Varargs::class) {
                    if (it.index != size - 1) throw ParameterNotMatchException("When a function receives an argument of type Varargs, only that argument is allowed at the end of the argument list")
                    return@associateWith args.subargs(it.index + 1)
                }
                return@associateWith mapToKValue(args.arg(it.index + 1), it.type.jvmErasure)
            }
        }
    }

    abstract fun List<KParameter>.buildParameterMapByVarargs(args: Varargs): Map<KParameter, Any?>
}

class ParameterNotMatchException(override val message: String?) : Exception(message)