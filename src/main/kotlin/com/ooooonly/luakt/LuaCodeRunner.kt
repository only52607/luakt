package com.ooooonly.luakt

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.JsePlatform

val standardGlobals: Globals by lazy {
    JsePlatform.standardGlobals()
}
fun runLua(code: String, globals: Globals = standardGlobals) {
    globals.load(code).call()
}

fun runLuaFile(filePath: String, globals: Globals = standardGlobals) {
    globals.loadfile(filePath).call()
}