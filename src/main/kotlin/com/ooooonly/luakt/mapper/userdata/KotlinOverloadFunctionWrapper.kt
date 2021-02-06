package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.ValueMapperChain
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction


open class KotlinOverloadFunctionWrapper(
    private val kFunctions: List<KFunction<*>>,
    private val mapperChain: ValueMapperChain? = null
) : VarArgFunction() {
    private val wrappers = kFunctions.map { KotlinFunctionWrapper(it, mapperChain) }

    override fun onInvoke(args: Varargs?): Varargs {
        wrappers.forEach {
            try {
                return@onInvoke it.invoke(args)
            } catch (e: ParameterNotMatchException) {
                e.printStackTrace()
            }
        }
        throw ParameterNotMatchException("""${wrappers.size} overload function has been tried,but failed to match the corresponding overloaded function.Please check for incorrect use '.' Operator instead of ':' operator.""")
    }
}