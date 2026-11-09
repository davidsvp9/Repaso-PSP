fun launchChild(name: String, delayMs: String): Process =
    ProcessBuilder("java", "-jar", "ejercicio3.2.jar", name, delayMs).start()

fun send(process: Process, text: String) {
    process.outputStream.bufferedWriter().use {
        it.write("$text\n")
    }
}

fun main() {
    val reloj = Thread{
        //bloque try/catch necesario para que el .interrupt() no de error
        try {
            while (!Thread.currentThread().isInterrupted){
                println("supervisor activo")
                Thread.sleep(1000)
            }
        }catch (e: InterruptedException){
            println("interrumpido")
        }

    }

    reloj.start()

    val a = launchChild("A", "2000")
    val b = launchChild("B", "1000")

    //3. Envía entrada a ambos
    send(a, "Proceso A")
    send(b, "Proceso B")

    //.join no funciona en procesos
    val codeA = a.waitFor()
    val codeB = b.waitFor()

    //leer informacion
    val outA = a.inputStream.bufferedReader().use{it.readLine()}
    val outB = b.inputStream.bufferedReader().use{it.readLine()}

    val errA = a.errorStream.bufferedReader().use{it.readLine()}
    val errB = b.errorStream.bufferedReader().use{it.readLine()}

    reloj.interrupt()
    reloj.join()

    // imprimir resultados
    println("A: $outA - $errA - $codeA")
    println("B: $outB - $errB - $codeB")
}