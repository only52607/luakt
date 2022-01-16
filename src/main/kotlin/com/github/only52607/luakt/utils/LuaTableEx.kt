package com.github.only52607.luakt.utils

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

operator fun LuaTable.get(key: Any, valueMapper: ValueMapper): LuaValue =
    get(valueMapper.mapToLuaValue(key))

operator fun LuaTable.set(key: Any, value: Any, valueMapper: ValueMapper) =
    set(valueMapper.mapToLuaValue(key), valueMapper.mapToLuaValue(value))

fun LuaTable.getOrNull(key: Any, valueMapper: ValueMapper): LuaValue? =
    get(valueMapper.mapToLuaValue(key))?.takeIf { it != LuaValue.NIL }

fun LuaTable.forEach(process: (key: LuaValue, value: LuaValue) -> Unit) {
    var k: LuaValue = LuaValue.NIL
    while (true) {
        val n = next(k)
        k = n.arg1()
        if (k.isnil())
            break
        val v = n.arg(2)
        process(k, v)
    }
}

fun LuaTable.setFrom(luaTable: LuaTable) {
    luaTable.forEach { key, value -> set(key, value) }
}