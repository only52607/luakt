@file:Suppress("UNUSED")
package com.github.only52607.luakt.extension

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

fun Varargs.unpackVarargs(): List<LuaValue> = mutableListOf<LuaValue>().apply {
    this@unpackVarargs.forEachVararg {
        add(it)
    }
}

fun varargsOf(vararg luaValues: LuaValue): Varargs = LuaValue.varargsOf(luaValues)

fun Array<LuaValue>.packVarargs() = varargsOf(*this)

fun List<LuaValue>.packVarargs() = varargsOf(*this.toTypedArray())

operator fun Varargs.component1(): LuaValue = arg(1)
operator fun Varargs.component2(): LuaValue = arg(2)
operator fun Varargs.component3(): LuaValue = arg(3)
operator fun Varargs.component4(): LuaValue = arg(4)
operator fun Varargs.component5(): LuaValue = arg(5)
operator fun Varargs.component6(): LuaValue = arg(6)
operator fun Varargs.component7(): LuaValue = arg(7)
operator fun Varargs.component8(): LuaValue = arg(8)
operator fun Varargs.component9(): LuaValue = arg(9)
operator fun Varargs.component10(): LuaValue = arg(10)
