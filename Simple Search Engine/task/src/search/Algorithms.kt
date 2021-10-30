package search

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
