fun main() {
    val i = 3 // read-only
    //i = 2 // 'val' cannot be reassigned.
    println(i)

    var x = 5 // mutable
    println(x)
    x = 7
    println(x)
}
