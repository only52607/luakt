package com.github.only52607.luakt.userdata.impl

import com.github.only52607.luakt.mappers.AbstractKValueMapper
import com.github.only52607.luakt.mappers.AbstractLuaValueMapper
import com.github.only52607.luakt.mappers.KValueMapper
import com.github.only52607.luakt.mappers.LuaValueMapper
import com.github.only52607.luakt.userdata.LuaKotlinClassRegistry
import com.github.only52607.luakt.userdata.LuaKotlinObject
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
