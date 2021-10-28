package search

typealias Person = List<String>

typealias Persons = List<Person>

fun filterByWord(data: Persons, word: String) =
    data.filter { containsWord(it, word) }

fun containsWord(person: Person, word: String) =
    person.map { it.lowercase() }.any { s -> s.contains(word.lowercase()) }


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

data class IO<T>(val effect: () -> T) : () -> T by effect

fun stdin() = IO { readLine().orEmpty() }

fun readPersons(n: Int): IO<List<List<String>>> =
    IO { (1..n).map { stdin()().split(" ") } }
fun readPersonsCount(): IO<Int> = IO { stdin()().toInt() }
fun readRequestsCount(): IO<Int> = IO { stdin()().toInt() }
fun readRequest() = stdin()


fun stdout(msg: String = "") = IO { println(msg) }

val enterNumberOfPeople = stdout("Enter the number of people:")
val enterNumberOfQueries = stdout("\nEnter the number of queries:")
val endLine = stdout()
val enterDataToSearchPeople = stdout("Enter data to search people")
val personsCount = readPersonsCount()
val dataReading = readPersons(personsCount())
val printProcessedData = { data: Persons, request: String ->
    stdout(processFiltered(filterByWord(data, request)))
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