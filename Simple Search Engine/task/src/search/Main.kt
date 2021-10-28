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

data class IO<T> private constructor(private val effect: () -> T) {
    companion object {
        operator fun <T> invoke(effect: IOContext.() -> T) =
            IO { IOContext().effect() }

        operator fun <T> invoke(io: IO<T>) = io
    }

    private operator fun invoke() = effect()

    infix fun <U> map(f: IOContext.(T) -> U) = IO { IOContext().f(this()) }
    infix fun <U> and(io: IO<U>) = IO { this(); io() }
    fun <U> mapT(f: T.(IOContext) -> U) = IO { this().f(IOContext()) }

    infix fun <U> flatMap(f: IOContext.(T) -> IO<U>) =
        IO { IOContext().f(this())() }

    class IOContext {
        fun <U> use(io: IO<U>): U = io()
    }

    fun unsafeRun() = effect()
}


val stdin = IO { readLine().orEmpty() }

fun readPersons(n: Int): IO<List<List<String>>> =
    IO { (1..n).map { use(stdin.mapT { split(" ") }) } }

val readPersonsCount: IO<Int> = stdin.mapT { toInt() }
val readRequestsCount: IO<Int> = stdin.mapT { toInt() }
val readRequest = stdin


fun stdout(msg: String = "") = IO { println(msg) }

val enterNumberOfPeople = stdout("Enter the number of people:")
val enterNumberOfQueries = stdout("\nEnter the number of queries:")
val endLine = stdout()
val enterDataToSearchPeople = stdout("Enter data to search people")
val personsCount = readPersonsCount
val dataReading = personsCount.map { use(readPersons(it)) }

fun printProcessedData(data: Persons, request: String) =
    stdout(processFiltered(filterByWord(data, request)))

fun main() {
    (enterNumberOfPeople and dataReading flatMap { data ->
        enterNumberOfQueries and readRequestsCount flatMap { n ->
            endLine map {
                repeat(n) {
                    use(enterDataToSearchPeople and readRequest.flatMap { request ->
                        printProcessedData(data, request) and endLine
                    })
                }
            }
        }
    }).unsafeRun()
}
