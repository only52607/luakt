package com.github.only52607.luakt.dsl

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.JsePlatform

/**
 * ClassName: GlobalsDsl
 * Description:
 * date: 2022/4/13 13:25
 * @author ooooonly
 * @version
 */
fun withJseStandardGlobals(block: Globals.() -> Unit) = with(JsePlatform.standardGlobals()) {
    block()
}