package com.ooooonly.luakt

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.JsePlatform

val defaultGlobals: Globals by lazy {
    JsePlatform.standardGlobals()
}

fun executeLuaCode(code: String, globals: Globals = defaultGlobals) {
    globals.load(code).call()
}

fun executeLuaFile(filePath: String, globals: Globals = defaultGlobals) {
    globals.loadfile(filePath).call()
}