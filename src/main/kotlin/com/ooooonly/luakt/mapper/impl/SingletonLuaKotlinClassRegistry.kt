package com.ooooonly.luakt.mapper.impl

import com.ooooonly.luakt.mapper.ValueMapper
import com.ooooonly.luakt.mapper.userdata.LuaKotlinClass
import com.ooooonly.luakt.mapper.userdata.LuaKotlinClassRegistry
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
    private val valueMapper: ValueMapper,
    private val builder: (kClass: KClass<*>) -> LuaKotlinClass<*>
) : LuaKotlinClassRegistry {
    private val luaKotlinClasses = ConcurrentHashMap<KClass<*>, LuaKotlinClass<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> obtainLuaKotlinClass(kClass: KClass<T>): LuaKotlinClass<T> {
        val lk: LuaKotlinClass<*>? = luaKotlinClasses[kClass]
        if (lk != null) return lk as LuaKotlinClass<T>
        val newLk = builder(kClass)
        luaKotlinClasses[kClass] = newLk
        return newLk as LuaKotlinClass<T>
    }
}