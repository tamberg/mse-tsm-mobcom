val i = 1 // public, visible from everywhere
internal val j = 2 // visible from inside same module
private val k = 3 // visible from inside same .kt file

fun f() {} // public, visible from everywhere
internal fun g() {} // visible from inside same module
private fun h() {} // visible from inside same .kt file

class C () { // public, visible from everywhere
    val x = 1 // public, visible from everywhere
    internal val y = 2 // visible from inside same module
    private val z = 4 // visible from inside same class

    fun m() { println(z) } // e.g. in a member function
    internal fun n() {} // visible from inside same module
    private fun p() {} // visible from inside same class
}
internal class D() // visible from inside same module
private class E() // visible from inside same .kt file

fun main() {
    println(i)
    println(j)
    println(k)

    f()
    g()
    h()

    val c = C()
    val d = D()
    val e = E()
    //val x = c.x // Cannot access 'val x: Int': it is private in '/C'
    c.m()
    c.n()
    //c.p() // Cannot access 'fun p(): Unit': it is private in '/C'
}