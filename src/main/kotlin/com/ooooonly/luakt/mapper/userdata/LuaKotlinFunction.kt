package com.ooooonly.luakt.mapper.userdata

import org.luaj.vm2.lib.VarArgFunction
import kotlin.reflect.KFunction

/**
 * ClassName: LuaKotlinFunction
 * Description:
 * date: 2022/1/8 21:18
 * @author ooooonly
 * @version
 */
abstract class LuaKotlinFunction(
    val kFunction: KFunction<*>,
    private val kReflectInfoBuilder: KReflectInfoBuilder
) : VarArgFunction() {
    override fun toString(): String = kReflectInfoBuilder.buildKFunctionInfo(kFunction)
}