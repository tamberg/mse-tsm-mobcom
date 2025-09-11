interface I { // not instantiated, no c.tor
    fun m() // no impl., abstract by default
} // only abstract members/functions

interface J {
    fun n()
}

class C() : I, J {
    override fun m() = println("m()")
    override fun n() = println("n()")
}

fun main() {
    val c = C()
    c.m()
    c.n()
    val i: I = c
    i.m()
    //i.n() // Unresolved reference 'n'.
    val j: J = c
    //j.m() // Unresolved reference 'm'.
    j.n()
}