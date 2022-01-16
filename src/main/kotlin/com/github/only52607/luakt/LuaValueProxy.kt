package com.github.only52607.luakt

import com.github.only52607.luakt.userdata.function.LuaKotlinMemberFunction
import org.luaj.vm2.LuaValue
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

/**
 * ClassName: LuaValueProxy
 * Description:
 * date: 2022/1/15 14:57
 * @author ooooonly
 * @version
 */
abstract class LuaValueProxy(
    protected val luaValue: LuaValue,
    protected val valueMapper: ValueMapper
) {
    init {
        setupFields()
    }

    private fun setupFields() {
        val clazz = this::class
        val members = clazz.memberFunctions + clazz.memberProperties

        val annotatedLuaMemberFunctions = clazz.memberFunctions.filter { f ->
            f.annotations.find { it::class == LuaFunctionField::class } != null
        }
        val annotatedVarArgMemberFunctions = clazz.memberFunctions.filter { f ->
            f.annotations.find { it::class == VarArgFunctionField::class } != null
        }
        val annotatedMemberProperties = clazz.memberProperties.filter { p ->
            p.annotations.find { it::class == PropertyField::class } != null
        }
        annotatedLuaMemberFunctions.forEach { f ->
            var key = f.name
            var keyAlias: Array<out String>? = null
            var value: LuaValue = LuaValue.NIL
            f.annotations.forEach {
                when (it) {
                    is LuaFunctionField -> {
                        key = it.key.takeIf(String::isNotEmpty) ?: key
                        value = LuaKotlinMemberFunction(
                            f,
                            valueMapper,
                            instanceOrReceiver = this,
                            description = it.description.takeIf(String::isNotEmpty)
                        )
                    }
//                    is VarArgFunctionField -> {
//                        key = it.key.takeIf(String::isNotEmpty) ?: key
//                        value = LuaKotlinVarargFunction(f, instance = this, description = it.description.takeIf(String::isNotEmpty))
//                    }
                    is KeyAlias -> keyAlias = it.aliases
                }
            }
            luaValue.set(key, value)
            keyAlias?.forEach { luaValue.set(it, value) }
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    protected annotation class LuaFunctionField(val key: String = "", val description: String = "")

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    protected annotation class VarArgFunctionField(val key: String = "", val description: String = "")

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.PROPERTY)
    protected annotation class PropertyField(val key: String = "")

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    protected annotation class KeyAlias(vararg val aliases: String)
}