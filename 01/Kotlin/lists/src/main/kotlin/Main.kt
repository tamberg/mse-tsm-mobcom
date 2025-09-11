fun main() {
    val list = listOf("hello", "hola", "hi") 
    val item = list[0] // or .get(0)
    println(list)
    println(item)
 
    val mutableList = list.toMutableList()
    println(mutableList)
    mutableList[0] = "ciao" // or .set(0, ...)
    println(mutableList)
}