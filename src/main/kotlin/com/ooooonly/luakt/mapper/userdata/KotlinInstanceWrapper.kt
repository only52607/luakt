package com.ooooonly.luakt.mapper.userdata

import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue

open class KotlinInstanceWrapper(
    val instance: Any = Any(),
    private val kClassWrapper: KotlinClassWrapper = KotlinClassWrapper.forKClass(instance::class)
) : LuaUserdata(instance) {

    override fun get(key: LuaValue): LuaValue {
        checkMetaInfo(key)?.let { return@get it }
        val keyString = key.checkjstring()
        if (kClassWrapper.containProperty(keyString))
            return kClassWrapper.getProperty(this, keyString)
        if (kClassWrapper.containFunction(keyString))
            return kClassWrapper.getFunctionWrapper(keyString)
        return LuaValue.NIL
    }

    override fun set(key: LuaValue, value: LuaValue) {
        val keyString = key.checkjstring()
        if (kClassWrapper.containProperty(keyString))
            return kClassWrapper.setProperty(this, keyString, value)
        super.set(key, value)
    }

    private fun checkMetaInfo(luaValue: LuaValue): LuaValue? = when (luaValue.tojstring()) {
        "__property_info" -> LuaValue.valueOf(kClassWrapper.getPropertyInfo())
        "__function_info" -> LuaValue.valueOf(kClassWrapper.getFunctionsInfo())
        else -> null
    }

    fun isSubClassOf(className: String) = kClassWrapper.isSubClassOf(className)
    override fun typename(): String? = instance::class.simpleName
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = m_instance.toString()
}