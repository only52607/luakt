package com.github.only52607.luakt.mappers

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.plus
import com.github.only52607.luakt.userdata.classes.EmptyKClassExtensionProvider
import com.github.only52607.luakt.userdata.classes.LuaKotlinClass
import com.github.only52607.luakt.userdata.classes.registry.impl.SingletonLuaKotlinClassRegistry

val defaultValueMapper: ValueMapper by lazy {
    val defaultLuaValueMapper = RootLuaValueMapper()
    val defaultKValueMapper = RootKValueMapper()
    val resultValueMapper = defaultLuaValueMapper + defaultKValueMapper

    defaultLuaValueMapper.append(
        UserDataLuaValueMapper(),
        BaseLuaValueMapper(),
        CollectionLuaValueMapper(),
        OriginalLuaValueMapper()
    )

    defaultKValueMapper.append(
        BaseKValueMapper(),
        CollectionKValueMapper(),
        UserDataKValueMapper(
            SingletonLuaKotlinClassRegistry {
                LuaKotlinClass(it, resultValueMapper, EmptyKClassExtensionProvider)
            }
        )
    )
    resultValueMapper
}

fun <R> withDefaultValueMapper(block: ValueMapper.() -> R) = with(defaultValueMapper, block)