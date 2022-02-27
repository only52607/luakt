package com.github.only52607.luakt.userdata.function

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction

open class LuaKotlinOverloadedFunction(
    private val luaKotlinFunctions: Collection<LuaKotlinFunction>
) : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        if (luaKotlinFunctions.isEmpty()) throw ParameterNotMatchException("Required at least one LuaKotlinFunction to invoke")
        val errorInfo = StringBuilder()
        luaKotlinFunctions.forEach { f ->
            try {
                return@onInvoke f.invoke(args)
            } catch (e: ParameterNotMatchException) {
                errorInfo.append("$f: ${e.message}\n")
            }
        }
        throw ParameterNotMatchException("${luaKotlinFunctions.size} overload function has been tried as follow, but failed to match the corresponding overloaded function.\n$errorInfo")
    }

    override fun tostring(): LuaValue = LuaValue.valueOf(toString())

    override fun toString(): String =
        if (luaKotlinFunctions.isEmpty()) "" else luaKotlinFunctions.joinToString(separator = "\n") { it.toString() }
}

context (ValueMapper) fun Collection<KFunction<*>>.joinAsLuaKotlinOverloadedFunction(
    parameterBuilder: ParameterBuilder = ParameterBuilder.Default,
    functionCaller: FunctionCaller = FunctionCaller.Blocking
) = LuaKotlinOverloadedFunction(map { LuaKotlinFunction(it, this@ValueMapper, parameterBuilder, functionCaller) })