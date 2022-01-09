package com.ooooonly.luakt.mapper.userdata

import org.luaj.vm2.LuaValue

open class LuaKotlinObject<T : Any>(
    val instance: T,
    private val luaKotlinClass: LuaKotlinClass
) : LuaKotlinUserdata(instance) {

    override fun get(key: LuaValue): LuaValue {
        checkMetaInfo(key)?.let { return@get it }
        val keyString = key.checkjstring()
        if (luaKotlinClass.containsProperty(keyString))
            return luaKotlinClass.getProperty(instance, keyString)
        if (luaKotlinClass.containsFunction(keyString))
            return luaKotlinClass.getFunction(keyString)
        return super.get(key)
    }

    override fun set(key: LuaValue, value: LuaValue) {
        val keyString = key.checkjstring()
        if (luaKotlinClass.containsProperty(keyString))
            return luaKotlinClass.setProperty(instance, keyString, value)
        super.set(key, value)
    }

    private fun checkMetaInfo(luaValue: LuaValue): LuaValue? = when (luaValue.tojstring()) {
        "__class" -> luaKotlinClass
        "__properties" -> luaKotlinClass.getAllProperties(instance)
        "__functions" -> luaKotlinClass.getAllFunctions()
        else -> null
    }

    override fun typename(): String? = luaKotlinClass.kClass.simpleName
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = instance.toString()
}