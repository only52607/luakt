package com.github.only52607.luakt.userdata

import com.github.only52607.luakt.userdata.classes.LuaKotlinClass
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
    private val builder: (kClass: KClass<*>) -> LuaKotlinClass
) : LuaKotlinClassRegistry {
    private val luaKotlinClasses = ConcurrentHashMap<KClass<*>, LuaKotlinClass>()

    @Suppress("UNCHECKED_CAST")
    override fun obtainLuaKotlinClass(kClass: KClass<*>): LuaKotlinClass {
        val lk: LuaKotlinClass? = luaKotlinClasses[kClass]
        if (lk != null) return lk
        val newLk = builder(kClass)
        luaKotlinClasses[kClass] = newLk
        return newLk
    }
}