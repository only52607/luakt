package com.ooooonly.luakt.mapper

import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: RootValueMapper
 * Description:
 * date: 2022/1/8 19:46
 * @author ooooonly
 * @version
 */
class RootLuaValueMapper : AbstractLuaValueMapper() {
    override var nextLuaValueMapper: LuaValueMapper? = null
    override var firstLuaValueMapper: LuaValueMapper? = this
    override fun mapToKValue(luaValue: LuaValue, targetClass: KClass<*>?): Any {
        return nextMapToKValue(luaValue, targetClass)
    }
}

class RootKValueMapper : AbstractKValueMapper() {
    override var nextKValueMapper: KValueMapper? = null
    override var firstKValueMapper: KValueMapper? = this
    override fun mapToLuaValue(obj: Any?): LuaValue {
        return nextMapToLuaValue(obj)
    }
}