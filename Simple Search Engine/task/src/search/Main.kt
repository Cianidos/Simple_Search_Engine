package search

fun main(args: Array<String>) {
    (readPersons(args[1]) flatMap { data ->
        menuProcess(data)
    }).unsafeRun()
}
