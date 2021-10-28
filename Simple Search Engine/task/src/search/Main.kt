package search

typealias Person = List<String>

typealias Persons = List<Person>

fun filterByWord(data: Persons, word: String) =
    data.filter { containsWord(it, word) }

fun containsWord(person: Person, word: String) =
    person.map { it.lowercase() }.any { s -> s.contains(word.lowercase()) }

fun readPersons(n: Int) =
    IO { (1..n).map { readLine().orEmpty().split(" ") } }

fun readPersonsCount() = IO { readLine().orEmpty().toInt() }

fun readRequestsCount() = IO { readLine().orEmpty().toInt() }

fun readRequest() = IO { readLine().orEmpty() }

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

data class IO<T>(val effect: () -> T) : () -> T by effect {
    fun <U> andAfter(f: (T) -> U) {
        return
    }
}

val enterNumberOfPeople = IO { println("Enter the number of people:") }
val enterNumberOfQueries = IO { println("\nEnter the number of queries:") }
val endLine = IO { println() }
val enterDataToSearchPeople = IO { println("Enter data to search people") }
val personsCount = readPersonsCount()
val dataReading = readPersons(personsCount())
val printProcessedData = { data: Persons, request: String ->
    IO { println(processFiltered(filterByWord(data, request))) }
}

// TODO convert sequence of calls to function composition
fun main() {
    enterNumberOfPeople()
    val data: List<List<String>> = dataReading()
    enterNumberOfQueries()
    val n = readRequestsCount()()
    endLine()

    repeat(n) {
        enterDataToSearchPeople()
        val request = readRequest()()
        printProcessedData(data, request)()
        endLine()
    }
}