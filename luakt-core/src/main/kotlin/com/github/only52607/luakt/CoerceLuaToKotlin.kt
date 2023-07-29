package com.github.only52607.luakt

import com.github.only52607.luakt.lib.LuaKotlinLib
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceLuaToJava
import java.lang.reflect.*
import java.lang.reflect.Array
import java.util.concurrent.ConcurrentHashMap
import kotlin.Byte
import kotlin.ByteArray
import kotlin.Char
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.String
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

class CoerceLuaToKotlin : CoerceLuaToJava() {
    class BoolCoercion : Coercion {
        override fun toString(): String {
            return "BoolCoercion()"
        }

        override fun score(value: LuaValue): Int {
            when (value.type()) {
                LuaValue.TBOOLEAN -> return 0
            }
            return 1
        }

        override fun coerce(value: LuaValue): Any {
            return value.toboolean()
        }
    }

    class NumericCoercion(private val targetType: Int) : Coercion {
        override fun toString(): String {
            return "NumericCoercion(" + TYPE_NAMES[targetType] + ")"
        }

        override fun score(value: LuaValue): Int {
            var v = value
            var fromStringPenalty = 0
            if (v.type() == LuaValue.TSTRING) {
                v = v.tonumber()
                if (v.isnil()) {
                    return SCORE_UNCOERCIBLE
                }
                fromStringPenalty = 4
            }
            return if (v.isint()) {
                when (targetType) {
                    TARGET_TYPE_BYTE -> {
                        val i = v.toint()
                        fromStringPenalty + if (i == i.toByte().toInt()) 0 else SCORE_WRONG_TYPE
                    }

                    TARGET_TYPE_CHAR -> {
                        val i = v.toint()
                        fromStringPenalty + when (i) {
                            i.toByte().toInt() -> 1
                            i.toChar().code -> 0
                            else -> SCORE_WRONG_TYPE
                        }
                    }

                    TARGET_TYPE_SHORT -> {
                        val i = v.toint()
                        fromStringPenalty + when (i) {
                            i.toByte().toInt() -> 1
                            i.toShort().toInt() -> 0
                            else -> SCORE_WRONG_TYPE
                        }
                    }

                    TARGET_TYPE_INT -> {
                        val i = v.toint()
                        fromStringPenalty + when {
                            i == i.toByte().toInt() -> 2
                            i == i.toChar().code || i == i.toShort().toInt() -> 1
                            else -> 0
                        }
                    }

                    TARGET_TYPE_FLOAT -> fromStringPenalty + 1
                    TARGET_TYPE_LONG -> fromStringPenalty + 1
                    TARGET_TYPE_DOUBLE -> fromStringPenalty + 2
                    else -> SCORE_WRONG_TYPE
                }
            } else if (v.isnumber()) {
                when (targetType) {
                    TARGET_TYPE_BYTE -> SCORE_WRONG_TYPE
                    TARGET_TYPE_CHAR -> SCORE_WRONG_TYPE
                    TARGET_TYPE_SHORT -> SCORE_WRONG_TYPE
                    TARGET_TYPE_INT -> SCORE_WRONG_TYPE
                    TARGET_TYPE_LONG -> {
                        val d = v.todouble()
                        fromStringPenalty + if (d == d.toLong().toDouble()) 0 else SCORE_WRONG_TYPE
                    }

                    TARGET_TYPE_FLOAT -> {
                        val d = v.todouble()
                        fromStringPenalty + if (d == d.toFloat().toDouble()) 0 else SCORE_WRONG_TYPE
                    }

                    TARGET_TYPE_DOUBLE -> {
                        val d = v.todouble()
                        fromStringPenalty + if (d == d.toLong().toDouble() || d == d.toFloat().toDouble()) 1 else 0
                    }

                    else -> SCORE_WRONG_TYPE
                }
            } else {
                SCORE_UNCOERCIBLE
            }
        }

        override fun coerce(value: LuaValue): Any? {
            return when (targetType) {
                TARGET_TYPE_BYTE -> value.toint().toByte()
                TARGET_TYPE_CHAR -> value.toint().toChar()
                TARGET_TYPE_SHORT -> value.toint().toShort()
                TARGET_TYPE_INT -> value.toint()
                TARGET_TYPE_LONG -> value.todouble().toLong()
                TARGET_TYPE_FLOAT -> value.todouble().toFloat()
                TARGET_TYPE_DOUBLE -> value.todouble()
                else -> null
            }
        }

        companion object {
            const val TARGET_TYPE_BYTE = 0
            const val TARGET_TYPE_CHAR = 1
            const val TARGET_TYPE_SHORT = 2
            const val TARGET_TYPE_INT = 3
            const val TARGET_TYPE_LONG = 4
            const val TARGET_TYPE_FLOAT = 5
            const val TARGET_TYPE_DOUBLE = 6
            val TYPE_NAMES = arrayOf("byte", "char", "short", "int", "long", "float", "double")
        }
    }

    class StringCoercion(val targetType: Int) : Coercion {
        override fun toString(): String {
            return "StringCoercion(" + (if (targetType == TARGET_TYPE_STRING) "String" else "byte[]") + ")"
        }

        override fun score(value: LuaValue): Int {
            return when (value.type()) {
                LuaValue.TSTRING -> if (value.checkstring().isValidUtf8) (if (targetType == TARGET_TYPE_STRING) 0 else 1) else if (targetType == TARGET_TYPE_BYTES) 0 else SCORE_WRONG_TYPE
                LuaValue.TNIL -> SCORE_NULL_VALUE
                else -> if (targetType == TARGET_TYPE_STRING) SCORE_WRONG_TYPE else SCORE_UNCOERCIBLE
            }
        }

        override fun coerce(value: LuaValue): Any? {
            if (value.isnil()) return null
            if (targetType == TARGET_TYPE_STRING) return value.tojstring()
            val s = value.checkstring()
            val b = ByteArray(s.m_length)
            s.copyInto(0, b, 0, b.size)
            return b
        }

        companion object {
            const val TARGET_TYPE_STRING = 0
            const val TARGET_TYPE_BYTES = 1
        }
    }

    class ArrayCoercion(
        private val componentType: KClass<*>,
        private val componentCoercion: Coercion = getCoercion(componentType)
    ) : Coercion {
        override fun toString(): String {
            return "ArrayCoercion(${componentType.qualifiedName})"
        }

        override fun score(value: LuaValue): Int {
            return when (value.type()) {
                LuaValue.TTABLE -> if (value.length() == 0) 0 else componentCoercion.score(value[1])
                LuaValue.TUSERDATA -> inheritanceLevels(
                    componentType,
                    value.touserdata().javaClass.componentType?.kotlin
                )

                LuaValue.TNIL -> SCORE_NULL_VALUE
                else -> SCORE_UNCOERCIBLE
            }
        }

        override fun coerce(value: LuaValue): Any? {
            return when (value.type()) {
                LuaValue.TTABLE -> {
                    val length = value.length()
                    Array.newInstance(componentType.java, length).apply {
                        for (i in 0 until length) {
                            Array.set(this, i, componentCoercion.coerce(value[i + 1]))
                        }
                    }
                }

                LuaValue.TUSERDATA -> value.touserdata()
                LuaValue.TNIL -> null
                else -> null
            }
        }
    }

    class ObjectCoercion(private val targetType: KClass<*>) : Coercion {
        override fun toString(): String {
            return "ObjectCoercion(${targetType.qualifiedName})"
        }

        override fun score(value: LuaValue): Int {
            return when (value.type()) {
                LuaValue.TNUMBER -> inheritanceLevels(
                    targetType,
                    if (value.isint()) Int::class else Double::class
                )

                LuaValue.TBOOLEAN -> inheritanceLevels(targetType, Boolean::class)
                LuaValue.TSTRING -> inheritanceLevels(targetType, String::class)
                LuaValue.TUSERDATA -> inheritanceLevels(targetType, value.touserdata()::class)
                LuaValue.TNIL -> SCORE_NULL_VALUE
                LuaValue.TFUNCTION -> if (targetType.isSAMInterface) 0 else SCORE_UNCOERCIBLE

                else -> inheritanceLevels(targetType, value::class)
            }
        }

        override fun coerce(value: LuaValue): Any? {
            return when (value.type()) {
                LuaValue.TNUMBER -> if (value.isint()) value.toint() else value.todouble()
                LuaValue.TBOOLEAN -> value.toboolean()
                LuaValue.TSTRING -> value.tojstring()
                LuaValue.TUSERDATA -> value.checkuserdata().also {
                    if (!targetType.isInstance(it)) throw LuaError("bad argument: " + targetType.qualifiedName + " expected, got " + value.typename())
                }

                LuaValue.TNIL -> null
                LuaValue.TFUNCTION -> Proxy.newProxyInstance(
                    javaClass.classLoader,
                    arrayOf(targetType.java),
                    ProxyInvocationHandler(value)
                )

                else -> value
            }
        }

        class ProxyInvocationHandler(private val func: LuaValue) : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: kotlin.Array<Any?>?): Any? {
                if (func.isnil()) return null
                val isvarargs = method.modifiers and LuaKotlinLib.METHOD_MODIFIERS_VARARGS != 0
                var n = args?.size ?: 0
                if (args?.last() is Continuation<*>) n--    // create suspend lambda proxy
                val v: kotlin.Array<LuaValue?>
                if (isvarargs) {
                    val o = args?.get(--n)
                    val m = Array.getLength(o)
                    v = arrayOfNulls(n + m)
                    for (i in 0 until n) v[i] = CoerceKotlinToLua.coerce(args?.get(i))
                    for (i in 0 until m) v[i + n] = CoerceKotlinToLua.coerce(Array.get(o, i))
                } else {
                    v = arrayOfNulls(n)
                    for (i in 0 until n) v[i] = CoerceKotlinToLua.coerce(args?.get(i))
                }
                val result = func.invoke(v).arg1()
                return coerce(result, method.returnType)
            }
        }
    }

    object UnsupportedCoercion: Coercion {
        override fun score(value: LuaValue): Int {
            return SCORE_UNCOERCIBLE
        }

        override fun coerce(value: LuaValue): Any? {
            throw IllegalStateException()
        }
    }

    companion object {
        const val SCORE_NULL_VALUE = 0x10
        const val SCORE_WRONG_TYPE = 0x100
        const val SCORE_UNCOERCIBLE = 0x10000

        private val COERCIONS = ConcurrentHashMap<KClass<*>, Coercion>()

        init {
            val boolCoercion: Coercion = BoolCoercion()
            val byteCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_BYTE)
            val charCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_CHAR)
            val shortCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_SHORT)
            val intCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_INT)
            val longCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_LONG)
            val floatCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_FLOAT)
            val doubleCoercion: Coercion =
                NumericCoercion(NumericCoercion.TARGET_TYPE_DOUBLE)
            val stringCoercion: Coercion =
                StringCoercion(StringCoercion.TARGET_TYPE_STRING)
            val bytesCoercion: Coercion =
                StringCoercion(StringCoercion.TARGET_TYPE_BYTES)

            COERCIONS[Boolean::class] = boolCoercion
            COERCIONS[Byte::class] = byteCoercion
            COERCIONS[Char::class] = charCoercion
            COERCIONS[Short::class] = shortCoercion
            COERCIONS[Int::class] = intCoercion
            COERCIONS[Long::class] = longCoercion
            COERCIONS[Float::class] = floatCoercion
            COERCIONS[Double::class] = doubleCoercion
            COERCIONS[String::class] = stringCoercion
            COERCIONS[ByteArray::class] = bytesCoercion
        }

        fun getCoercion(param: KParameter): Coercion {
            return getCoercion(param.type)
        }

        fun getCoercion(param: Class<*>): Coercion {
            return getCoercion(param.kotlin)
        }

        fun getCoercion(param: KType): Coercion {
            val kc = param.kClass
            return COERCIONS[kc] ?: when {
                kc.java.isArray -> ArrayCoercion(
                    componentType = param.arguments[0].type!!.jvmErasure,
                    componentCoercion = getCoercion(param.arguments[0].type!!)
                )

                else -> ObjectCoercion(kc)
            }.also { COERCIONS[kc] = it }
        }

        fun getCoercion(param: KClass<*>): Coercion {
            return COERCIONS[param] ?: when {
                param.java.isArray -> ArrayCoercion(param.java.componentType.kotlin)
                else -> ObjectCoercion(param)
            }.also { COERCIONS[param] = it }
        }

        fun coerce(value: LuaValue, type: KType): Any? {
            return getCoercion(type).coerce(value)
        }

        fun coerce(value: LuaValue, type: Class<*>): Any? {
            return getCoercion(type).coerce(value)
        }

        fun inheritanceLevels(baseclass: KClass<*>, subclass: KClass<*>?): Int {
            if (subclass == null) return SCORE_UNCOERCIBLE
            if (baseclass == subclass) return 0
            var min = SCORE_UNCOERCIBLE
            val ifaces = subclass.superclasses
            for (i in ifaces.indices) min = min.coerceAtMost(inheritanceLevels(baseclass, ifaces[i]) + 1)
            return min
        }

        private val KType.kClass: KClass<*>
            get() {
                try {
                    return jvmErasure
                } catch (_: Throwable) {
                    // throw Error when param is Function
                }
                val e: Throwable?
                try {
                    return when(val jType = javaType) {
                        is Class<*> -> jType.kotlin
                        is ParameterizedType -> (jType.rawType as Class<*>).kotlin
                        else -> throw IllegalStateException("Unknown javaType $jType")
                    }
                } catch (throwable: Throwable) {
                    e = throwable
                }
                throw IllegalArgumentException("Unable to get KClass corresponding to $this", e)
            }

        private val KClass<*>.isSAMInterface: Boolean
            get() = java.isInterface && java.declaredMethods.filter { Modifier.isAbstract(it.modifiers) }.size == 1
    }

    interface Coercion {
        fun score(value: LuaValue): Int
        fun coerce(value: LuaValue): Any?
    }
}