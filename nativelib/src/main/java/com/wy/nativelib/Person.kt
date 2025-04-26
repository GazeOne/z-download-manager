package com.wy.nativelib

class Person @JvmOverloads constructor(val age: Int = 10, val name: String = "bob") {

    override fun toString(): String {
        return "age = $age, name = $name"
    }
}