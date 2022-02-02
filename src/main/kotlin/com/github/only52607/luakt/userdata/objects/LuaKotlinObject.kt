package com.github.only52607.luakt.userdata.objects

import com.github.only52607.luakt.userdata.LuaKotlinUserdata
import com.github.only52607.luakt.userdata.classes.AbstractLuaKotlinClass
import org.luaj.vm2.LuaValue

open class LuaKotlinObject(
    val instance: Any,
    internal val luaKotlinClass: AbstractLuaKotlinClass
) : LuaKotlinUserdata(instance) {

    override fun get(key: LuaValue): LuaValue {
        checkMetaInfo(key)?.let { return@get it }
        val keyString = key.checkjstring()
        if (luaKotlinClass.containsMemberProperty(keyString))
            return luaKotlinClass.getMemberProperty(instance, keyString)
        if (luaKotlinClass.containsMemberFunction(keyString))
            return luaKotlinClass.getMemberFunction(keyString)
        return super.get(key)
    }

    override fun set(key: LuaValue, value: LuaValue) {
        val keyString = key.checkjstring()
        if (luaKotlinClass.containsMemberProperty(keyString))
            return luaKotlinClass.setMemberProperty(instance, keyString, value)
        super.set(key, value)
    }

    private fun checkMetaInfo(luaValue: LuaValue): LuaValue? = when (luaValue.tojstring()) {
        "__class" -> luaKotlinClass
        "__properties" -> luaKotlinClass.getAllMemberProperties(instance)
        "__functions" -> luaKotlinClass.getAllMemberFunctions()
        else -> null
    }

    override fun typename(): String? = luaKotlinClass.kClass.simpleName
    override fun tostring(): LuaValue = LuaValue.valueOf(toString())
    override fun toString(): String = instance.toString()
}