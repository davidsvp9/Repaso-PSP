/**
 * ============================================================================
 * EJERCICIO 3.2 - COMBINACIÓN DE PROCESOS E HILOS CON INTERRUPCIÓN
 * ============================================================================
 * TEMA: Lanzamiento concurrente de procesos hijos, hilo supervisor en segundo
 * plano, cancelación cooperativa con interrupt() y control de tiempos.
 */

// Lanza un proceso hijo independiente pasando su nombre y el retardo que debe simular.
// Retorna la instancia de 'Process' devuelta por .start().
fun launchChild(name: String, delayMs: String): Process =
    // Pasa cada argumento como un String separado a ProcessBuilder.
    // .start() no bloquea; arranca el proceso hijo en el Sistema Operativo.
    ProcessBuilder("java", "-jar", "ejercicio3.2.jar", name, delayMs).start()

// Envía texto a la entrada estándar (stdin) del proceso hijo.
fun send(process: Process, text: String) {
    // process.outputStream = canal que escribe en el stdin del hijo (visto desde el padre).
    // .bufferedWriter(): optimiza la escritura en buffer.
    // .use { }: cierra automáticamente el stream al salir, enviando EOF al hijo.
    process.outputStream.bufferedWriter().use {
        // Obligatorio '\n' porque el proceso hijo espera una línea completa con readln().
        it.write("$text\n")
    }
}

fun main() {
    // ------------------------------------------------------------------------
    // 1. CREACIÓN DEL HILO SUPERVISOR (CANCELACIÓN COOPERATIVA - PDF 2.7)
    // ------------------------------------------------------------------------
    val reloj = Thread {
        // La cancelación en Java/Kotlin es cooperativa: interrupt() NO "mata" el hilo,
        // solo le envía una señal. El hilo debe comprobarla y cooperar para salir.
        try {
            // Condición 1: Comprobación activa de la bandera de interrupción si estuviera calculando.
            while (!Thread.currentThread().isInterrupted) {
                println("supervisor activo")
                // Thread.sleep() es una operación interrumpible.
                // Si mientras duerme alguien llama a reloj.interrupt(), sleep() se interrumpe
                // inmediatamente y lanza InterruptedException (limpiando el flag de interrupción).
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            // Condición 2: Captura de la excepción cuando el hilo es interrumpido durante sleep().
            // Al capturar la excepción y terminar este bloque lambda, el hilo finaliza limpiamente.
            println("interrumpido")
        }
        // Al terminar el bloque, el hilo pasa a estado TERMINATED.
    }

    // Arranca el hilo supervisor en la JVM. Se ejecutará concurrentemente con 'main'.
    reloj.start()

    // ------------------------------------------------------------------------
    // 2. LANZAMIENTO DE PROCESOS HIJOS CONCURRENTES (PDF 1.7)
    // ------------------------------------------------------------------------
    // Lanzamos A (con retardo de 2000 ms) y B (con retardo de 1000 ms).
    // Se lanzan ambos antes de esperar para no serializar la ejecución.
    val a = launchChild("A", "2000")
    val b = launchChild("B", "1000")

    // ------------------------------------------------------------------------
    // 3. ENVÍO DE DATOS A LA ENTRADA ESTÁNDAR (STDIN)
    // ------------------------------------------------------------------------
    // Enviamos el mensaje que cada hijo leerá con 'readln()'.
    send(a, "Proceso A")
    send(b, "Proceso B")

    // ------------------------------------------------------------------------
    // 4. ESPERA DE TERMINACIÓN DE LOS PROCESOS (waitFor vs join - PDF 2.5)
    // ------------------------------------------------------------------------
    // RECORDATORIO CLAVE DE EXAMEN:
    // Para esperar un PROCESO se usa process.waitFor(). Devuelve el código de salida int (0 = OK).
    // Para esperar un HILO se usa thread.join(). No devuelve valor.
    // (En un proceso NO existe método join()).
    val codeA = a.waitFor() // Espera a que termine el proceso hijo A
    val codeB = b.waitFor() // Espera a que termine el proceso hijo B

    // ------------------------------------------------------------------------
    // 5. LECTURA DE STDOUT Y STDERR DE CADA PROCESO
    // ------------------------------------------------------------------------
    // inputStream lee lo que el hijo escribió con println()
    val outA = a.inputStream.bufferedReader().use { it.readLine() }
    val outB = b.inputStream.bufferedReader().use { it.readLine() }

    // errorStream lee lo que el hijo escribió con System.err.println()
    val errA = a.errorStream.bufferedReader().use { it.readLine() }
    val errB = b.errorStream.bufferedReader().use { it.readLine() }

    // ------------------------------------------------------------------------
    // 6. DETENCIÓN LIMPIA DEL HILO SUPERVISOR
    // ------------------------------------------------------------------------
    // Una vez que los procesos hijos han terminado, ya no necesitamos al supervisor.
    // reloj.interrupt(): solicita al hilo que se detenga (hace saltar InterruptedException en su sleep).
    // OJO EXAMEN (PDF 2.7): Thread.stop(), Thread.suspend() y Thread.resume() están OBSOLETOS
    // y NUNCA se deben usar para detener o pausar hilos. Solo se usa interrupt().
    reloj.interrupt()

    // reloj.join(): bloquea 'main' hasta que el hilo supervisor confirme que ha terminado su run().
    reloj.join()

    // ------------------------------------------------------------------------
    // 7. IMPRESIÓN DE RESULTADOS FINALES
    // ------------------------------------------------------------------------
    // Aunque B tardó 1000ms y terminó antes que A (que tardó 2000ms),
    // aquí imprimimos A y luego B según el orden de nuestro código en 'main'.
    println("A: $outA - $errA - $codeA")
    println("B: $outB - $errB - $codeB")
}
