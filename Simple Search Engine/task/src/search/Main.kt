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
typealias Persons = List<Person>

fun filterByWord(data: Persons, word: String) =
    data.filter { containsWord(it, word) }

fun containsWord(person: Person, word: String) =
    person.map { it.lowercase() }.any { s -> s.contains(word.lowercase()) }

fun format(person: Person): String =
    person.joinToString(" ")

fun personsToString(p: Persons) =
    p.joinToString("\n", transform = ::format)


fun processFiltered(filteredData: Persons) = when {
    filteredData.isEmpty() -> "No matching people found."
    else -> personsToString(filteredData)
}

fun printProcessedData(data: Persons) = readRequest flatMap { request ->
    stdout(processFiltered(filterByWord(data, request)))
}

val stdin = IO { readLine().orEmpty() }
val readInt = stdin.mapT { toInt() }

fun readPersons(fileName: String): IO<List<List<String>>> =
    IO { File(fileName).readLines().map { it.split(" ") } }

val readRequest = stdout("Enter data to search people") * stdin

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

            MenuOptions.FindPerson
            -> printProcessedData(data) * menuProcess(data)

            MenuOptions.PrintAll -> printPeopleHeader *
                    stdout(personsToString(data)) * menuProcess(data)
        }
    }

fun main(args: Array<String>) {
    (readPersons(args[1]) flatMap { data ->
        menuProcess(data)
    }).unsafeRun()
}

/*
2
a b c
b t g
 */