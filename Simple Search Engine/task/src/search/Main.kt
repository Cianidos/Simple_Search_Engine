package search

data class IO<A>(private val a: () -> A) {
    fun unsafeRun(): A = a()

    fun <B> withoutIO(f: (A) -> B): IO<B> =
        IO { f(this.unsafeRun()) }

    fun <B> continueIO(f: (A) -> IO<B>): IO<B> =
        IO { f(this.unsafeRun()).unsafeRun() }
}

fun stdin(): IO<String> = IO { readLine().orEmpty() }
fun stdout(msg: String): IO<Unit> = IO { println(msg) }

fun search(where: String, what: String): Int? =
    where.split(" ").withIndex().find { it.value == what }?.index?.plus(1)


fun main() {
    stdin().continueIO { whereSearch ->
        stdin().continueIO { whatSearch ->
            search(whereSearch, whatSearch).let { idx ->
                when (idx) {
                    null -> stdout("Not Found")
                    else -> stdout("$idx")
                }
            }
        }
    }.unsafeRun()
}
