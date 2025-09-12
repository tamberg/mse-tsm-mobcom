class Hat<T>() {
    var item: T? = null
}

class Bunny()
class Dove()

fun main() {
    val h = Hat<Bunny>()
    //h.item = Dove() // Assignment type mismatch: actual type is 'Dove', but 'Bunny?' was expected.
    h.item = Bunny()
    val b = h.item
    //println(b is T) // Unresolved reference 'T'.
    println(b is Bunny)
}
