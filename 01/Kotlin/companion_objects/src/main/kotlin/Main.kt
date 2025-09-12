class C private constructor() {
    object Other { fun m() = println("m()") }
    companion object Factory { fun create(): C = C() }
}

fun main() {
    //C.m() Unresolved reference 'm'.
    C.Other.m()

    //val c = C() // Cannot access 'constructor(): C': it is private in 'C'.
    val c = C.create() // like a static method in Java
}
