@file:Suppress("UNUSED")

package com.github.only52607.luakt.utils

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.Globals
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.File

val defaultGlobals: Globals by lazy {
    JsePlatform.standardGlobals()
}

fun <R> withDefaultGlobals(block: Globals.() -> R) = with(defaultGlobals, block)

context (Globals)
fun String.execute() {
    load(this).call()
}

context (Globals)
        operator fun String.invoke() {
    load(this).call()
}

context (ValueMapper, Globals) operator fun String.invoke(varargs: Varargs): Varargs = load(this).invoke(varargs)

context (ValueMapper, Globals)
        inline operator fun <reified R> String.invoke(varargs: Varargs): R =
    load(this).invoke(varargs).arg1().asKValue()

context (ValueMapper, Globals)operator fun String.invoke(vararg args: Any): Varargs =
    load(this).invoke(args.map { mapToLuaValue(it) }.toTypedArray())

context (ValueMapper, Globals)inline operator fun <reified R> String.invoke(vararg args: Any): R =
    load(this).invoke(args.map { mapToLuaValue(it) }.toTypedArray()).arg1().asKValue()

context (Globals)
fun String.executeAsLuaFile() {
    loadfile(this).call()
}

context (Globals)
fun File.execute() {
    loadfile(absolutePath).call()
}

context (Globals)
        operator fun File.invoke() {
    loadfile(absolutePath).call()
}

context (ValueMapper, Globals) operator fun File.invoke(varargs: Varargs): Varargs =
    loadfile(absolutePath).invoke(varargs)

context (ValueMapper, Globals)
        inline operator fun <reified R> File.invoke(varargs: Varargs): R =
    loadfile(absolutePath).invoke(varargs).arg1().asKValue()

context (ValueMapper, Globals)operator fun File.invoke(vararg args: Any): Varargs =
    loadfile(absolutePath).invoke(args.map { mapToLuaValue(it) }.toTypedArray())

context (ValueMapper, Globals)inline operator fun <reified R> File.invoke(vararg args: Any): R =
    loadfile(absolutePath).invoke(args.map { mapToLuaValue(it) }.toTypedArray()).arg1().asKValue()