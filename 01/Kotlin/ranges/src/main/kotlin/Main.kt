fun main() {
    val r = 1..4 // 1, 2, 3, 4 or 1.rangeTo(4)
    val r2 = 1..<4 // 1, 2, 3 or 1.rangeUntil(4)
    val r3 = 1..10 step 2 // or .step(2)
    val r4 = 1.0..4.0 // according to IEEE-754
    val r5 = 'a'..'d' // 'a', 'b', 'c', 'd'

    println(r)
    println(r2)
    println(r3)
    println(r4)
    println(r5)
}