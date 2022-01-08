package com.ooooonly.luakt.mapper

import com.ooooonly.luakt.mapper.impl.*
import com.ooooonly.luakt.mapper.userdata.ConcurrentKotlinClassWrapperRegistry
import com.ooooonly.luakt.mapper.userdata.KClassExtensionProvider

val defaultLuaValueMapper: LuaValueMapper by lazy {
    UserDataLuaValueMapper().apply {
        appendNext(
            BaseLuaValueMapper()
        ).appendNext(
            CollectionLuaValueMapper()
        ).appendNext(
            OriginalLuaValueMapper()
        )
    }
}

val defaultKValueMapper: KValueMapper by lazy {
    BaseKValueMapper().apply {
        appendNext(
            CollectionKValueMapper()
        ).appendNext(
            UserDataKValueMapper(
                ConcurrentKotlinClassWrapperRegistry(
                    this + defaultLuaValueMapper,
                    kClassExtensionProvider = object : KClassExtensionProvider {}
                )
            )
        )
    }
}