package com.github.only52607.luakt.userdata.classes

import com.github.only52607.luakt.userdata.LuaKotlinUserdata
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
abstract class LuaKotlinClass(
    val kClass: KClass<*>
) : LuaKotlinUserdata(kClass) {
    abstract fun containsProperty(name: String): Boolean

    abstract fun containsFunction(name: String): Boolean

    abstract fun setProperty(self: Any, name: String, value: LuaValue)

    abstract fun getProperty(self: Any, name: String): LuaValue

    abstract fun getFunction(name: String): LuaValue

    abstract fun getAllProperties(self: Any): LuaTable

    abstract fun getAllFunctions(): LuaTable
}