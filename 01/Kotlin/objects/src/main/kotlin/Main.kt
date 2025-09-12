object C {
    fun m() = println("$this")
}

//object D : C {} // Cannot extend an object.

open class B
interface I
interface J
object D : B(), I, J {}

fun main() {
    C.m()
    println(D is B)
    println(D is I)
    println(D is J)
}
