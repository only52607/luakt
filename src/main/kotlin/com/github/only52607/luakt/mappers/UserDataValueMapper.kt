package com.github.only52607.luakt.mappers

import com.github.only52607.luakt.KValueMapper
import com.github.only52607.luakt.LuaValueMapper
import com.github.only52607.luakt.userdata.LuaKotlinObject
import com.github.only52607.luakt.userdata.classes.LuaKotlinClassRegistry
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

class UserDataKValueMapper(
    private val luaKotlinClassRegistry: LuaKotlinClassRegistry,
    override var nextKValueMapper: KValueMapper? = null,
    override var firstKValueMapper: KValueMapper? = null
) : AbstractKValueMapper() {
    @Suppress("UNCHECKED_CAST")
    override fun mapToLuaValue(obj: Any?): LuaValue {
        obj ?: return LuaValue.NIL
        return LuaKotlinObject(obj, luaKotlinClassRegistry.obtainLuaKotlinClass(obj::class as KClass<Any>))
    }
}

class UserDataLuaValueMapper(
    override var nextLuaValueMapper: LuaValueMapper? = null,
    override var firstLuaValueMapper: LuaValueMapper? = null
) : AbstractLuaValueMapper() {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>?
    ): Any {
        if (luaValue is LuaUserdata) return luaValue.m_instance
        return nextMapToKValue(luaValue, targetClass)
    }
}

