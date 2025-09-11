fun main() {
    val ch = 'a'

    val result: Char
    when (ch) {
        'a' -> result = 'A' // break
        'b' -> result = 'B'
        'c' -> result = 'C'
        else -> result = '?' // default
    }
    println(result)

    val result2 = when (ch) {
        'a' -> 'A' // break
        'b' -> 'B'
        'c' -> 'C'
        else -> '?' // default
    }
    println(result2)
}