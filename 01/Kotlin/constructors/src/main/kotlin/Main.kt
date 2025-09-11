class A() // public class, public constructor

class B private constructor() // non-public constructor

class C(val i: Int, val j: Int) { // primary constructor
    constructor(i: Int): this(i, 0) // secondary constructor
}

fun main() {
    val a = A()
    //val b = B() // Cannot access 'constructor(): B': it is private in 'B'.
    val c = C(1, 2)
    val c2 = C(1)
    println(a)
    println(c)
    println(c2)
}