@file:Suppress("UNUSED")
package com.github.only52607.luakt.utils

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction


fun varArgFunctionOf(block: (Varargs) -> Varargs) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        if (args == null) return LuaValue.NIL
        return block(args)
    }
}

fun luaFunctionOf(valueMapper: ValueMapper, block: () -> Any) = varArgFunctionOf {
    return@varArgFunctionOf block().asLuaValue(valueMapper)
}

inline fun <reified T0> luaFunctionOf(valueMapper: ValueMapper, crossinline block: (T0) -> Any) =
    varArgFunctionOf { args ->
        return@varArgFunctionOf block(args[1].asKValue(valueMapper)).asLuaValue(valueMapper)
    }

inline fun <reified T0, reified T1> luaFunctionOf(valueMapper: ValueMapper, crossinline block: (T0, T1) -> Any) =
    varArgFunctionOf { args ->
        return@varArgFunctionOf block(args[1].asKValue(valueMapper), args[2].asKValue(valueMapper)).asLuaValue(
            valueMapper
        )
    }

inline fun <reified T0, reified T1, reified T2> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2) -> Any
) =
    varArgFunctionOf { args ->
        block(
            args[1].asKValue(valueMapper),
            args[2].asKValue(valueMapper),
            args[3].asKValue(valueMapper)
        ).asLuaValue(
            valueMapper
        )
    }

inline fun <reified T0, reified T1, reified T2, reified T3> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2, T3) -> Any
) =
    varArgFunctionOf { args ->
        block(
            args[1].asKValue(valueMapper),
            args[2].asKValue(valueMapper),
            args[3].asKValue(valueMapper),
            args[4].asKValue(valueMapper)
        ).asLuaValue(valueMapper)
    }

inline fun <reified T0, reified T1, reified T2, reified T3, reified T4> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2, T3, T4) -> Any
) =
    varArgFunctionOf { args ->
        block(
            args[1].asKValue(valueMapper),
            args[2].asKValue(valueMapper),
            args[3].asKValue(valueMapper),
            args[4].asKValue(valueMapper),
            args[5].asKValue(valueMapper)
        ).asLuaValue(valueMapper)
    }

inline fun <
        reified T0,
        reified T1,
        reified T2,
        reified T3,
        reified T4,
        reified T5> luaFunctionOf(valueMapper: ValueMapper, crossinline block: (T0, T1, T2, T3, T4, T5) -> Any) =
    varArgFunctionOf { args ->
        block(
            args[1].asKValue(valueMapper),
            args[2].asKValue(valueMapper),
            args[3].asKValue(valueMapper),
            args[4].asKValue(valueMapper),
            args[5].asKValue(valueMapper),
            args[6].asKValue(valueMapper)
        ).asLuaValue(valueMapper)
    }


inline fun <
        reified T0,
        reified T1,
        reified T2,
        reified T3,
        reified T4,
        reified T5,
        reified T6>
        luaFunctionOf(valueMapper: ValueMapper, crossinline block: (T0, T1, T2, T3, T4, T5, T6) -> Any) =
    varArgFunctionOf { args ->
        block(
            args[1].asKValue(valueMapper),
            args[2].asKValue(valueMapper),
            args[3].asKValue(valueMapper),
            args[4].asKValue(valueMapper),
            args[5].asKValue(valueMapper),
            args[6].asKValue(valueMapper),
            args[7].asKValue(valueMapper)
        ).asLuaValue(valueMapper)
    }
