/**
 * ============================================================================
 * EJERCICIO 3.2 - PROCESO HIJO CON RETARDO (ChildWork.kt)
 * ============================================================================
 * TEMA: Simulación de carga/retardo en proceso hijo, lectura de stdin y escritura.
 * Este archivo se empaqueta típicamente como 'ejercicio3.2.jar'.
 */

fun main(args: Array<String>) {
    // 1. RECEPCIÓN DE ARGUMENTOS CLI:
    // args[0] -> Nombre identificador ("A" o "B")
    // args[1] -> Milisegundos de retardo (ej. "2000" para A, "1000" para B)
    // Provienen de la llamada ProcessBuilder("java", "-jar", "ejercicio3.2.jar", name, delayMs).
    val nombre = args[0]
    val retardo = args[1].toLong()

    // 2. LECTURA DE LA ENTRADA ESTÁNDAR (STDIN):
    // Espera a que el proceso padre le escriba una línea por su 'outputStream'.
    // Queda bloqueado aquí hasta recibir '\n' o el cierre del stream (EOF).
    val texto = readln()

    // 3. SIMULACIÓN DE PROCESAMIENTO / RETARDO:
    // El hilo principal de este proceso hijo se duerme durante los milisegundos indicados.
    // Como A duerme 2000 ms y B duerme 1000 ms, B terminará su cálculo antes que A,
    // demostrando la concurrencia entre ambos procesos en el Sistema Operativo.
    Thread.sleep(retardo)

    // 4. ESCRITURA EN SALIDA ESTÁNDAR (STDOUT):
    // El padre leerá esta línea a través de 'process.inputStream'.
    println("$nombre -> ${texto.uppercase()}")

    // 5. ESCRITURA EN SALIDA DE ERROR (STDERR):
    // El padre leerá esta línea a través de 'process.errorStream'.
    System.err.println("$nombre: trabajo terminado")

    // Fin del proceso con código de salida 0 (éxito), capturable con 'waitFor()'.
}
