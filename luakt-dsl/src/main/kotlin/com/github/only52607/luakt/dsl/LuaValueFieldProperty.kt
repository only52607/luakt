package com.github.only52607.luakt.dsl

import com.github.only52607.luakt.ValueMapper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

/**
 * ClassName: LuaTableFieldProperty
 * Description:
 * date: 2022/1/15 22:18
 * @author ooooonly
 * @version
 */
class LuaValueFieldProperty<T : Any?> (
    private val valueMapper: ValueMapper,
    private val luaValue: org.luaj.vm2.LuaValue,
    private val key: String? = null,
    private val defaultValue: T? = null,
) : ReadWriteProperty<Any?, T?> {

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        luaValue.set(key ?: property.name, valueMapper.mapToLuaValue(value))
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        val result = luaValue.get(key ?: property.name)
        if (result.isnil()) {
            return defaultValue
        }
        return valueMapper.mapToKValueNullable(result, property.returnType.jvmErasure) as T?
    }
}