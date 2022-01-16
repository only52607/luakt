package com.github.only52607.luakt.userdata

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * ClassName: KClassExtensionProvider
 * Description:
 * date: 2022/1/8 13:26
 * @author ooooonly
 * @version
 */
interface KClassExtensionProvider {
    fun provideExtensionFunctions(kClass: KClass<*>): Collection<KFunction<*>> {
        return emptyList()
    }

    fun provideExtensionProperties(kClass: KClass<*>): Collection<KProperty<*>> {
        return emptyList()
    }
}

object EmptyKClassExtensionProvider : KClassExtensionProvider