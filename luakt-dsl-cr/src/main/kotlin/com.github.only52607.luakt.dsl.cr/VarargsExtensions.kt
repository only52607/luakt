@file:Suppress("UNUSED")
package com.github.only52607.luakt.dsl.cr

import com.github.only52607.luakt.ValueMapper

context (ValueMapper)
fun varargsOf(vararg values: Any): org.luaj.vm2.Varargs = org.luaj.vm2.LuaValue.varargsOf(values.map { it.asLuaValue() }.toTypedArray())

context (ValueMapper)
fun Array<Any>.packVarargs() = varargsOf(*this)

context (ValueMapper)
fun List<Any>.packVarargs() = varargsOf(*this.toTypedArray())
