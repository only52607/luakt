package com.github.only52607.luakt.dsl.cr

import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.dsl.LuaValueFieldProperty
import org.luaj.vm2.LuaValue

/**
 * ClassName: LuaTableFieldProperty
 * Description:
 * date: 2022/1/15 22:18
 * @author ooooonly
 * @version
 */
context (ValueMapper)
fun <T : Any?> LuaValue.field(
    key: String? = null,
    defaultValue: T? = null
) = LuaValueFieldProperty(this@ValueMapper, this, key, defaultValue)