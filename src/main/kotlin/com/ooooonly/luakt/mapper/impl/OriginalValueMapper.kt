package com.ooooonly.luakt.mapper.impl

import com.ooooonly.luakt.mapper.AbstractLuaValueMapper
import com.ooooonly.luakt.mapper.LuaValueMapper
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: DefaultValueMapper
 * Description:
 * date: 2022/1/7 19:40
 * @author ooooonly
 * @version
 */

class OriginalLuaValueMapper(
    override var nextLuaValueMapper: LuaValueMapper? = null,
    override var firstLuaValueMapper: LuaValueMapper? = null
) : AbstractLuaValueMapper() {
    override fun mapToKValue(
        luaValue: LuaValue,
        targetClass: KClass<*>?
    ): Any {
        if (targetClass == null) return luaValue
        return nextMapToKValue(luaValue, targetClass)
    }
}