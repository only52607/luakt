package com.github.only52607.luakt.lib

import com.github.only52607.luakt.mappers.ValueMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

@Suppress("unused")
class KotlinCoroutineLib(private val coroutineScope: CoroutineScope, val valueMapper: ValueMapper) : TwoArgFunction() {
    override fun call(modName: LuaValue?, env: LuaValue?): LuaValue? {
        val globals: Globals? = env?.checkglobals()
        valueMapper.provideScope {
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
        }
        return globals
    }
}