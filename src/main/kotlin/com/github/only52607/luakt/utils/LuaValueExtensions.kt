@file:Suppress("UNUSED")

package com.github.only52607.luakt.utils

import com.github.only52607.luakt.KValueMapper
import com.github.only52607.luakt.LuaValueMapper
import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

context (LuaValueMapper)fun LuaValue.asKValue(
    targetClass: KClass<*>? = null
): Any = mapToKValue(this, targetClass)

context (LuaValueMapper)fun LuaValue.asKValue(
    targetType: KType? = null
): Any = mapToKValue(this, targetType?.jvmErasure)

context (LuaValueMapper)inline fun <reified T> LuaValue.asKValue(): T =
    asKValue(T::class) as T

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

context (KValueMapper)
fun Any?.asLuaValue(): LuaValue = mapToLuaValue(this)

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

context (ValueMapper)operator fun LuaValue.get(key: Any): LuaValue = get(mapToLuaValue(key))

context (ValueMapper) @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline operator fun <reified R> LuaValue.get(key: Any): R = get(key).asKValue()

context (ValueMapper)fun LuaValue.getOrNull(key: Any): LuaValue? =
    get(mapToLuaValue(key))?.takeIf { it != LuaValue.NIL }

context (ValueMapper)inline fun <reified R> LuaValue.getOrNull(key: Any): R? =
    mapToKValueNullable(get(mapToLuaValue(key)), R::class) as R?

// setters

context (ValueMapper)operator fun LuaValue.set(key: String, value: Any) = set(key, mapToLuaValue(value))

context (ValueMapper)operator fun LuaValue.set(key: Any, value: Any) =
    set(mapToLuaValue(key), mapToLuaValue(value))


// invokers

context (ValueMapper)@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
operator fun LuaValue.invoke(varargs: Varargs): Varargs = invoke(varargs)

context (ValueMapper)@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline operator fun <reified R> LuaValue.invoke(varargs: Varargs): R = invoke(varargs).arg1().asKValue()

context (ValueMapper)operator fun LuaValue.invoke(vararg args: Any): Varargs =
    invoke(args.map { mapToLuaValue(it) }.toTypedArray())

context (ValueMapper)inline operator fun <reified R> LuaValue.invoke(vararg args: Any): R =
    invoke(args.map { mapToLuaValue(it) }.toTypedArray()).arg1().asKValue()


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


context(ValueMapper, LuaValue)  @Deprecated(
    "Using set field method instead",
    ReplaceWith("tableValue.set(this, mapToLuaValue(value))")
)
infix fun Iterable<*>.nto(value: Any) {
    forEach {
        tableValue.set(mapToLuaValue(it), mapToLuaValue(value))
    }
}

context(ValueMapper, LuaValue) @Deprecated(
    "Using set field method instead",
    ReplaceWith("tableValue.set(this, mapToLuaValue(value))")
)
infix fun String.to(value: Any) {
    tableValue.set(this, mapToLuaValue(value))
}


context(ValueMapper, LuaValue)@Deprecated(
    "Using set field method instead",
    ReplaceWith("tableValue.set(this, mapToLuaValue(value))")
)
infix fun Any.to(value: Any) {
    tableValue.set(mapToLuaValue(this), mapToLuaValue(value))
}

context(ValueMapper, LuaValue)operator fun LuaValue.unaryPlus() {
    tableValue.insert(tableValue.keyCount(), this)
}

context(ValueMapper)inline fun luaTableOf(builder: LuaValue.() -> Unit): LuaValue =
    LuaTable().apply(builder).tableValue

context(ValueMapper)fun <K : Any, V : Any> Map<K, V>.asLuaTable(): LuaValue = luaTableOf {
    this@asLuaTable.forEach { (t, u) ->
        set(t.asLuaValue(), u.asLuaValue())
    }
}

context(ValueMapper)fun <T> Iterator<T>.asLuaTable(): LuaValue = luaTableOf {
    this@asLuaTable.forEach {
        tableValue.insert(tableValue.keyCount(), it.asLuaValue())
    }
}

// metatables

var LuaValue.metatable: LuaValue?
    get() = getmetatable()
    set(value) {
        setmetatable(value)
    }