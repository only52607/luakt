package com.github.only52607.luakt.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction

@Suppress("unused")
class KotlinCoroutineLib(private val coroutineScope: CoroutineScope) :
    TwoArgFunction() {
    override fun call(modName: LuaValue, env: LuaValue): LuaValue? {
        val co = LuaTable().apply {
            this["sleep"] = object : VarArgFunction() {
                override fun onInvoke(args: Varargs): Varargs {
                    runBlocking { delay(args.checklong(1)) }
                    return NIL
                }
            }
            this["delay"] to object : VarArgFunction() {
                override fun onInvoke(args: Varargs): Varargs {
                    runBlocking { delay(args.checklong(1)) }
                    return NIL
                }
            }
        }
        env.set("co", co)
        env["package"]["loaded"].set("co", co)
        return NIL
    }
}