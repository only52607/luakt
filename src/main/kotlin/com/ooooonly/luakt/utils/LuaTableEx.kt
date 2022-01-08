package com.ooooonly.luakt.utils

import com.ooooonly.luakt.mapper.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T : Any?> LuaTable.item(
    key: String? = null,
    defaultValue: T? = null,
    valueMapper: ValueMapper = globalValueMapper
) =
    object : ReadWriteProperty<Any?, T?> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            set(key ?: property.name, valueMapper.mapToLuaValue(value))
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            val result = get(key ?: property.name)
            if (result.isnil()) return defaultValue
            return valueMapper.mapToKValueNullable(result, T::class) as T?
        }
    }

operator fun LuaTable.get(key: Any): LuaValue = get(globalValueMapper.mapToLuaValue(key))
operator fun LuaTable.set(key: Any, value: Any) =
    set(globalValueMapper.mapToLuaValue(key), globalValueMapper.mapToLuaValue(value))

fun LuaTable.getOrNull(key: Any): LuaValue? =
    get(globalValueMapper.mapToLuaValue(key))?.takeIf { it != LuaValue.NIL }

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