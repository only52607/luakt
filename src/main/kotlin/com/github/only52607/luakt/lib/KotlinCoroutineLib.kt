package com.github.only52607.luakt.lib

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.utils.luaFunctionOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

@Suppress("unused")
class KotlinCoroutineLib(private val coroutineScope: CoroutineScope, val valueMapper: ValueMapper) : TwoArgFunction(), ValueMapper by valueMapper {
    override fun call(modName: LuaValue?, env: LuaValue?): LuaValue? {
        env?.checkglobals()?.apply {
            "sleep" to luaFunctionOf(this@KotlinCoroutineLib) { time: Long ->
                runBlocking { delay(time) }
            }
            "delay" to luaFunctionOf(this@KotlinCoroutineLib) { time: Long ->
                runBlocking { delay(time) }
            }
            "launch" to luaFunctionOf(this@KotlinCoroutineLib) { cls: LuaValue ->
                return@luaFunctionOf coroutineScope.launch {
                    cls.invoke()
                }
            }
        }
        return NIL
    }
}