package com.github.only52607.luakt.mappers

import com.github.only52607.luakt.LuaValueMapper
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: LuaTableDelegateBuilder
 * Description:
 * date: 2022/1/15 15:14
 * @author ooooonly
 * @version
 */
class LuaTableDelegateMapper(
    val rootLuaValueMapper: LuaValueMapper
) : LuaValueMapper {
    override fun mapToKValue(luaValue: LuaValue, targetClass: KClass<*>?): Any {
        if (!luaValue.istable()) throw IllegalArgumentException("LuaTableDelegateMapper should receive a LuaTable")
        TODO()
    }

    override fun mapToKValueNullable(luaValue: LuaValue, targetClass: KClass<*>?): Any? {
        throw IllegalArgumentException("LuaTableDelegateMapper can not map value to nullable value")
    }
}