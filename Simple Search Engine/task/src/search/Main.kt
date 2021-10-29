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

typealias Person = List<String>

data class Persons(val persons: List<Person>) : List<Person> by persons {
    val index = persons.withIndex().flatMap { row ->
        row.value.map { it to row.index }
    }.groupBy({ it.first.lowercase() }, { it.second })
}

fun findAny(data: Persons, words: List<String>): Persons =
    Persons(words.flatMap { word ->
        data.index[word] ?: emptyList()
    }.toSet().map { id -> data[id] })

fun findAll(data: Persons, words: List<String>): Persons =
    Persons(words.flatMap { word ->
        data.index[word] ?: emptyList()
    }.groupBy { it }.flatMap {
        if (it.value.size != words.size) emptyList()
        else listOf(it.key)
    }.map { id ->
        data[id]
    })

fun findNone(data: Persons, words: List<String>): Persons {
    val badIds = words.flatMap { word ->
        data.index[word] ?: emptyList()
    }.toSet()
    return Persons(data.filterIndexed { idx, _ -> !badIds.contains(idx) })
}

fun format(person: Person): String =
    person.joinToString(" ")

fun personsToString(p: Persons) =
    p.joinToString("\n", transform = ::format)

fun printFiltered(filteredData: Persons) = stdout(
    when {
        filteredData.isEmpty() -> "No matching people found."
        else -> personsToString(filteredData)
    }
)

fun printProcessedData(data: Persons, searchStrategy: SearchStrategy) =
    readRequest map { it.lowercase().split(" ") } flatMap { request ->
        printFiltered(
            when (searchStrategy) {
                SearchStrategy.All -> ::findAll
                SearchStrategy.Any -> ::findAny
                SearchStrategy.None -> ::findNone
            }(data, request)
        )
    }

val stdin = IO { readLine().orEmpty() }
val readInt = stdin.mapT { toInt() }

fun readPersons(fileName: String): IO<Persons> =
    IO { Persons(File(fileName).readLines().map { it.split(" ") }) }

val readRequest = stdout("Enter data to search people") * stdin
val readStrategy = stdout("Select a matching strategy: ALL, ANY, NONE") *
        stdin map { SearchStrategy(it) }

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

sealed class SearchStrategy {
    companion object {
        operator fun invoke(str: String) = when (str) {
            "ANY" -> Any
            "ALL" -> All
            "NONE" -> None
            else -> throw IllegalArgumentException("Impossible")
        }
    }

    object Any : SearchStrategy()
    object All : SearchStrategy()
    object None : SearchStrategy()
}

sealed class MenuOptions {
    companion object {
        operator fun invoke(code: Int) = when (code) {
            0 -> Exit
            1 -> FindPerson
            2 -> PrintAll
            else -> Error
        }
    }

    object Exit : MenuOptions()
    object FindPerson : MenuOptions()
    object PrintAll : MenuOptions()
    object Error : MenuOptions()
}

fun menuProcess(data: Persons): IO<Unit> =
    endL * printMenu * readInt flatMap { code ->
        endL * when (MenuOptions(code)) {
            MenuOptions.Error -> printIncorrectOption * menuProcess(data)
            MenuOptions.Exit -> printExit

            MenuOptions.FindPerson -> (readStrategy flatMap
                    { printProcessedData(data, it) }) * menuProcess(data)

            MenuOptions.PrintAll -> printPeopleHeader *
                    stdout(personsToString(data)) * menuProcess(data)
        }
    }

fun main(args: Array<String>) {
    (readPersons(args[1]) flatMap { data ->
        menuProcess(data)
    }).unsafeRun()
}
