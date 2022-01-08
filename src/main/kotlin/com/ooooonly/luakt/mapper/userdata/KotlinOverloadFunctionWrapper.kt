package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.ValueMapper
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

open class KotlinOverloadFunctionWrapper(
    private val kFunctions: Collection<KFunction<*>>,
    private val valueMapper: ValueMapper
) : VarArgFunction() {

    private val wrappers = kFunctions.map { KotlinFunctionWrapper(it, valueMapper) }

    override fun onInvoke(args: Varargs?): Varargs {
        if (kFunctions.isEmpty()) throw ParameterNotMatchException("KFunctions is empty.")
        val errorInfo = StringBuilder()
        wrappers.forEach { wrapper ->
            try {
                return@onInvoke wrapper.invoke(args)
            } catch (e: ParameterNotMatchException) {
                errorInfo.append("${wrapper.kFunction.name}(${wrapper.kFunction.parameters.joinToString(separator = ",") { "${it.name}:${it.type.jvmErasure.simpleName}" }}) ${e.message} \n")
            }
        }
        throw ParameterNotMatchException("${wrappers.size} overload function has been tried as follow,but failed to match the corresponding overloaded function.Please check for incorrect use '.' Operator instead of ':' operator. \n $errorInfo")
    }

    override fun tostring(): LuaValue = LuaValue.valueOf(toString())

    override fun toString(): String = if (kFunctions.isEmpty()) "" else {
        "<Overload Functions>:[${wrappers.joinToString(separator = "\n") { it.toString() }}]"
    }

}