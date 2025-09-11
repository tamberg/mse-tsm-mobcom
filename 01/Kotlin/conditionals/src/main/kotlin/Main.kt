fun main(args: Array<String>) {
    val i = args[0].toInt()

    if (i > 3) {
        println("big")
    } else {
        println("small")
    }

    val result = if (i > 3) "big" else "small"
    println(result)
}