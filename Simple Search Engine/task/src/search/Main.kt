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

data class IO<T>(private val effect: IOContext.() -> T) {
    private operator fun invoke() = IOContext().effect()

    infix fun <U> map(f: IOContext.(T) -> U) = IO { IOContext().f(this@IO()) }
    operator fun <U> times(io: IO<U>) = IO { this@IO(); io() }
    fun <U> mapT(f: T.(IOContext) -> U) = IO { this@IO().f(IOContext()) }

    infix fun <U> flatMap(f: IOContext.(T) -> IO<U>) =
        IO { IOContext().f(this@IO())() }

    class IOContext {
        operator fun <T> IO<T>.unaryPlus() = this()
    }

    fun unsafeRun() = this()
}


val stdin = IO { readLine().orEmpty() }

fun readPersons(n: Int): IO<List<List<String>>> =
    IO { (1..n).map { +stdin.mapT { split(" ") } } }

val readPersonsCount: IO<Int> = stdin.mapT { toInt() }
val readRequestsCount: IO<Int> = stdin.mapT { toInt() }
val readRequest = stdin


fun stdout(msg: String = "") = IO { println(msg) }
val endLine = stdout()

val enterNumberOfPeople = stdout("Enter the number of people:")
val enterNumberOfQueries = stdout("\nEnter the number of queries:")
val enterDataToSearchPeople = stdout("Enter data to search people")
val personsCount = readPersonsCount
val dataReading = personsCount.map { +readPersons(it) }

fun printProcessedData(data: Persons, request: String) =
    stdout(processFiltered(filterByWord(data, request)))

fun main() {
    (enterNumberOfPeople * dataReading flatMap { data ->
        enterNumberOfQueries * readRequestsCount flatMap { n ->
            endLine map {
                repeat(n) {
                    +(enterDataToSearchPeople * readRequest flatMap { request ->
                        printProcessedData(data, request) * endLine
                    })
                }
            }
        }
    }).unsafeRun()
}
