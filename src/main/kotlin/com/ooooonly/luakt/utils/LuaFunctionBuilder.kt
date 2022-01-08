package com.ooooonly.luakt.utils

import com.ooooonly.luakt.get
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction


fun varArgFunctionOf(block: (Varargs) -> Varargs) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        if (args == null) return LuaValue.NIL
        return block(args)
    }
}

fun luaFunctionOf(block: () -> Any) = varArgFunctionOf {
    return@varArgFunctionOf block().asLuaValue()
}

inline fun <reified T0> luaFunctionOf(crossinline block: (T0) -> Any) = varArgFunctionOf { args ->
    return@varArgFunctionOf block(args[0].asKValue()).asLuaValue()
}

inline fun <reified T0, reified T1> luaFunctionOf(crossinline block: (T0, T1) -> Any) = varArgFunctionOf { args ->
    return@varArgFunctionOf block(args[0].asKValue(), args[1].asKValue()).asLuaValue()
}

inline fun <reified T0, reified T1, reified T2> luaFunctionOf(crossinline block: (T0, T1, T2) -> Any) =
    varArgFunctionOf { args ->
        block(args[0].asKValue(), args[1].asKValue(), args[2].asKValue()).asLuaValue()
    }

inline fun <reified T0, reified T1, reified T2, reified T3> luaFunctionOf(crossinline block: (T0, T1, T2, T3) -> Any) =
    varArgFunctionOf { args ->
        block(args[0].asKValue(), args[1].asKValue(), args[2].asKValue(), args[3].asKValue()).asLuaValue()
    }

inline fun <reified T0, reified T1, reified T2, reified T3, reified T4> luaFunctionOf(crossinline block: (T0, T1, T2, T3, T4) -> Any) =
    varArgFunctionOf { args ->
        block(
            args[0].asKValue(),
            args[1].asKValue(),
            args[2].asKValue(),
            args[3].asKValue(),
            args[4].asKValue()
        ).asLuaValue()
    }

inline fun <
        reified T0,
        reified T1,
        reified T2,
        reified T3,
        reified T4,
        reified T5> luaFunctionOf(crossinline block: (T0, T1, T2, T3, T4, T5) -> Any) = varArgFunctionOf { args ->
    block(
        args[0].asKValue(),
        args[1].asKValue(),
        args[2].asKValue(),
        args[3].asKValue(),
        args[4].asKValue(),
        args[5].asKValue()
    ).asLuaValue()
}


inline fun <
        reified T0,
        reified T1,
        reified T2,
        reified T3,
        reified T4,
        reified T5,
        reified T6>
        luaFunctionOf(crossinline block: (T0, T1, T2, T3, T4, T5, T6) -> Any) = varArgFunctionOf { args ->
    block(
        args[0].asKValue(),
        args[1].asKValue(),
        args[2].asKValue(),
        args[3].asKValue(),
        args[4].asKValue(),
        args[5].asKValue(),
        args[6].asKValue()
    ).asLuaValue()
}
