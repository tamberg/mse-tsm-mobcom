abstract class B { // not instantiated
    abstract fun m() // no implementation
}

class C: B() {
    //fun m() = println("m()") // 'm' hides member of supertype 'B' and needs an 'override' modifier.
    override fun m() = println("m()")
}

fun main() {
    //val b = B() // Cannot create an instance of an abstract class.
    val c = C()
    c.m()
}