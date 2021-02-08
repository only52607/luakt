package com.ooooonly.luakt

import com.ooooonly.luakt.mapper.ValueMapperChain
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val mapperChain = ValueMapperChain.DEFAULT

inline fun <reified T : Any?> LuaTable.provideDelegate(key: String? = null, defaultValue: T? = null) =
    object : ReadWriteProperty<Any?, T?> {
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            set(key ?: property.name, ValueMapperChain.mapToLuaValueNullableInChain(value))
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            val result = get(key ?: property.name)
            if (result.isnil()) return defaultValue
            return ValueMapperChain.mapToKValue(result, T::class, ValueMapperChain.DEFAULT) as T?
        }
    }

operator fun LuaTable.get(key: Any): LuaValue = get(mapperChain.mapToLuaValueNullableInChain(key))
operator fun LuaTable.set(key: Any, value: Any) =
    set(mapperChain.mapToLuaValueNullableInChain(key), mapperChain.mapToLuaValueNullableInChain(value))

fun LuaTable.getOrNull(key: Any): LuaValue? =
    get(mapperChain.mapToLuaValueNullableInChain(key))?.takeIf { it != LuaValue.NIL }

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