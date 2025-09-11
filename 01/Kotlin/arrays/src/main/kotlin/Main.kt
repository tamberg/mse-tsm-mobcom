fun main() {
    val pets = arrayOf("cat", "dog", "snake")
    println(pets.joinToString())
    
    val legs: Array<Int?> = arrayOfNulls(3)
    //val legs = IntArray(3) // Java: new int[3]
    legs[0] = 4;
    println(legs.joinToString())
}
