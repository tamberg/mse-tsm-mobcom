class Contact (val id: Int, var email: String)

class Project (val lead: Contact) {
    val team = mutableListOf(lead)
    fun start() {
        for (person in team) {
            println("Go ${person.email}!")
        }
    }
}

fun main() {
    TODO("Create instances")
}