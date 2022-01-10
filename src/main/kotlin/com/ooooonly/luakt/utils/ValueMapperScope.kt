package com.ooooonly.luakt.utils

import com.ooooonly.luakt.mapper.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: ValueMapperScope
 * Description:
 * date: 2022/1/9 20:34
 * @author ooooonly
 * @version
 */
class ValueMapperScope(
    val valueMapper: ValueMapper
) {
    inline fun <reified T> LuaValue.asKValue(): T = asKValue(valueMapper)

    fun LuaValue.asKValue(
        targetClass: KClass<*>? = null
    ): Any = valueMapper.mapToKValue(this, targetClass)

    fun Any?.asLuaValue(): LuaValue = valueMapper.mapToLuaValue(this)

    fun <T : Any?> LuaTable.entry(
        key: String? = null,
        defaultValue: T? = null
    ) = entry(valueMapper, key, defaultValue)

    operator fun LuaTable.get(key: Any): LuaValue =
        get(key, valueMapper)

    operator fun LuaTable.set(key: Any, value: Any) =
        set(key, value, valueMapper)

    fun LuaTable.getOrNull(key: Any): LuaValue? =
        getOrNull(key, valueMapper)

    inline fun buildLuaTable(builder: LuaTableBuilder.() -> Unit): LuaTable =
        buildLuaTable(valueMapper, builder)

    inline fun LuaTable.edit(process: LuaTableBuilder.() -> Unit) =
        edit(valueMapper, process)

}

fun ValueMapper.provideScope(block: ValueMapperScope.() -> Unit) =
    ValueMapperScope(this).block()