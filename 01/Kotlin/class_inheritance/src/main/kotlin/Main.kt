//class B // This type is final, so it cannot be extended.
open class B

class C: B()

fun main() {
    val b = B()
    val c = C()
    println(b is B)
    println(b is Any)
    println(c is C)
    println(c is B)
    println(c is Any)
}