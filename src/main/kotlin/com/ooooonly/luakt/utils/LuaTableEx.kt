package com.ooooonly.luakt.utils

import com.ooooonly.luakt.mapper.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure


class LuaTableEntryProperty<T : Any?>(
    private val valueMapper: ValueMapper,
    private val table: LuaTable,
    private val key: String? = null,
    private val defaultValue: T? = null
) : ReadWriteProperty<Any?, T?> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        table.set(key ?: property.name, valueMapper.mapToLuaValue(value))
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        val result = table.get(key ?: property.name)
        if (result.isnil()) return defaultValue
        return valueMapper.mapToKValueNullable(result, property.returnType.jvmErasure) as T?
    }
}

fun <T : Any?> LuaTable.entry(
    valueMapper: ValueMapper,
    key: String? = null,
    defaultValue: T? = null
) = LuaTableEntryProperty(valueMapper, this, key, defaultValue)

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