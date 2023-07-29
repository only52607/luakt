@file:Suppress("unused")
package luakt

import kotlinx.coroutines.delay
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

interface Food

abstract class Animal {
    abstract val name: String
    open fun eat(food: Food) {
        println("$name eat $food")
    }

    fun eat() {
        println("$name eat nothing")
    }

    fun eat(foods: Array<Food>) {
        println("$name eat mupltiple food: ${foods.joinToString(", ")}")
    }

    fun jump() {
        println("$name is jump")
    }

    suspend fun sleep(duration: Long) {
        println("$name is sleeping now")
        delay(duration)
        println("$name wake up")
    }
}

open class Fish(override val name: String) : Animal(), Food {
    constructor(): this("Delicious Fish")

    override fun toString(): String {
        return "Fish($name)"
    }
}

class Apple: Food {
    override fun toString(): String {
        return "Apple"
    }
}

open class Cat(
    override val name: String
): Animal() {
    var color: String = "white"
        set(value) {
            field = value
            println("$name's color changed to $value")
        }

    fun eat(fish: Fish) {
        println("$name eat its favorite food: $fish")
    }

    fun meow() {
        println("$name is meowing")
    }

    fun likes(): Array<String> {
        return arrayOf("fish", "apple")
    }

    fun transformColor(transformer: (String) -> String) {
        color = transformer(color)
    }

    suspend fun transformColorSuspended(transformer: suspend (String) -> String) {
        color = transformer(color)
    }

    override fun toString(): String {
        return "Cat($name)"
    }
}

object TomCat: Cat("tom") {
    fun run() {
        println("Tom is running")
    }
}

class Dog private constructor(override val name: String): Animal() {
    companion object {
        fun create(name: String): Dog {
            println("Dog $name created")
            return Dog(name)
        }
    }
}

class TestLoadLib {
    companion object {
        @JvmStatic
        fun load(): LuaTable {
            return LuaValue.tableOf(arrayOf(
                LuaValue.valueOf("hello"),
                LuaValue.valueOf("world")
            ))
        }
    }
}