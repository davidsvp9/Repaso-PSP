/**
 * ============================================================================
 * EJERCICIO 1 - PROCESO HIJO (ChildWorker.kt)
 * ============================================================================
 * TEMA: Recepción de argumentos, lectura de stdin y escritura en stdout/stderr.
 * Este archivo se compila habitualmente como un .jar ejecutable (ChildWorker.jar).
 */

fun main(args: Array<String>) {
    // 1. RECEPCIÓN DE PARÁMETROS DE EJECUCIÓN (CLI):
    // Viene directamente de los argumentos enviados por el padre en ProcessBuilder:
    // args[0] es el nombre asignado al proceso hijo ("A" o "B").
    val name = args[0]

    // 2. LECTURA DESDE LA ENTRADA ESTÁNDAR (STDIN):
    // readln() lee una línea completa proveniente de la consola o del padre.
    // El padre la envía a través de 'process.outputStream.bufferedWriter().write("...\n")'.
    // Si el padre no manda '\n' ni cierra el stream, readln() se queda bloqueado esperando.
    val text = readln()

    // 3. ESCRITURA EN SALIDA ESTÁNDAR (STDOUT):
    // println escribe en la salida estándar del proceso hijo.
    // En el padre, este texto se recoge a través de 'process.inputStream'.
    println("$name -> ${text.uppercase()}")

    // 4. ESCRITURA EN SALIDA DE ERROR ESTÁNDAR (STDERR):
    // System.err.println escribe en el canal de errores.
    // En el padre, este texto se recoge a través de 'process.errorStream'.
    System.err.println("$name: fin")

    // Al terminar el main, el proceso finaliza devolviendo código 0 (éxito) al SO,
    // el cual es capturado en el padre mediante 'process.waitFor()'.
}
