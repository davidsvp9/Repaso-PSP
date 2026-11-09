/**
 * ============================================================================
 * EJERCICIO 1 - PARTE 1 DEL PDF: GESTIÓN DE PROCESOS (ProcessBuilder)
 * ============================================================================
 * TEMA: Creación de procesos hijos, comunicación bidireccional (stdin/stdout/stderr),
 * estados y prevención de la serialización accidental.
 */

// Función auxiliar para lanzar un proceso hijo independiente en el Sistema Operativo.
// Recibe 'name' que se pasa como argumento de línea de comandos al JAR hijo.
// Retorna la instancia de 'Process' devuelta por .start().
fun launchChild(name: String): Process =
    // ProcessBuilder recibe comando y argumentos como cadenas independientes (String aparte).
    // .start() arranca el proceso hijo en el SO y devuelve el objeto Process para interactuar con él.
    // OJO EXAMEN: .start() NO bloquea; la ejecución del padre continúa inmediatamente.
    ProcessBuilder("java", "-jar", "ChildWorker.jar", name).start()

// Función auxiliar para enviar datos a la entrada estándar (stdin) del proceso hijo.
// Recibe la referencia al proceso destino 'process' y el texto 'text' a enviar.
fun send(process: Process, text: String) {
    // 'process.outputStream': flujo de salida DESDE la perspectiva del padre.
    // Va directo al 'stdin' (entrada estándar) del proceso hijo. (Nombres "al revés").
    // .bufferedWriter(): envuelve el flujo en un buffer de texto eficiente.
    // .use { }: estructura de Kotlin que cierra automáticamente el stream al salir del bloque.
    // Cerrar el stream es vital porque avisa al hijo (EOF) de que no llegará más texto.
    process.outputStream.bufferedWriter().use {
        // Escribimos el texto obligatoriamente terminado en '\n' porque el hijo usa readln(),
        // el cual se quedaría bloqueado indefinidamente esperando el fin de línea.
        it.write("$text\n")
    }
}

fun main() {
    // ------------------------------------------------------------------------
    // 1. LANZAMIENTO CONCURRENTE (PATRÓN CORRECTO DEL PDF: SECCIÓN 1.7)
    // ------------------------------------------------------------------------
    // Lanzamos A y luego B sin esperar entre medias. Ambos coexisten en el SO.
    // Si hiciéramos launchChild("A") -> a.waitFor() -> launchChild("B"),
    // caeríamos en SERIALIZACIÓN ACCIDENTAL (error típico de examen).
    val a = launchChild("A")
    val b = launchChild("B")

    // .isAlive consulta en este instante si el proceso sigue activo en el SO.
    // NO bloquea ni espera. Tras arrancar suele dar 'true' (a menos que fuera ultrarrápido).
    println("A vivo: ${a.isAlive}")
    println("B vivo: ${b.isAlive}")

    // ------------------------------------------------------------------------
    // 2. COMUNICACIÓN HACIA EL HIJO (STDIN)
    // ------------------------------------------------------------------------
    // Enviamos un saludo a cada hijo. Los datos viajan por el pipe de entrada del hijo.
    send(a, "Hola desde el padre")
    send(b, "Adios desde el padre")

    // ------------------------------------------------------------------------
    // 3. ESPERA DE TERMINACIÓN (waitFor)
    // ------------------------------------------------------------------------
    // a.waitFor() bloquea el hilo 'main' hasta que el proceso hijo A finaliza.
    // Retorna el código de salida (exit code): típicamente 0 indica ejecución exitosa.
    // (Equivalente en hilos: Thread.join()).
    val codeA = a.waitFor()
    val codeB = b.waitFor()

    // ------------------------------------------------------------------------
    // 4. LECTURA DE STDOUT Y STDERR DEL HIJO
    // ------------------------------------------------------------------------
    // 'inputStream': lo que el hijo emite por su salida estándar (stdout / println)
    // entra al padre como inputStream.
    val outA = a.inputStream.bufferedReader().use { it.readLine() }
    val outB = b.inputStream.bufferedReader().use { it.readLine() }

    // 'errorStream': lo que el hijo emite por su salida de error (stderr / System.err.println)
    // entra al padre como errorStream.
    val errA = a.errorStream.bufferedReader().use { it.readLine() }
    val errB = b.errorStream.bufferedReader().use { it.readLine() }

    // ------------------------------------------------------------------------
    // 5. COMPROBACIÓN TRAS TERMINACIÓN
    // ------------------------------------------------------------------------
    // Tras completarse waitFor(), se garantiza al 100% que isAlive == false.
    println("A vivo: ${a.isAlive}")
    println("B vivo: ${b.isAlive}")

    // Imprimimos la respuesta recibida de cada proceso:
    // NOTA EXAMEN (PDF 1.8): Que imprimamos 'A' antes que 'B' NO significa
    // que A terminara antes físicamente; solo refleja el orden en que el padre hace el print.
    println("A -> $outA - $errA - $codeA")
    println("B -> $outB - $errB - $codeB")
}
