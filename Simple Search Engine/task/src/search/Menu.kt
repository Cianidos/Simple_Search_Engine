package search

import java.io.File

enum class MenuOptions(val io: (Persons) -> IO<Unit>) {
    Exit({ printExit }),
    FindPerson({ data ->
        (readStrategy flatMap { endL * printProcessedData(data, it) }) *
                menuProcess(data)
    }),
    PrintAll({ printPeopleHeader * stdout("$it") * menuProcess(it) }),
    Error({ printIncorrectOption * menuProcess(it) }),
}

fun parseMenuOption(code: Int) = when (code) {
    0 -> MenuOptions.Exit
    1 -> MenuOptions.FindPerson
    2 -> MenuOptions.PrintAll
    else -> MenuOptions.Error
}

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
    stdout(
        if (filteredData.isEmpty()) "No matching people found." else
            "${filteredData.size} persons found:\n$filteredData"
    )

fun printProcessedData(data: Persons, searchStrategy: SearchStrategy) =
    readRequest map { it.lowercase().split(" ") } flatMap { request ->
        endL * printFiltered(searchStrategy.find(data, request))
    }

fun readPersons(fileName: String): IO<Persons> =
    IO { Persons(File(fileName).readLines().map { Person(it.split(" ")) }) }

val readRequest = stdout("Enter data to search people") * stdin
val readStrategy = stdout("Select a matching strategy: ALL, ANY, NONE") *
        stdin map { parseSearchStrategy(it) }

fun menuProcess(data: Persons): IO<Unit> =
    endL * printMenu * readInt flatMap { code ->
        endL * parseMenuOption(code).io(data)
    }
