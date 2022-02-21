@file:Suppress("UNUSED")

package com.github.only52607.luakt.utils

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction

fun varArgFunctionOf(block: (Varargs) -> Varargs) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(args)
    }
}

context(ValueMapper)
fun luaFunctionOf(block: () -> Any) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block().asLuaValue()
    }
}

context(ValueMapper)inline fun <reified T0> luaFunctionOf(crossinline block: (T0) -> Any) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(args.arg(1).asKValue()).asLuaValue()
    }
}

context(ValueMapper)inline fun <reified T0, reified T1> luaFunctionOf(crossinline block: (T0, T1) -> Any) =
    object : VarArgFunction() {
        override fun onInvoke(args: Varargs?): Varargs {
            args ?: return LuaValue.NIL
            return block(args.arg(1).asKValue(), args.arg(2).asKValue()).asLuaValue()
        }
    }

context(ValueMapper)inline fun <reified T0, reified T1, reified T2> luaFunctionOf(
    crossinline block: (T0, T1, T2) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(
            args.arg(1).asKValue(),
            args.arg(2).asKValue(),
            args.arg(3).asKValue()
        ).asLuaValue()
    }
}

context(ValueMapper)inline fun <reified T0, reified T1, reified T2, reified T3> luaFunctionOf(
    crossinline block: (T0, T1, T2, T3) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(
            args.arg(1).asKValue(),
            args.arg(2).asKValue(),
            args.arg(3).asKValue(),
            args.arg(4).asKValue()
        ).asLuaValue()
    }
}

context(ValueMapper)inline fun <reified T0, reified T1, reified T2, reified T3, reified T4> luaFunctionOf(
    crossinline block: (T0, T1, T2, T3, T4) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(
            args.arg(1).asKValue(),
            args.arg(2).asKValue(),
            args.arg(3).asKValue(),
            args.arg(4).asKValue(),
            args.arg(5).asKValue()
        ).asLuaValue()
    }
}

context(ValueMapper)inline fun <reified T0, reified T1, reified T2, reified T3, reified T4, reified T5> luaFunctionOf(
    crossinline block: (T0, T1, T2, T3, T4, T5) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(
            args.arg(1).asKValue(),
            args.arg(2).asKValue(),
            args.arg(3).asKValue(),
            args.arg(4).asKValue(),
            args.arg(5).asKValue(),
            args.arg(6).asKValue()
        ).asLuaValue()
    }
}

context(ValueMapper)inline fun <reified T0, reified T1, reified T2, reified T3, reified T4, reified T5, reified T6> luaFunctionOf(
    crossinline block: (T0, T1, T2, T3, T4, T5, T6) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return LuaValue.NIL
        return block(
            args.arg(1).asKValue(),
            args.arg(2).asKValue(),
            args.arg(3).asKValue(),
            args.arg(4).asKValue(),
            args.arg(5).asKValue(),
            args.arg(6).asKValue(),
            args.arg(7).asKValue()
        ).asLuaValue()
    }
}

context(ValueMapper, LuaValue) inline fun <reified T0> setFunction(
    key: String,
    alias: List<String>? = null,
    noinline block: () -> Any
) {
    val f = luaFunctionOf(block)
    set(key, f)
    alias?.forEach { set(it, f) }
}