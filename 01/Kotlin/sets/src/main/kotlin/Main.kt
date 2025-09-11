fun main() {
    val animalList = listOf("cow", "cow", "horse") 
    println(animalList)
    
    val speciesSet = animalList.toSet()
    println(speciesSet)
    
    val mutableSet = speciesSet.toMutableSet()
    mutableSet.remove("cow")
    mutableSet.add("cattle")
    println(mutableSet)
}