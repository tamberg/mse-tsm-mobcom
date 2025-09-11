fun main() {
    //var s: String = null // Null cannot be a value of a non-null type 'String'.
    var s2: String? = null
    //s2 = "hello"
    //val m = s2.length // Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'String?'.
    val n = s2?.length
    println(n)
}