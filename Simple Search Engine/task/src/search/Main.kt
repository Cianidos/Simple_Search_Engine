package search

typealias Person = List<String>

typealias Persons = List<Person>

fun filterByWord(data: Persons, word: String) =
    data.filter { containsWord(it, word) }

fun containsWord(person: Person, word: String) =
    person.map { it.lowercase() }.any { s -> s.contains(word.lowercase()) }

fun readPersons(n: Int): List<List<String>> =
    (1..n).map { readLine().orEmpty().split(" ") }

fun readPersonsCount(): Int =
    readLine().orEmpty().toInt()

fun readRequestsCount(): Int =
    readLine().orEmpty().toInt()

fun readRequest(): String =
    readLine().orEmpty()

fun format(person: Person): String =
    person.joinToString(" ")

fun processFiltered(filteredData: Persons) = when {
    filteredData.isEmpty() -> "No matching people found."
    else -> filteredData.joinToString(
        "\n",
        prefix = "\nPeople found:\n",
        transform = ::format
    )
}

fun main() {
    println("Enter the number of people:")
    val data = readPersons(readPersonsCount())
    println()
    println("Enter the number of queries:")
    val n = readRequestsCount()
    println()

    repeat(n) {
        println("Enter data to search people")
        println(processFiltered(filterByWord(data, readRequest())))
        println()
    }
}
/*
Dwight Joseph djo@gmail.com
Rene Webb webb@gmail.com
Katie Jacobs
Erick Harrington harrington@gmail.com
Myrtle Medina
Erick Burgess
 */