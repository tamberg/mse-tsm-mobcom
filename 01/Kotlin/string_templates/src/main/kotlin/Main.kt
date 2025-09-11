fun main() {
    val what = "hi"
    val whom = "folks"
    print(what)
    print(", ")
    print(whom)
    println('!')
   
    val text = "$what, $whom!"
    println(text)
    
    val stat = "(${text.length} chars)"
    println(stat)

    val copy = what + ", " + whom + "!"
    println(copy)
}