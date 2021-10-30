package search

import java.io.File

data class IO<T>(private val effect: IOContext.() -> T) {
    private operator fun invoke() = IOContext().effect()
    infix fun <U> map(f: IOContext.(T) -> U) = IO { IOContext().f(this@IO()) }
    fun <U> mapT(f: T.(IOContext) -> U) = IO { this@IO().f(IOContext()) }
    infix fun <U> flatMap(f: IOContext.(T) -> IO<U>) =
        IO { IOContext().f(this@IO())() }

    operator fun <U> times(io: IO<U>) = IO { this@IO(); io() }
    class IOContext {
        operator fun <T> IO<T>.unaryPlus() = this()
    }

    fun unsafeRun() = this()
}

data class Person(val data: List<String>) : List<String> by data {
    override fun toString(): String =
        this.joinToString(" ")
}

data class Persons(val persons: List<Person>) : List<Person> by persons {
    val index: Map<String, Set<Int>> = persons
        .flatMapIndexed { idx, value -> value.map { it to idx } }
        .groupBy({ it.first.lowercase() }, { it.second })
        .mapValues { it.value.toSet() }

    override fun toString(): String =
        persons.joinToString("\n")
}

fun findAny(data: Persons, words: List<String>): Persons =
    Persons(words.fold(emptySet<Int>()) { res, word ->
        res.union(data.index[word].orEmpty())
    }.map { data[it] })

fun findAll(data: Persons, words: List<String>): Persons =
    Persons(words.fold(emptySet<Int>()) { res, word ->
        res.intersect(data.index[word].orEmpty())
    }.map { data[it] })

fun findNone(data: Persons, words: List<String>): Persons =
    Persons(words.fold(data.indices.toSet()) { res, word ->
        res.subtract(data.index[word].orEmpty())
    }.map { data[it] })


enum class SearchStrategy(val find: (Persons, List<String>) -> Persons) {
    Any(::findAny), All(::findAll), None(::findNone)
}

fun parseSearchStrategy(str: String) = when (str) {
    "ANY" -> SearchStrategy.Any
    "ALL" -> SearchStrategy.All
    "NONE" -> SearchStrategy.None
    else -> throw IllegalArgumentException("Impossible")
}

fun parseMenuOption(code: Int) = when (code) {
    0 -> MenuOptions.Exit
    1 -> MenuOptions.FindPerson
    2 -> MenuOptions.PrintAll
    else -> MenuOptions.Error
}

val stdin = IO { readLine().orEmpty() }
val readInt = stdin.mapT { toInt() }
fun readPersons(fileName: String): IO<Persons> =
    IO { Persons(File(fileName).readLines().map { Person(it.split(" ")) }) }

val readRequest = stdout("Enter data to search people") * stdin
val readStrategy = stdout("Select a matching strategy: ALL, ANY, NONE") *
        stdin map { parseSearchStrategy(it) }

fun stdout(msg: String = "") = IO { println(msg) }
val endL = stdout()
val printMenu = stdout(
    """
        === Menu ===
        1. Search information.
        2. Print all data.
        0. Exit.
        """.trimIndent()
)
val printIncorrectOption = stdout("Incorrect option! Try again")
val printPeopleHeader = stdout("=== List of people ===")
val printExit = stdout("Bye!")
fun printFiltered(filteredData: Persons) =
    stdout(if (filteredData.isEmpty()) "No matching people found." else "$filteredData")

fun printProcessedData(data: Persons, searchStrategy: SearchStrategy) =
    readRequest map { it.lowercase().split(" ") } flatMap { request ->
        printFiltered(searchStrategy.find(data, request))
    }

enum class MenuOptions(val io: (Persons) -> IO<Unit>) {
    Exit({ printExit }),
    FindPerson({ data ->
        (readStrategy flatMap { printProcessedData(data, it) }) *
                menuProcess(data)
    }),
    PrintAll({ printPeopleHeader * stdout("$it") * menuProcess(it) }),
    Error({ printIncorrectOption * menuProcess(it) }),
}

fun menuProcess(data: Persons): IO<Unit> =
    endL * printMenu * readInt flatMap { code ->
        endL * parseMenuOption(code).io(data)
    }

fun main(args: Array<String>) {
    (readPersons(args[1]) flatMap { data ->
        menuProcess(data)
    }).unsafeRun()
}
