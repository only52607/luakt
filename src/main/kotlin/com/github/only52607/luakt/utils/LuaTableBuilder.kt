package com.github.only52607.luakt.utils

import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

@DslMarker
annotation class LuaTableBuilderMarker

@LuaTableBuilderMarker
class LuaTableBuilder(
    val valueMapper: ValueMapper,
    val tableValue: LuaTable = LuaTable()
) {
    infix fun Iterable<*>.nto(value: Any) {
        forEach {
            tableValue.set(valueMapper.mapToLuaValue(it), valueMapper.mapToLuaValue(value))
        }
    }

    infix fun String.to(value: Any) {
        tableValue.set(this, valueMapper.mapToLuaValue(value))
    }

    infix fun Any.to(value: Any) {
        tableValue.set(valueMapper.mapToLuaValue(this), valueMapper.mapToLuaValue(value))
    }

    operator fun LuaValue.unaryPlus() {
        tableValue.insert(tableValue.keyCount(), this)
    }

    fun get(key: String): LuaValue = tableValue.rawget(key)

    fun get(key: Any): LuaValue = tableValue.rawget(key.asLuaValue(valueMapper))
}

inline fun buildLuaTable(valueMapper: ValueMapper, builder: LuaTableBuilder.() -> Unit): LuaTable =
    LuaTableBuilder(valueMapper)
        .apply { builder() }.tableValue

inline fun LuaTable.edit(valueMapper: ValueMapper, process: LuaTableBuilder.() -> Unit) = LuaTableBuilder(
    valueMapper,
    this
).process()

