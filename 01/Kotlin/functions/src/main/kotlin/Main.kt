fun sum(x: Int, y: Int): Int {
    return x + y
}

fun log(x: Double, b: Double = 2.0): Double {
    return kotlin.math.log(x, b)
}

fun main() {
    val i = sum(3, 5)
    val d = log(x = 100.0, b = 10.0) // named args
    val e = log(128.0) // default argument b = 2.0
    println(i)
    println(d)
    println(e)
}