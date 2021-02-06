package com.ooooonly.luakt.mapper.userdata

import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue

open class KotlinInstanceWrapper(
    instance: Any = Any(),
    private val kClassWrapper: KotlinClassWrapper = KotlinClassWrapper.forKClass(instance::class)
) : LuaUserdata(instance) {

    override fun get(key: LuaValue): LuaValue {
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

    override fun toString(): String = m_instance.toString()
}