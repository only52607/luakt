package com.github.only52607.luakt.utils

import com.github.only52607.luakt.LuaValueFieldProperty
import com.github.only52607.luakt.ValueMapper
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.reflect.KClass

/**
 * ClassName: ValueMapperScope
 * Description:
 * date: 2022/1/9 20:34
 * @author ooooonly
 * @version
 */
abstract class ValueMapperScope(
    val valueMapper: ValueMapper
) {
    inline fun <reified T> LuaValue.asKValue(): T = asKValue(valueMapper)

    fun LuaValue.asKValue(
        targetClass: KClass<*>? = null
    ): Any = valueMapper.mapToKValue(this, targetClass)

    fun Any?.asLuaValue(): LuaValue = valueMapper.mapToLuaValue(this)

    fun <T : Any?> LuaValue.field(
        key: String? = null,
        defaultValue: T? = null
    ) = LuaValueFieldProperty(valueMapper, this, key, defaultValue)

    operator fun LuaTable.get(key: Any): LuaValue =
        get(key, valueMapper)

    operator fun LuaTable.set(key: Any, value: Any) =
        set(key, value, valueMapper)

    fun LuaTable.getOrNull(key: Any): LuaValue? =
        getOrNull(key, valueMapper)

    inline fun buildLuaTable(builder: LuaTableBuilderScope.() -> Unit): LuaTable =
        buildLuaTable(valueMapper, builder)

    inline fun LuaTable.edit(process: LuaTableBuilderScope.() -> Unit) =
        edit(valueMapper, process)

    fun luaFunctionOf(block: () -> Any) = varArgFunctionOf {
        return@varArgFunctionOf block().asLuaValue(valueMapper)
    }

    inline fun <reified T0> luaFunctionOf(crossinline block: (T0) -> Any) =
        varArgFunctionOf { args ->
            return@varArgFunctionOf block(args[0].asKValue(valueMapper)).asLuaValue(valueMapper)
        }

    inline fun <reified T0, reified T1> luaFunctionOf(crossinline block: (T0, T1) -> Any) =
        varArgFunctionOf { args ->
            return@varArgFunctionOf block(args[0].asKValue(valueMapper), args[1].asKValue(valueMapper)).asLuaValue(
                valueMapper
            )
        }

    inline fun <reified T0, reified T1, reified T2> luaFunctionOf(
        crossinline block: (T0, T1, T2) -> Any
    ) =
        varArgFunctionOf { args ->
            block(
                args[0].asKValue(valueMapper),
                args[1].asKValue(valueMapper),
                args[2].asKValue(valueMapper)
            ).asLuaValue(
                valueMapper
            )
        }

    inline fun <reified T0, reified T1, reified T2, reified T3> luaFunctionOf(
        crossinline block: (T0, T1, T2, T3) -> Any
    ) =
        varArgFunctionOf { args ->
            block(
                args[0].asKValue(valueMapper),
                args[1].asKValue(valueMapper),
                args[2].asKValue(valueMapper),
                args[3].asKValue(valueMapper)
            ).asLuaValue(valueMapper)
        }

    inline fun <reified T0, reified T1, reified T2, reified T3, reified T4> luaFunctionOf(
        crossinline block: (T0, T1, T2, T3, T4) -> Any
    ) =
        varArgFunctionOf { args ->
            block(
                args[0].asKValue(valueMapper),
                args[1].asKValue(valueMapper),
                args[2].asKValue(valueMapper),
                args[3].asKValue(valueMapper),
                args[4].asKValue(valueMapper)
            ).asLuaValue(valueMapper)
        }

    inline fun <
            reified T0,
            reified T1,
            reified T2,
            reified T3,
            reified T4,
            reified T5> luaFunctionOf(crossinline block: (T0, T1, T2, T3, T4, T5) -> Any) =
        varArgFunctionOf { args ->
            block(
                args[0].asKValue(valueMapper),
                args[1].asKValue(valueMapper),
                args[2].asKValue(valueMapper),
                args[3].asKValue(valueMapper),
                args[4].asKValue(valueMapper),
                args[5].asKValue(valueMapper)
            ).asLuaValue(valueMapper)
        }

    inline fun <
            reified T0,
            reified T1,
            reified T2,
            reified T3,
            reified T4,
            reified T5,
            reified T6>
            luaFunctionOf(crossinline block: (T0, T1, T2, T3, T4, T5, T6) -> Any) =
        varArgFunctionOf { args ->
            block(
                args[0].asKValue(valueMapper),
                args[1].asKValue(valueMapper),
                args[2].asKValue(valueMapper),
                args[3].asKValue(valueMapper),
                args[4].asKValue(valueMapper),
                args[5].asKValue(valueMapper),
                args[6].asKValue(valueMapper)
            ).asLuaValue(valueMapper)
        }
}

fun <T> ValueMapper.provideScope(block: ValueMapperScope.() -> T): T =
    object : ValueMapperScope(this) {}.block()