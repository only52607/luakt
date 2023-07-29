package com.github.only52607.luakt

import org.luaj.vm2.LuaDouble
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaValue
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class CoerceKotlinToLua {
    interface Coercion {
        fun coerce(kotlinValue: Any?): LuaValue
    }

    private class BoolCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            val b = kotlinValue as Boolean
            return if (b) LuaValue.TRUE else LuaValue.FALSE
        }
    }

    private class IntCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            val n = kotlinValue as Number
            return LuaInteger.valueOf(n.toInt())
        }
    }

    private class CharCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            val c = kotlinValue as Char
            return LuaInteger.valueOf(c.code)
        }
    }

    private class DoubleCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            val n = kotlinValue as Number
            return LuaDouble.valueOf(n.toDouble())
        }
    }

    private class StringCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            return LuaString.valueOf(kotlinValue.toString())
        }
    }

    private class BytesCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            return LuaValue.valueOf(kotlinValue as ByteArray?)
        }
    }

    private class ClassCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            return KotlinClass.forKClass(kotlinValue as KClass<*>)
        }
    }

    private class InstanceCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            kotlinValue ?: return LuaValue.NIL
            return KotlinInstance(kotlinValue)
        }
    }

    private class ArrayCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            kotlinValue ?: return LuaValue.NIL
            return KotlinArray(kotlinValue)
        }
    }

    private class LuaCoercion : Coercion {
        override fun coerce(kotlinValue: Any?): LuaValue {
            return kotlinValue as LuaValue
        }
    }

    companion object {
        val COERCIONS: MutableMap<KClass<*>, Coercion> = ConcurrentHashMap<KClass<*>, Coercion>()
        private val arrayCoercion: Coercion = ArrayCoercion()
        private val instanceCoercion: Coercion = InstanceCoercion()
        private val luaCoercion: Coercion = LuaCoercion()
        
        init {
            COERCIONS[Boolean::class] = BoolCoercion()
            COERCIONS[Byte::class] = IntCoercion()
            COERCIONS[Char::class] = CharCoercion()
            COERCIONS[Short::class] = IntCoercion()
            COERCIONS[Int::class] = IntCoercion()
            COERCIONS[Long::class] = DoubleCoercion()
            COERCIONS[Float::class] = DoubleCoercion()
            COERCIONS[Double::class] = DoubleCoercion()
            COERCIONS[String::class] = StringCoercion()
            COERCIONS[ByteArray::class] = BytesCoercion()
            COERCIONS[Class::class] = ClassCoercion()
        }
        
        fun coerce(o: Any?): LuaValue {
            if (o == null) return LuaValue.NIL
            val clazz = o::class
            val c = COERCIONS[clazz] ?: when (o) {
                is Array<*> -> arrayCoercion
                is LuaValue -> luaCoercion
                else -> instanceCoercion
            }.also { COERCIONS[clazz] = it }
            return c.coerce(o)
        }
    }
}