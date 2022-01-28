@file:Suppress("UNUSED")
package com.github.only52607.luakt.utils

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs


/**
 * 1-base Index
 */
operator fun Varargs.get(index: Int): LuaValue = arg(index)

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

fun varargsOf(vararg luaValues: LuaValue) {
    LuaValue.varargsOf(luaValues)
}