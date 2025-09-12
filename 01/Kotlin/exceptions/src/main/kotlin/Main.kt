class MyException : Exception()

fun f(i: Int) {
    if (i == 0) throw MyException()
    println("f($i)")
}

fun g(i: Int) {
    require(i != 0)
    println("f($i)")
}

fun main() {
    try {
        //f(0)
        f(1)
        //g(0)
        println("done")
    } catch (e: MyException) {
        println("oops, $e")
    //} catch (e: IllegalArgumentException) {
    //    println("oops, $e")
    } finally {
        println("anyway")
    }
}
