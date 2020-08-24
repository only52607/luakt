package com.ooooonly.luakt.luakotlin

import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue


open class KotlinInstanceInLua(
    instance: Any,
    val kClassInLua: KotlinClassInLua = KotlinClassInLua.forKClass(
        instance::class
    )
) : LuaUserdata(instance) {
    override fun get(key: LuaValue): LuaValue {
        val keyString = key.checkjstring()
        if (kClassInLua.containProperty(keyString))
            return kClassInLua.getProperty(this, keyString)
        if (kClassInLua.containFunction(keyString))
            return kClassInLua.getLuaFunction(keyString)
        return LuaValue.NIL
    }

    override fun set(key: LuaValue, value: LuaValue) {
        val keyString = key.checkjstring()
        if (kClassInLua.containProperty(keyString))
            return kClassInLua.setProperty(this, keyString, value)
        super.set(key, value)
    }
}
