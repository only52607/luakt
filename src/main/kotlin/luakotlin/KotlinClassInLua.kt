package luakotlin

import asKValue
import asLuaValue
import luaFunctionOfKFunction
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure


class KotlinClassInLua(kClass: KClass<*>) {
    companion object {
        private val classes: MutableMap<KClass<*>, KotlinClassInLua> = Collections.synchronizedMap(HashMap())
        fun forKClass(c: KClass<*>): KotlinClassInLua {
            val j: KotlinClassInLua? = classes[c]
            return j?.let {
                it
            } ?: run {
                KotlinClassInLua(c).also {
                    classes[c] = it
                }
            }
        }
    }

    var properties: MutableMap<String, KProperty<*>>
    var kFunctions: MutableMap<String, KFunction<*>>
    var luaFunctions: MutableMap<String, LuaFunction>

    init {
        properties = mutableMapOf()
        kClass.declaredMemberProperties.forEach {
            properties[it.name] = it
        }
        kFunctions = mutableMapOf()
        kClass.declaredMemberFunctions.forEach {
            kFunctions[it.name] = it
        }
        luaFunctions = mutableMapOf()
    }

    fun containProperty(name: String) = properties.containsKey(name)
    fun containFunction(name: String) = kFunctions.containsKey(name)

    fun setProperty(self: KotlinInstanceInLua, name: String, value: LuaValue) {
        properties[name]?.let { property ->
            if (!property.isConst) {
                (property as KMutableProperty).let { mutableProperty ->
                    val setter = mutableProperty.setter
                    setter.call(self.m_instance, value.asKValue(setter.parameters[1].type.jvmErasure))
                }
            }
        }
    }

    fun getProperty(self: KotlinInstanceInLua, name: String): LuaValue =
        properties[name]?.let { property ->
            property.getter.call(self.m_instance)?.asLuaValue() ?: LuaValue.NIL
        } ?: LuaValue.NIL

    fun getLuaFunction(name: String): LuaFunction {
        if (!luaFunctions.containsKey(name)) {
            luaFunctions[name] = luaFunctionOfKFunction(kFunctions[name]!!)
        }
        return luaFunctions[name]!!
    }
}
