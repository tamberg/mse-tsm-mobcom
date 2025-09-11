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
    val c = Contact(23, "me@example.com")
    println(c.id)
    println(c.email)
    c.email = "you@example.com"

    val p = Project(c)
    println(p.lead.email)
    println(p.team.size)
    p.team += Contact(42, "dev@example.com")
    p.start()
}