package com.github.only52607.luakt.annotation

/**
 * ClassName: LuaFunctionAnnotations
 * Description:
 * date: 2022/1/15 21:44
 * @author ooooonly
 * @version
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class RawVarargFunction

/**
 * 不将返回值转换
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RawReturn



