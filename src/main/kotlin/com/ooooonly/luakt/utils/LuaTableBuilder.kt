package com.ooooonly.luakt.utils

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue


@DslMarker
annotation class LuaTableBuilderMarker

@LuaTableBuilderMarker
class LuaTableBuilder(var tableValue: LuaTable = LuaTable()) {

    infix fun Iterable<*>.nto(value: Any) {
        forEach {
            tableValue[it.asLuaValue()] = value
        }
    }

    infix fun String.to(value: Any) {
        tableValue[this] = value
    }

    infix fun Any.to(value: Any) {
        tableValue[this.asLuaValue()] = value
    }

//    @LuaTableBuilderMarker
//    infix fun String.map(mapper: (LuaValue) -> Any) {
//        tableValue[this] = mapper(tableValue[this]).asLuaValue()
//    }
//
//    @LuaTableBuilderMarker
//    infix fun Any.map(mapper: (LuaValue) -> Any) {
//        tableValue[this] = mapper(tableValue[this]).asLuaValue()
//    }

    operator fun LuaValue.unaryPlus() {
        tableValue.insert(tableValue.keyCount(), this)
    }

    fun get(key: String): LuaValue = tableValue.rawget(key)

    fun get(key: Any): LuaValue = tableValue.rawget(key.asLuaValue())
}

inline fun buildLuaTable(builder: LuaTableBuilder.() -> Unit): LuaTable = LuaTableBuilder()
    .apply { builder() }.tableValue

inline fun LuaTable.edit(process: LuaTableBuilder.() -> Unit) = LuaTableBuilder(
    this
).process()

