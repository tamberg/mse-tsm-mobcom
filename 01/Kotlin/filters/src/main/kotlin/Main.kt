fun main() {
    val nums = 1..10 // or array, collection, etc.
    val isEven = { i: Int -> i % 2 == 0 }
    val evens = nums.filter(isEven)
    val odds = nums.filter({ i -> i % 2 == 1 })
    //val odds = nums.filter { i -> i % 2 == 1 } // trailing lambda, no ()
    println(odds)
    println(evens)
}