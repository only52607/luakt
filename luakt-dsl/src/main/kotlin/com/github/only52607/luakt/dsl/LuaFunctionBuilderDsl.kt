@file:Suppress("UNUSED")

package com.github.only52607.luakt.dsl

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.mapToKValue
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.*

fun varArgFunctionOf(block: (Varargs) -> Varargs) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return block(args)
    }
}

fun zeroArgLuaFunctionOf(
    block: () -> LuaValue
) = object : ZeroArgFunction() {
    override fun call(): LuaValue {
        return block()
    }
}

fun oneArgLuaFunctionOf(
    block: (LuaValue) -> LuaValue
) = object : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        return block(arg!!)
    }
}

fun twoArgLuaFunctionOf(
    block: (LuaValue, LuaValue) -> LuaValue
) = object : TwoArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue {
        return block(arg1!!, arg2!!)
    }
}

fun threeArgLuaFunctionOf(
    block: (LuaValue, LuaValue, LuaValue) -> LuaValue
) = object : ThreeArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue {
        return block(arg1!!, arg2!!, arg3!!)
    }
}

fun luaFunctionOf(valueMapper: ValueMapper, block: () -> Any) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return valueMapper.mapToLuaValue(block())
    }
}

inline fun <reified T0> luaFunctionOf(valueMapper: ValueMapper, crossinline block: (T0) -> Any) =
    object : VarArgFunction() {
        override fun onInvoke(args: Varargs?): Varargs {
            args ?: return NIL
            return valueMapper.mapToLuaValue(block(valueMapper.mapToKValue(args.arg(1))))
        }
    }

inline fun <reified T0, reified T1> luaFunctionOf(valueMapper: ValueMapper, crossinline block: (T0, T1) -> Any) =
    object : VarArgFunction() {
        override fun onInvoke(args: Varargs?): Varargs {
            args ?: return NIL
            return valueMapper.mapToLuaValue(block(valueMapper.mapToKValue(args.arg(1)), valueMapper.mapToKValue(args.arg(2))))
        }
    }

inline fun <reified T0, reified T1, reified T2> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return valueMapper.mapToLuaValue(block(
            valueMapper.mapToKValue(args.arg(1)),
            valueMapper.mapToKValue(args.arg(2)),
            valueMapper.mapToKValue(args.arg(3))
        ))
    }
}

inline fun <reified T0, reified T1, reified T2, reified T3> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2, T3) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return valueMapper.mapToLuaValue(block(
            valueMapper.mapToKValue(args.arg(1)),
            valueMapper.mapToKValue(args.arg(2)),
            valueMapper.mapToKValue(args.arg(3)),
            valueMapper.mapToKValue(args.arg(4))
        ))
    }
}

inline fun <reified T0, reified T1, reified T2, reified T3, reified T4> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2, T3, T4) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return valueMapper.mapToLuaValue(block(
            valueMapper.mapToKValue(args.arg(1)),
            valueMapper.mapToKValue(args.arg(2)),
            valueMapper.mapToKValue(args.arg(3)),
            valueMapper.mapToKValue(args.arg(4)),
            valueMapper.mapToKValue(args.arg(5))
        ))
    }
}

inline fun <reified T0, reified T1, reified T2, reified T3, reified T4, reified T5> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2, T3, T4, T5) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return valueMapper.mapToLuaValue(block(
            valueMapper.mapToKValue(args.arg(1)),
            valueMapper.mapToKValue(args.arg(2)),
            valueMapper.mapToKValue(args.arg(3)),
            valueMapper.mapToKValue(args.arg(4)),
            valueMapper.mapToKValue(args.arg(5)),
            valueMapper.mapToKValue(args.arg(6))
        ))
    }
}

inline fun <reified T0, reified T1, reified T2, reified T3, reified T4, reified T5, reified T6> luaFunctionOf(
    valueMapper: ValueMapper,
    crossinline block: (T0, T1, T2, T3, T4, T5, T6) -> Any
) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return valueMapper.mapToLuaValue(block(
            valueMapper.mapToKValue(args.arg(1)),
            valueMapper.mapToKValue(args.arg(2)),
            valueMapper.mapToKValue(args.arg(3)),
            valueMapper.mapToKValue(args.arg(4)),
            valueMapper.mapToKValue(args.arg(5)),
            valueMapper.mapToKValue(args.arg(6)),
            valueMapper.mapToKValue(args.arg(7))
        ))
    }
}