package com.github.only52607.luakt.userdata

import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue

/**
 * ClassName: LuaKotlinUserdata
 * Description:
 * date: 2022/1/8 20:48
 * @author ooooonly
 * @version
 */
abstract class LuaKotlinUserdata(
    instance: Any? = null,
    metatable: LuaValue? = null
) : LuaUserdata(
    instance,
    metatable
) {
    var instance: Any?
        get() = m_instance
        set(value) {
            m_instance = value
        }
}