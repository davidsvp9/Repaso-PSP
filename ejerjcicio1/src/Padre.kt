fun launchChild(name: String): Process =
    ProcessBuilder("java", "-jar", "ChildWorker.jar", name).start()

fun send(process: Process, text: String) {
    process.outputStream.bufferedWriter().use {
        it.write("$text\n")
    }
}

fun main() {
    val a = launchChild("A")
    val b = launchChild("B")

    println("A vivo: ${a.isAlive}")
    println(("B vivo: ${b.isAlive}"))

    send(a, "Hola desde el padre")
    send(b, "Adios desde el padre")

    val codeA = a.waitFor()
    val codeB = b.waitFor()

    val outA = a.inputStream.bufferedReader().use {it.readLine()}
    val outB = b.inputStream.bufferedReader().use {it.readLine()}

    val errA = a.errorStream.bufferedReader().use {it.readLine()}
    val errB = b.errorStream.bufferedReader().use {it.readLine()}

    println("A vivo: ${a.isAlive}")
    println(("B vivo: ${b.isAlive}"))

    println("A -> $outA - $errA - $codeA")
    println("B -> $outB - $errB - $codeB")


}