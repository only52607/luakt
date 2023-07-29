# LuaKt [![](https://www.jitpack.io/v/only52607/luakt.svg)](https://www.jitpack.io/#only52607/luakt)

Based on [luaj](https://github.com/luaj/luaj), with enhanced interoperability with kotlin.


## Get Started

> Assuming you are already familiar with the basic API of [luaj](https://github.com/luaj/luaj).

1. Add the JitPack repository to your build file
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add dependency with luaj

```gradle
dependencies {
    implementation 'com.github.only52607.luakt:luakt:$luakt_version'
}
```

### or without luaj
```gradle
dependencies {
    implementation 'com.github.only52607.luakt:luakt-core:$luakt_version'
    implementation 'com.github.only52607.luakt:luakt-extension:$luakt_version'
}
```

> `luakt` contains the following three modules \
> `luakt-core`: The core module of luakt, including features such as type coercion. \
> `luakt-extension`: The extension module of luakt provides some useful utility functions. \
> `luakt-luaj`: The luaj library, if your project already includes this library, you don’t need to add it repeatedly.

### Example

main.kt
```kotlin
val globals = JsePlatform.standardGlobals()
globals.load(LuaKotlinLib())
globals.loadfile("main.lua").invoke()
```

main.lua
```lua
-- launch a thread
Thread = luakotlin.bindClass("java.lang.Thread")
Thread(function()
    print("print from new thread: " .. tostring(Thread:currentThread()))
end):start()
```

## LuaKotlinLib

If you are going to interact with kotlin in `luaj`, you can use `Luajava` library, but `LuaKotlin` library is a better choice, it provides similar api to `Luajava` library, but it interacts with kotlin much simpler.

### Examples

- bind class
```lua
Thread = luakotlin.bindClass("java.lang.Thread")
```

- new instance
```lua
-- bindClass and new
Random = luakotlin.bindClass("java.util.Random")
local r = luakotlin.new(Random, 10) -- use 10 as rand seed

-- newInstance
local r = luakotlin.newInstance("java.util.Random", 10)

-- with invoke sugar
Random = luakotlin.bindClass("java.util.Random")
local r = Random(10)
```

- create proxy
```lua
Thread = luakotlin.bindClass("java.lang.Thread")
local runnable = luakotlin.createProxy("java.lang.Runnable", {
    run = function ()
        print("print from new thread: " .. tostring(Thread:currentThread()))
    end
})
Thread(runnable):start()
```

If a variable is a previously bound interface class, it can be called directly, which will return a proxy.
```lua
Thread = luakotlin.bindClass("java.lang.Thread")
Runnable = luakotlin.bindClass("java.lang.Runnable")
local runnable = Runnable {
    run = function ()
        print("print from new thread: " .. tostring(Thread:currentThread()))
    end
}
Thread(runnable):start()
```

- Single abstract method(SAM) interface coercion

If the interface is a SAM interface, then the proxy can be created like below without specifying the method name.

```lua
Thread = luakotlin.bindClass("java.lang.Thread")
Runnable = luakotlin.bindClass("java.lang.Runnable")
-- use function to create proxy ofsingle method interface
local runnable = Runnable(function ()
    print("print from new thread: " .. tostring(Thread:currentThread()))
end)
Thread(runnable):start()
```

If the type of a parameter is a SAM interface, you can pass in a Lua function as follows, and the proxy will be created automatically.
```lua
Thread = luakotlin.bindClass("java.lang.Thread")
Thread(function ()
    print("print from new thread: " .. tostring(Thread:currentThread()))
end):start()
```

- getter and setter


```kotlin
class Cat {
    var color: String = "white"
        set(value) {
            field = value
            println("color changed to $value")
        }
}
```

```lua
Cat = luakotlin.bindClass("Cat")
local c = Cat()
print(c.color) -- white
c.color = "pink" -- print "color changed to pink"
print(c.colot) -- pink
```

- call suspend function blocking
```kotlin
class Cat {
    suspend fun sleep(duration: Long) {
        println("start sleep")
        delay(duration)
        println("wake up")
    }
}
```

```lua
Cat = luakotlin.bindClass("Cat")
local c = Cat()
c.sleep(1000) -- will block here
```

- Interact with singleton objects
```kotlin
object TomCat {
    fun run() {
        prinln("TomCat is runnning")
    }
}
```

```lua
TomCat = luakotlin.bindClass("TomCat")
TomCat:run()
```


- Interact with companion object
```kotlin
class Cat private constructor(val name: String) {
    companion object {
        fun create(name: String) {
            println("Cat $name is created")
            return Cat(name)
        }
    }
}
```

```lua
Cat = luakotlin.bindClass("Cat")
Cat:create("tom")
```

## LuaKotlinExLib

`LuaKotlinExlib` provides more utility functions based on the `LuaKotlinLib`.

### Load lib

```kotlin
globals.load(LuaKotlinlib()) // Make sure the LuaKotlinlib is preloaded
globals.load(LuaKotlinExlib())
```

### Examples

- import class
```lua
import "java.util.Random" -- Shorthand for `Random = luakotlin.bindClass("java.util.Random")`
Random(10).nextInt()

-- with alias
import("java.util.Random", MyRandom) -- Equivalent to `MyRandom = luakotlin.bindClass("java.util.Random")`
MyRandom(10).nextInt()
```

- convert kotlin Collection or Map as lua table

The `Array` will be automatically converted to a lua table, but the Collection interface and Map interface will not be automatically converted. If you want to convert, you can use the `totable` function, which receives a userdata that implements the `Collection` or `Map` interface. And return the corresponding table.

```kotlin
object C {
    fun getMap(): Map<String, String> = mapOf(Pair("k1", "v1"))
    fun getList(): List<String> = listOf("v1", "v2")
}
```

```lua
local map = C:getMap()
for k, v in pairs(totable(map)) do
    print(k, v) -- k1 v1
end
local map = C:getList()
for i, v in ipairs(totable(map)) do
    print(i, v)
    -- v1
    -- v2
end 
```

- convert lua table as kotlin Collection or Map

```lua
tolist({"v1", "v2"}) -- listOf("v1", "v2")
toset({"v1", "v2"}) -- setOf("v1", "v2")
tomap({ k1 = "v1", k2 = "v2" }) -- mapOf(Pair("k1", "v1"), Pair("k2", "v2"))
```

## Extension functions

`luakt` also provides some useful extension functions in kotlin

### Examples

- create LuaFunction
```kotlin
val add = VarArgFunction { varargs ->
    return LuaValue.valueOf(varargs.checkint(1) + varargs.checkint(2))
}
```

- deconstruct varargs
```kotlin
val v = LuaValue.varargsOf(LuaValue.valueOf(1), LuaValue.valueOf(2))
val (a, b) = v // deconstruct
print(a, b) // 1 2
```

## License
[MIT License](https://github.com/only52607/luakt/blob/master/LICENSE)