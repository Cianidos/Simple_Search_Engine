package search

typealias Person = List<String>

typealias Persons = List<Person>

fun filterByWord(data: Persons, word: String) =
    data.filter { containsWord(it, word) }

fun containsWord(person: Person, word: String) =
    person.map { it.lowercase() }.any { s -> s.contains(word.lowercase()) }

fun readPersons(n: Int) =
    (1..n).map { readLine().orEmpty().split(" ") }

fun readPersonsCount() =
    readLine().orEmpty().toInt()

fun readRequestsCount() =
    readLine().orEmpty().toInt()

fun readRequest(): String {
    println("Enter data to search people")
    return readLine().orEmpty()
}

fun println(person: Person) {
    println(person.joinToString(" "))
}

fun processFiltered(filteredData: Persons) {
    when {
        filteredData.isEmpty() -> {
            println("No matching people found.")
        }
        else -> {
            println()
            println("People found:")
            filteredData.forEach(::println)
        }
    }
}

fun main() {
    println("Enter the number of people:")
    val data = readPersons(readPersonsCount())
    println()
    println("Enter the number of queries:")
    val n = readRequestsCount()
    println()

    repeat(n) {
        processFiltered(filterByWord(data, readRequest()))
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