package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.ValueMapper
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

open class KotlinFunctionWrapper(
    val kFunction: KFunction<*>,
    private val valueMapper: ValueMapper,
    private val kReflectInfoBuilder: KReflectInfoBuilder = SimpleKReflectInfoBuilder
) : VarArgFunction() {

    private val parameters: List<KParameter> = kFunction.parameters

    override fun onInvoke(args: Varargs): Varargs {
        if (args.narg() != parameters.size) throw ParameterNotMatchException("Required ${parameters.size} args, but ${args.narg()} was given")
        val parametersMap = buildParametersMap(args)
        kFunction.isAccessible = true
        val result = try {
            callFunctionBlocking(parametersMap)
        } catch (e: IllegalArgumentException) {
            throw ParameterNotMatchException("Parameter not match: ${e.message}")
        }
        return valueMapper.mapToLuaValue(result)
    }

    private fun callFunctionBlocking(parametersMap: Map<KParameter, Any?>): Any? {
        return if (kFunction.isSuspend) {
            runBlocking {
                kFunction.callSuspendBy(parametersMap)
            }
        } else {
            kFunction.callBy(parametersMap)
        }
    }

    private fun buildParametersMap(args: Varargs): Map<KParameter, Any?> {
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
                parametersMap[instanceParameter] =
                    valueMapper.mapToKValueNullable(args.arg(1), instanceParameter.type.jvmErasure)
                2
            }
            extensionReceiverParameter != null -> {
                parametersMap[extensionReceiverParameter] =
                    valueMapper.mapToKValueNullable(args.arg(1), extensionReceiverParameter.type.jvmErasure)
                2
            }
            else -> 1
        }

        for (parameter in valueParameters) {
            parametersMap[parameter] =
                valueMapper.mapToKValueNullable(args.arg(valueParametersIndex++), parameter.type.jvmErasure)
        }

        return parametersMap
    }

    override fun toString(): String = kReflectInfoBuilder.buildKFunctionInfo(kFunction)
}

class ParameterNotMatchException(override val message: String?) : Exception(message)