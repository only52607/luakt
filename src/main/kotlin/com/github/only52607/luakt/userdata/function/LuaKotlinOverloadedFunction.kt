package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.userdata.KReflectInfoBuilder
import com.github.only52607.luakt.userdata.SimpleKReflectInfoBuilder
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

open class LuaKotlinOverloadedFunction(
    private val kFunctions: Collection<KFunction<*>>,
    private val valueMapper: ValueMapper,
    kReflectInfoBuilder: KReflectInfoBuilder? = SimpleKReflectInfoBuilder
) : VarArgFunction() {

    private val luaKotlinFunctions = kFunctions.map { LuaKotlinMemberFunction(it, valueMapper, kReflectInfoBuilder) }

    override fun onInvoke(args: Varargs?): Varargs {
        if (kFunctions.isEmpty()) throw ParameterNotMatchException("KFunctions is empty.")
        val errorInfo = StringBuilder()
        luaKotlinFunctions.forEach { f ->
            try {
                return@onInvoke f.invoke(args)
            } catch (e: ParameterNotMatchException) {
                errorInfo.append("${f.kFunction.name}(${f.kFunction.parameters.joinToString(separator = ",") { "${it.name}:${it.type.jvmErasure.simpleName}" }}) ${e.message} \n")
            }
        }
        throw ParameterNotMatchException("${luaKotlinFunctions.size} overload function has been tried as follow, but failed to match the corresponding overloaded function.\n$errorInfo")
    }

    override fun tostring(): LuaValue = LuaValue.valueOf(toString())

    override fun toString(): String =
        if (kFunctions.isEmpty()) "" else luaKotlinFunctions.joinToString(separator = "\n") { it.toString() }

}