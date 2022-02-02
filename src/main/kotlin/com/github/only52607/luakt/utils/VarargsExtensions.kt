@file:Suppress("UNUSED")
package com.github.only52607.luakt.utils

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

fun Varargs.forEachVararg(block: (LuaValue) -> Unit) {
    for (i in 1..narg())
        block(arg(i))
}

fun Varargs.forEachVarargIndexed(block: (LuaValue, Int) -> Unit) {
    for (i in 1..narg())
        block(arg(i), i)
}

fun Varargs.varargsToLuaValueList(): List<LuaValue> = mutableListOf<LuaValue>().apply {
    this@varargsToLuaValueList.forEachVararg {
        add(it)
    }
}

fun Varargs.varargsTotoLuaValueArray() = varargsToLuaValueList().toTypedArray()

fun varargsOf(vararg luaValues: LuaValue): Varargs = LuaValue.varargsOf(luaValues)

operator fun Varargs.component1(): LuaValue = arg(1)
operator fun Varargs.component2(): LuaValue = arg(2)
operator fun Varargs.component3(): LuaValue = arg(3)
operator fun Varargs.component4(): LuaValue = arg(4)
operator fun Varargs.component5(): LuaValue = arg(5)
operator fun Varargs.component6(): LuaValue = arg(6)