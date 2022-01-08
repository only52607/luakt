package com.ooooonly.luakt.mapper.impl

import com.ooooonly.luakt.mapper.*
import com.ooooonly.luakt.mapper.userdata.ConcurrentKotlinClassWrapperRegistry
import com.ooooonly.luakt.mapper.userdata.KClassExtensionProvider
import com.ooooonly.luakt.mapper.userdata.KotlinClassWrapperRegistry
import com.ooooonly.luakt.mapper.userdata.KotlinInstanceWrapper
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

class UserDataKValueMapper(
    override var nextKValueMapper: KValueMapper? = null,
    override var firstKValueMapper: KValueMapper? = null,
    valueMapper: ValueMapper,
    kClassExtensionProvider: KClassExtensionProvider,
    private val wrapperRegistry: KotlinClassWrapperRegistry = ConcurrentKotlinClassWrapperRegistry(
        valueMapper,
        kClassExtensionProvider
    )
) : AbstractKValueMapper() {
    override fun mapToLuaValue(obj: Any?): LuaValue {
        obj ?: return LuaValue.NIL
        return KotlinInstanceWrapper(obj, wrapperRegistry.obtainClassWrapper(obj::class))
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

