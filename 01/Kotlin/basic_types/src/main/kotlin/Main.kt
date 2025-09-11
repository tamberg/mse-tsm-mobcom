fun main() {
    val i = 3 // inferred type, initialized
    println(i)
    println(i is Int)
    
    val j: Int // explicit type, not initialized
    //println(j) // error: Variable 'j' must be initialized.
    j = 4 // late initialization of value
    println(j)
    
    val k: Int = 5 // explicit type, redundant
    println(k)
}
