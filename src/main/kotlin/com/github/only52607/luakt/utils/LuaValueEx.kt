package com.github.only52607.luakt.utils

import com.github.only52607.luakt.KValueMapper
import com.github.only52607.luakt.LuaValueMapper
import org.luaj.vm2.*
import kotlin.reflect.KClass

inline fun <reified T> LuaValue.asKValue(luaValueMapper: LuaValueMapper): T =
    asKValue(T::class, luaValueMapper) as T

fun LuaValue.asKValue(
    targetClass: KClass<*>? = null,
    luaValueMapper: LuaValueMapper,
): Any = luaValueMapper.mapToKValue(this, targetClass)

val LuaValue.nullable: LuaValue?
    get() = if (isnil()) null else this

val LuaValue.tableValue: LuaTable
    get() = checktable()

val LuaValue.tableValueOrNull: LuaTable?
    get() = if (istable()) checktable() else null

val LuaValue.functionValue: LuaFunction
    get() = checkfunction()

val LuaValue.functionValueOrNull: LuaFunction?
    get() = if (isfunction()) checkfunction() else null

val LuaValue.closureValue: LuaClosure
    get() = checkclosure()

val LuaValue.closureValueOrNull: LuaClosure?
    get() = if (isclosure()) checkclosure() else null

val LuaValue.threadValue: LuaThread
    get() = checkthread()

val LuaValue.threadValueOrNull: LuaThread?
    get() = if (isthread()) checkthread() else null

val LuaValue.globalsValue: Globals
    get() = checkglobals()

val LuaValue.booleanValue: Boolean
    get() = checkboolean()

val LuaValue.booleanValueOrNull: Boolean?
    get() = if (isboolean()) checkboolean() else null

val LuaValue.intValue: Int
    get() = checkint()

val LuaValue.intValueOrNull: Int?
    get() = if (isint()) checkint() else null

val LuaValue.longValue: Long
    get() = checklong()

val LuaValue.longValueOrNull: Long?
    get() = if (islong()) checklong() else null

val LuaValue.doubleValue: Double
    get() = checkdouble()

val LuaValue.doubleValueOrNull: Double?
    get() = if (isnumber()) checkdouble() else null

val LuaValue.stringValue: String
    get() = checkjstring()

val LuaValue.stringValueOrNull: String?
    get() = if (isstring()) checkjstring() else null

val LuaValue.userdataValue: Any
    get() = checkuserdata()

val LuaValue.userdataValueOrNull: Any?
    get() = if (isuserdata()) checkuserdata() else null


fun Any?.asLuaValue(
    kValueMapper: KValueMapper
): LuaValue = kValueMapper.mapToLuaValue(this)

val Int.luaValue: LuaValue
    get() = LuaValue.valueOf(this)

val Long.luaValue: LuaValue
    get() = if (this >= Int.MIN_VALUE && this <= Int.MAX_VALUE) LuaValue.valueOf(this.toInt()) else LuaValue.valueOf(
        this.toString()
    )

val Boolean.luaValue: LuaValue
    get() = LuaValue.valueOf(this)

val String.luaValue: LuaValue
    get() = LuaValue.valueOf(this)

val Double.luaValue: LuaValue
    get() = LuaValue.valueOf(this)

val ByteArray.luaValue: LuaValue
    get() = LuaValue.valueOf(this)

val Array<LuaValue>.luaListValue: LuaTable
    get() = LuaValue.listOf(this)