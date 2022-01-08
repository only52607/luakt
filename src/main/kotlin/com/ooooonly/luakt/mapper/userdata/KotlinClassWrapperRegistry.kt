package com.ooooonly.luakt.mapper.userdata

import com.ooooonly.luakt.mapper.ValueMapper
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * ClassName: KotlinClassWrapperRegistry
 * Description:
 * date: 2022/1/8 13:35
 * @author ooooonly
 * @version
 */

interface KotlinClassWrapperRegistry {
    fun obtainClassWrapper(kClass: KClass<*>): KotlinClassWrapper
}

class ConcurrentKotlinClassWrapperRegistry(
    private val valueMapper: ValueMapper,
    private val kClassExtensionProvider: KClassExtensionProvider
) : KotlinClassWrapperRegistry {
    private val classWrappers = ConcurrentHashMap<KClass<*>, KotlinClassWrapper>()

    override fun obtainClassWrapper(kClass: KClass<*>): KotlinClassWrapper {
        val wrapper: KotlinClassWrapper? = classWrappers[kClass]
        if (wrapper != null) return wrapper
        val newWrapper = KotlinClassWrapper(kClass, valueMapper, kClassExtensionProvider)
        classWrappers[kClass] = newWrapper
        return newWrapper
    }
}