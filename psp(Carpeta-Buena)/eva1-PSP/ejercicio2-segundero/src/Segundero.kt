/**
 * ============================================================================
 * EJERCICIO COMPLEMENTARIO: EL SEGUNDERO Y SUBCLASE DE THREAD (PDF PARTE 2 - 2.10)
 * ============================================================================
 * ESTE EJERCICIO ES CALIFICADO EN EL PDF COMO "MUY PROBABLE EN EXAMEN".
 *
 * TEMAS QUE ABARCA:
 * 1. Creación de hilo mediante SUBCLASE de Thread (PDF 2.2).
 * 2. Diferencia entre datos locales al hilo vs estado compartido (PDF 2.1 y 3.1).
 * 3. Cancelación cooperativa limpia con interrupt() y catch(InterruptedException) (PDF 2.7).
 * 4. Consulta de estados isAlive y Thread.State (PDF 2.6).
 * 5. Regla de oro: Un Thread SOLO se arranca una vez (PDF 2.8).
 */

// ----------------------------------------------------------------------------
// FORMA 1 DE CREAR UN HILO: SUBCLASE DE THREAD (PDF 2.2)
// Pasamos el nombre del hilo al constructor padre: Thread("segundero")
// ----------------------------------------------------------------------------
class Segundero : Thread("segundero") {

    // override fun run() contiene el trabajo que ejecutará este hilo en su flujo independiente.
    override fun run() {
        // 'segundos' es una VARIABLE LOCAL al método run().
        // Cada ejecución de este hilo tiene su propia copia en la pila (stack).
        // NO es un estado compartido, por lo que NO hay problemas de concurrencia sobre ella.
        var segundos = 0

        try {
            // Bucle potencialmente infinito; solo terminará cuando se solicite su interrupción.
            while (true) {
                // Thread.currentThread().name devolverá "segundero".
                // NOTA CLAVE: El primer segundo ("0 · segundero") sale INMEDIATAMENTE
                // porque este println va antes de la llamada a sleep().
                println("$segundos · ${Thread.currentThread().name}")

                // Thread.sleep(1000) duerme el hilo ~1 segundo.
                // En este intervalo, el estado del hilo en la JVM es TIMED_WAITING.
                // Si mientras duerme se llama a segundero.interrupt(), se lanza InterruptedException.
                Thread.sleep(1000)

                // Incrementa el contador local en 1
                segundos++
            }
        } catch (e: InterruptedException) {
            // El hilo recibe la señal cooperativa de parada durante el sleep().
            // Capturar la excepción nos permite salir del bucle while limpiamente.
            println("Segundero detenido")
        }
        // Al alcanzar el final de run(), el hilo finaliza y pasa a estado TERMINATED.
    }
}

fun main() {
    println("--- PRIMERA EJECUCIÓN DEL SEGUNDERO ---")

    // 1. Instanciamos el objeto hilo. Estado inicial: NEW
    var segundero = Segundero()
    println("Estado inicial: ${segundero.state}, isAlive=${segundero.isAlive}") // NEW, false

    // 2. Arrancamos el hilo con .start().
    // OJO TRAMPA EXAMEN: Si llamáramos a 'segundero.run()', se ejecutaría en el hilo 'main',
    // bloqueando main y NO creando un hilo nuevo.
    segundero.start()

    // 3. El hilo 'main' se duerme 3500 ms (3.5 segundos).
    // 'main' simplemente deja transcurrir tiempo; no coordina con sleep.
    // Durante este tiempo saldrán por pantalla: 0, 1, 2 y 3.
    Thread.sleep(3500)

    // 4. Parada cooperativa: interrupt() solicita parar (PDF 2.7).
    // NO mata el hilo al instante: provoca que el Thread.sleep(1000) lance InterruptedException.
    segundero.interrupt()

    // 5. Esperamos con join() a que termine completamente (PDF 2.5).
    // sleep(3500) solo dejó pasar tiempo; la confirmación de finalización SIEMPRE se hace con join().
    segundero.join()

    // Comprobamos estado tras finalizar:
    // isAlive == false, state == TERMINATED
    println("1: vivo=${segundero.isAlive}, estado=${segundero.state}")


    // ------------------------------------------------------------------------
    // REGLA CRUCIAL DE EXAMEN (PDF 2.8): UN THREAD SOLO SE ARRANCA UNA VEZ
    // ------------------------------------------------------------------------
    /*
     * Si intentáramos hacer:
     *     segundero.start()
     * La JVM lanzaría: java.lang.IllegalThreadStateException.
     * No existe la transición TERMINATED -> NEW en el mismo objeto.
     * Para reiniciar la tarea, ES OBLIGATORIO crear un NUEVO objeto:
     */
    println("\n--- SEGUNDA EJECUCIÓN (CON NUEVO OBJETO) ---")

    // Creamos una NUEVA instancia de Segundero:
    segundero = Segundero() // Objeto completamente nuevo
    segundero.start()

    Thread.sleep(2500) // Deja pasar 2.5 segundos (saldrán 0, 1, 2)
    segundero.interrupt()
    segundero.join()

    println("2: vivo=${segundero.isAlive}, estado=${segundero.state}")
}
