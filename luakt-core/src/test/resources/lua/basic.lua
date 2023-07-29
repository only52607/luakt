-- bind class
Cat = luakotlin.bindClass("luakt.Cat")
Fish = luakotlin.bindClass("luakt.Fish")
print(Cat, Fish)

-- new instance
local kitty = luakotlin.new(Cat, "Kitty")
local fish = luakotlin.new(Fish)
local apple = luakotlin.newInstance("luakt.Apple")
print("created instance", kitty, fish, apple)

-- getter and setter
print("kitty's original color is " .. kitty.color)
kitty.color = "pink"
print("kitty's new color is " .. kitty.color)
assert(kitty.color == "pink")

-- call method
kitty:meow()
for k, v in pairs(kitty:likes()) do
    print("kitty like", k, v)
end

-- call method from super class
kitty:jump()

-- call overload method
kitty:eat()
kitty:eat(apple)
kitty:eat(fish)
kitty:eat({apple})

-- create proxy
Thread = luakotlin.bindClass("java.lang.Thread")
luakotlin.new(Thread, luakotlin.createProxy("java.lang.Runnable", {
    run = function ()
        print("print from new thread: " .. tostring(Thread:currentThread()))
    end
})):start()

-- sugar for new instance and createProxy
Runnable = luakotlin.bindClass("java.lang.Runnable")
Thread(luakotlin.createProxy("java.lang.Runnable", {
    run = function ()
        print("print from new thread with sugar1: " .. tostring(Thread:currentThread()))
    end
})):start()
Thread(luakotlin.createProxy("java.lang.Runnable", function ()
    print("print from new thread with sugar2: " .. tostring(Thread:currentThread()))
end)):start()
Thread(Runnable(function ()
    print("print from new thread with sugar3: " .. tostring(Thread:currentThread()))
end)):start()
Thread(Runnable {
    run = function ()
        print("print from new thread with sugar4: " .. tostring(Thread:currentThread()))
    end
}):start()
Thread(function ()
    print("print from new thread with sugar5: " .. tostring(Thread:currentThread()))
end):start()

-- suspend call
kitty:sleep(1000)

-- load lib
local lib = luakotlin.loadLib("luakt.TestLoadLib", "load")
for k, v in pairs(lib) do
    print("lib: ", k, v)
end

-- lambda coercion
kitty:transformColor(function(oldColor)
    print("old color is " .. oldColor)
    return "darker " .. oldColor
end)

-- suspended lambda coercion
kitty:transformColorSuspended(function(oldColor)
    print("old color is " .. oldColor)
    return "lighter " .. oldColor
end)

-- call object method
TomCat = luakotlin.bindClass("luakt.TomCat")
TomCat:run()

-- call companion object method
Dog = luakotlin.bindClass("luakt.Dog")
print(Dog:create("d"))