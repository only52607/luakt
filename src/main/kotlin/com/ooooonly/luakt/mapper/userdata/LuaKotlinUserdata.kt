package com.ooooonly.luakt.mapper.userdata

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
    instance: Any,
    metatable: LuaValue? = null
) : LuaUserdata(
    instance,
    metatable
)