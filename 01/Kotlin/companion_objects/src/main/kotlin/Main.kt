class E private constructor() {
    object Other { fun m() = println("m()") }
    companion object Factory { fun create(): E = E() }
}

fun main() {
    //E.m() Unresolved reference 'm'.
    E.Other.m()

    //val e = E() // Cannot access 'constructor(): E': it is private in 'E'.
    val e = E.create() // like a static method in Java
}
