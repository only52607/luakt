package com.github.only52607.luakt.userdata.classes.registry.impl

import com.github.only52607.luakt.userdata.classes.AbstractLuaKotlinClass
import com.github.only52607.luakt.userdata.classes.registry.LuaKotlinClassRegistry
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * ClassName: SingletonLuaKotlinClassRegistry
 * Description:
 * date: 2022/1/8 20:56
 * @author ooooonly
 * @version
 */
class SingletonLuaKotlinClassRegistry(
    private val builder: (kClass: KClass<*>) -> AbstractLuaKotlinClass
) : LuaKotlinClassRegistry {
    private val luaKotlinClasses = ConcurrentHashMap<KClass<*>, AbstractLuaKotlinClass>()

    @Suppress("UNCHECKED_CAST")
    override fun obtainLuaKotlinClass(kClass: KClass<*>): AbstractLuaKotlinClass {
        val lk: AbstractLuaKotlinClass? = luaKotlinClasses[kClass]
        if (lk != null) return lk
        val newLk = builder(kClass)
        luaKotlinClasses[kClass] = newLk
        return newLk
    }
}