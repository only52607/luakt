package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.KReflectInfoBuilder
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.isAccessible

/**
 * ClassName: LuaKotlinFunction
 * Description:
 * date: 2022/1/8 21:18
 * @author ooooonly
 * @version
 */
abstract class LuaKotlinFunction(
    val kFunction: KFunction<*>,
    protected val valueMapper: ValueMapper,
    private val kReflectInfoBuilder: KReflectInfoBuilder? = null,
    val description: String? = null
) : VarArgFunction() {

    override fun onInvoke(args: Varargs): Varargs {
        checkArgs(args)
        val parametersMap = buildParametersMap(args)
        kFunction.isAccessible = true
        val result = try {
            callFunction(parametersMap)
        } catch (e: IllegalArgumentException) {
            throw ParameterNotMatchException("Parameter not match: ${e.message}")
        }
        return convertResult(result)
    }

    protected open fun checkArgs(args: Varargs) {
        val parameters: List<KParameter> = kFunction.parameters
        if (args.narg() != parameters.size) throw ParameterNotMatchException("Required ${parameters.size} args, but ${args.narg()} was given")
    }

    protected open fun callFunction(parametersMap: Map<KParameter, Any?>): Any? {
        return if (kFunction.isSuspend) {
            runBlocking {
                kFunction.callSuspendBy(parametersMap)
            }
        } else {
            kFunction.callBy(parametersMap)
        }
    }

    abstract fun buildParametersMap(args: Varargs): Map<KParameter, Any?>

    protected open fun convertResult(result: Any?): Varargs = valueMapper.mapToLuaValue(result)

    override fun toString(): String = description ?: kReflectInfoBuilder?.buildKFunctionInfo(kFunction) ?: ""
}

class ParameterNotMatchException(override val message: String?) : Exception(message)