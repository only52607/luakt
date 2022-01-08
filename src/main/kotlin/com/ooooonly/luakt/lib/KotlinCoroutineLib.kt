package com.ooooonly.luakt.lib

import com.ooooonly.luakt.utils.edit
import com.ooooonly.luakt.utils.luaFunctionOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

@Suppress("unused")
class KotlinCoroutineLib(private val coroutineScope: CoroutineScope) : TwoArgFunction() {
    override fun call(modName: LuaValue?, env: LuaValue?): LuaValue {
        val globals: Globals? = env?.checkglobals()
        globals?.edit {
            "sleep" to luaFunctionOf { time: Long ->
                runBlocking { delay(time) }
            }
            "delay" to luaFunctionOf { time: Long ->
                runBlocking { delay(time) }
            }
            "launch" to luaFunctionOf { cls: LuaClosure ->
                return@luaFunctionOf coroutineScope.launch {
                    cls.invoke()
                }
            }
        }
        return LuaValue.NIL
    }
}