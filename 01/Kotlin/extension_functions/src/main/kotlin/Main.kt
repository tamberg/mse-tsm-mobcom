fun String.len(): Int { return this.length }
// or fun String.len(): Int = this.length // no {} brackets
// or fun String.len() = this.length // implicit return type

fun main() {
    val s = "hello" // type String, not our class
    val n = s.len() // extended, our new function
    print(n)
}