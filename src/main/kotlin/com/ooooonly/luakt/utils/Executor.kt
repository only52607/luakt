package com.ooooonly.luakt.utils

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.JsePlatform

val defaultGlobals: Globals by lazy {
    JsePlatform.standardGlobals()
}

fun String.executeAsLuaCode(
    globals: Globals = defaultGlobals
) {
    globals.load(this).call()
}

fun String.executeAsLuaFile(
    globals: Globals = defaultGlobals
) {
    globals.loadfile(this).call()
}