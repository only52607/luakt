package com.ooooonly.luakt.mapper.userdata

import kotlin.reflect.KClass

/**
 * ClassName: KotlinClassWrapperRegistry
 * Description:
 * date: 2022/1/8 13:35
 * @author ooooonly
 * @version
 */

interface LuaKotlinClassRegistry {
    fun <T : Any> obtainLuaKotlinClass(kClass: KClass<T>): LuaKotlinClass<T>
}