@file:Suppress("UNUSED")

package com.github.only52607.luakt.dsl

import org.luaj.vm2.*

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

// getters

// setters

// invokers

// collection operators

fun LuaValue.forEach(process: (key: LuaValue, value: LuaValue) -> Unit) {
    var k: LuaValue = LuaValue.NIL
    while (true) {
        val n = next(k)
        k = n.arg1()
        if (k.isnil())
            break
        val v = n.arg(2)
        process(k, v)
    }
}

@Deprecated("Using applyFrom instead")
fun LuaValue.setFrom(luaTable: LuaTable) {
    luaTable.forEach { key, value -> set(key, value) }
}

fun LuaValue.applyFrom(luaTable: LuaTable) {
    luaTable.forEach { key, value -> set(key, value) }
}

// metatables

var LuaValue.metatable: LuaValue?
    get() = getmetatable()
    set(value) {
        setmetatable(value)
    }