package com.ooooonly.luakt.mapper

import com.ooooonly.luakt.mapper.impl.*
import com.ooooonly.luakt.mapper.userdata.EmptyKClassExtensionProvider

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