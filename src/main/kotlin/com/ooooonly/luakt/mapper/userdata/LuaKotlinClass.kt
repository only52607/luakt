package com.ooooonly.luakt.mapper.userdata

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: LuaKotlinClass
 * Description:
 * date: 2022/1/8 20:44
 * @author ooooonly
 * @version
 */
abstract class LuaKotlinClass<T : Any>(
    val kClass: KClass<T>
) : LuaKotlinUserdata(kClass) {
    abstract fun containsProperty(name: String): Boolean

    abstract fun containsFunction(name: String): Boolean

    abstract fun setProperty(self: T, name: String, value: LuaValue)

    abstract fun getProperty(self: T, name: String): LuaValue

    abstract fun getFunction(name: String): LuaValue

    abstract fun getAllProperties(self: T): LuaTable

    abstract fun getAllFunctions(): LuaTable
}