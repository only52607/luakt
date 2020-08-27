package com.ooooonly.luakt

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

fun Any.asVarargs(): Varargs = when (this) {
    is Varargs -> this
//    is Array<*> -> LuaValue.varargsOf(mutableListOf<LuaValue>().apply {
//        this@asVarargs.forEach {
//            it?.let {
//                add(it.asLuaValue())
//            }
//        }
//    }.toTypedArray())
    else -> asLuaValue()
}

fun Array<LuaValue>.asVarargs(): Varargs = LuaValue.varargsOf(this)

fun varargsOf(): LuaValue = LuaValue.NIL

fun varargsOf(vararg args:Any): Varargs =
    LuaValue.varargsOf(mutableListOf<LuaValue>().apply {
        args.forEach {
            add(it.asLuaValue())
        }
    }.toTypedArray())

fun varargsOf(vararg args:LuaValue): Varargs = LuaValue.varargsOf(args)