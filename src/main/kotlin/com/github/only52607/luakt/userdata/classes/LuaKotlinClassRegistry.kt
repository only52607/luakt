package com.github.only52607.luakt.userdata.classes

import kotlin.reflect.KClass

/**
 * ClassName: KotlinClassWrapperRegistry
 * Description:
 * date: 2022/1/8 13:35
 * @author ooooonly
 * @version
 */

interface LuaKotlinClassRegistry {
    fun obtainLuaKotlinClass(kClass: KClass<*>): LuaKotlinClass
}