@file:Suppress("UNUSED")

package com.github.only52607.luakt.dsl.cr
import com.github.only52607.luakt.KValueMapper
import com.github.only52607.luakt.LuaValueMapper
import com.github.only52607.luakt.ValueMapper
import com.github.only52607.luakt.dsl.tableValue
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import org.luaj.vm2.*

context (LuaValueMapper)fun LuaValue.asKValue(
    targetClass: KClass<*>? = null
): Any = mapToKValue(this, targetClass)

context (LuaValueMapper)fun LuaValue.asKValue(
    targetType: KType? = null
): Any = mapToKValue(this, targetType?.jvmErasure)

context (LuaValueMapper)inline fun <reified T> LuaValue.asKValue(): T =
    asKValue(T::class) as T

context (KValueMapper)
fun Any?.asLuaValue(): LuaValue = mapToLuaValue(this)

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

context(ValueMapper, LuaValue)  @Deprecated(
    "Using set field method instead",
    ReplaceWith("tableValue.set(this, mapToLuaValue(value))")
)
infix fun Iterable<*>.nto(value: Any) {
    forEach {
        tableValue.set(mapToLuaValue(it), mapToLuaValue(value))
    }
}

context(ValueMapper)inline fun luaTableOf(builder: LuaValue.() -> Unit): LuaValue =
    org.luaj.vm2.LuaTable().apply(builder).tableValue

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