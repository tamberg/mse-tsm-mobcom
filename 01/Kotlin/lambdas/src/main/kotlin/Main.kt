fun len(s: String): Int { return s.length }
val len2 = { s: String -> s.length } // lambda

fun main() {
    println(len("hello"))
    println(len2("hello"))
}