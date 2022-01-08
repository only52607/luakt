package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/*
    Varargs
        LuaValue
            LuaUserdata
            LuaTable(Metatable)
                Globals
            LuaFunction
                LuaClosure
                LibFunction
                    ZeroArgFunction
                    TwoArgFunction
                        BaseLib(ResourceFinder)
                        PackageLib
                        IoLib
                        DebugLib
                        StringLib
                    ThreeArgFunction
                    VarArgFunction
*/

fun interface KValueMapper {
    fun mapToLuaValue(obj: Any?): LuaValue
}

interface LuaValueMapper {
    // Automatically select an appropriate Mapper when targetClass is null
    fun mapToKValue(luaValue: LuaValue, targetClass: KClass<*>?): Any

    fun mapToKValueNullable(luaValue: LuaValue, targetClass: KClass<*>?): Any?
}

inline fun <reified T> LuaValueMapper.mapToKValue(luaValue: LuaValue): T {
    return mapToKValue(luaValue, T::class) as T
}

interface ValueMapper : KValueMapper, LuaValueMapper

private fun buildValueMapper(kValueMapper: KValueMapper, luaValueMapper: LuaValueMapper): ValueMapper {
    return object : ValueMapper, KValueMapper by kValueMapper, LuaValueMapper by luaValueMapper {}
}

operator fun KValueMapper.plus(luaValueMapper: LuaValueMapper): ValueMapper = buildValueMapper(this, luaValueMapper)

operator fun LuaValueMapper.plus(kValueMapper: KValueMapper): ValueMapper = buildValueMapper(kValueMapper, this)

class CouldNotMapToLuaValueException(override val message: String) : Exception(message)

class CouldNotMapToKValueException(override val message: String) : Exception(message)