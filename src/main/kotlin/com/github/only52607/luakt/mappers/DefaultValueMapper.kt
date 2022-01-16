package com.github.only52607.luakt.mappers

import com.github.only52607.luakt.userdata.EmptyKClassExtensionProvider
import com.github.only52607.luakt.userdata.SingletonLuaKotlinClassRegistry
import com.github.only52607.luakt.userdata.classes.LuaKotlinClassCallable
import com.github.only52607.luakt.userdata.impl.*

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
                LuaKotlinClassCallable(it, resultValueMapper, EmptyKClassExtensionProvider)
            }
        )
    )
    resultValueMapper
}