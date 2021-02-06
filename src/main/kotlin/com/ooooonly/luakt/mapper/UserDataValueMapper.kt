package com.ooooonly.luakt.mapper

import com.ooooonly.luakt.mapper.userdata.KotlinInstanceWrapper
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

class UserDataKValueMapper : KValueMapper {
    override fun mapToLuaValue(obj: Any, defaultValueMapperChain: ValueMapperChain?): LuaValue {
        return KotlinInstanceWrapper(obj)
    }
}

class UserDataLuaValueMapper : LuaValueMapper {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>,
        defaultValueMapperChain: ValueMapperChain?
    ): Any? {
        if (luaValue is LuaUserdata) return luaValue.m_instance
        return null
    }
}