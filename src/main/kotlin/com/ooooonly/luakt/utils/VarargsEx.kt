package com.ooooonly.luakt

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

/**
 * o-base Index
 */
operator fun Varargs.get(index: Int): LuaValue = arg(index + 1)

fun Varargs.forEach(block: (LuaValue) -> Unit) {
    for (i in 1..narg())
        block(arg(i))
}

fun Varargs.toList() = mutableListOf<LuaValue>().apply {
    this@toList.forEach {
        add(it)
    }
}

fun Varargs.toTypedArray() = toList().toTypedArray()