package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/*
    LuaJ 数据结构继承树

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

interface KValueMapper {
    /* Returns null if there is no matching mapper */
    fun mapToLuaValue(obj: Any, defaultValueMapperChain: ValueMapperChain?): LuaValue?
}

interface LuaValueMapper {
    /* Returns null if there is no matching mapper */
    fun mapToKValue(luaValue: LuaValue, targetClass: KClass<*>, defaultValueMapperChain: ValueMapperChain?): Any?
}

interface ValueMapper : KValueMapper, LuaValueMapper

class KValueMapFailException(override val message: String) : Exception(message)
class LuaValueMapFailException(override val message: String) : Exception(message)