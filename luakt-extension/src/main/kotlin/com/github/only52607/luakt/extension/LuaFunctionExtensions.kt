@file:Suppress("UNUSED")

package com.github.only52607.luakt.extension

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.*

fun VarArgFunction(block: (Varargs) -> Varargs) = object : VarArgFunction() {
    override fun onInvoke(args: Varargs?): Varargs {
        args ?: return NIL
        return block(args)
    }
}

fun ZeroArgFunction(
    block: () -> LuaValue
) = object : ZeroArgFunction() {
    override fun call(): LuaValue {
        return block()
    }
}

fun OneArgFunction(
    block: (LuaValue) -> LuaValue
) = object : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        return block(arg!!)
    }
}

fun TwoArgFunction(
    block: (LuaValue, LuaValue) -> LuaValue
) = object : TwoArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue {
        return block(arg1!!, arg2!!)
    }
}

fun ThreeArgFunction(
    block: (LuaValue, LuaValue, LuaValue) -> LuaValue
) = object : ThreeArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue {
        return block(arg1!!, arg2!!, arg3!!)
    }
}