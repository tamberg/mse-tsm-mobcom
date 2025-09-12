class User(val friends: List<User>)

fun calculateTotalStringLength(items: List<Any>): Int {
    var totalLength = 0
    for (item in items) {
        if (item is String) totalLength += item.length
    }
    return totalLength
}

fun calculateTotalStringLength2(items: List<Any>): Int {
    return items.sumOf { (it as? String)?.length ?: 0 }
}

fun getNumberOfFriends(users: Map<Int, User>, userId: Int): Int {
    return users[userId]?.friends?.size ?: -1
}

fun main() {
    TODO() 
}
