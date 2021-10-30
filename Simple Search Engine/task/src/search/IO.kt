package search

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

val stdin = IO { readLine().orEmpty() }
val readInt = stdin.mapT { toInt() }

fun stdout(msg: String = "") = IO { println(msg) }
val endL = stdout()
