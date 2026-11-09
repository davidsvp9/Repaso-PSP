/**
 * ============================================================================
 * EJERCICIO 3.1 - PARTE 2 DEL PDF: HILOS SIN DATOS COMPARTIDOS
 * ============================================================================
 * TEMA: Creación de hilos (lambda), ciclo de vida, estados (Thread.State),
 * sleep() vs join(), y start() vs run().
 */

fun main() {
    // ------------------------------------------------------------------------
    // 1. CREACIÓN DEL HILO MEDIANTE LAMBDA (FORMA 2 DEL PDF 2.2)
    // ------------------------------------------------------------------------
    // Se define el bloque de código que ejecutará el hilo en su método run().
    // En este punto, 'lento' está en estado NEW (creado, pero todavía no iniciado).
    // NOTA EXAMEN: La otra forma exigida en el PDF es heredar de Thread:
    // class MiHilo : Thread() { override fun run() { ... } }
    val lento = Thread {
        // Código ejecutado de manera concurrente por el nuevo hilo 'lento':
        println("Lento empieza")

        // Thread.sleep(3000): suspende la ejecución de ESTE hilo (lento) durante 3 segundos.
        // Mientras duerme, su estado en la JVM pasa a TIMED_WAITING.
        // OJO TRAMPA EXAMEN: El estado NO es 'BLOCKED'. 'BLOCKED' es solo para esperar un monitor/lock.
        Thread.sleep(3000)

        println("Lento termina")
        // Al terminar el bloque lambda, el hilo finaliza y pasa al estado TERMINATED.
    }

    // ------------------------------------------------------------------------
    // 2. ARRANQUE DEL HILO (start vs run - PDF 2.3)
    // ------------------------------------------------------------------------
    // .start(): solicita a la JVM crear un nuevo hilo de ejecución y llamar a run() en él.
    // TRAMPA TÍPICA DE EXAMEN:
    // Si pusiéramos 'lento.run()', sería una simple llamada a un método ordinario:
    // lo ejecutaría el hilo 'main' de forma secuencial y bloqueante, ¡NO crearía ningún hilo!
    lento.start()

    // El hilo 'main' continúa su propia ejecución de forma concurrente con 'lento'.
    println("main continua")

    // ------------------------------------------------------------------------
    // 3. OBSERVACIÓN DE ESTADOS (Thread.State - PDF 2.6)
    // ------------------------------------------------------------------------
    // El hilo 'main' se duerme 1 segundo para dar tiempo a que 'lento' ya haya empezado
    // y esté dentro de su Thread.sleep(3000).
    Thread.sleep(1000)

    // Consultamos el estado del hilo 'lento' en este momento:
    // Imprimirá TIMED_WAITING (porque 'lento' está dentro de su sleep de 3s).
    // RECORDATORIO DE EXAMEN: En Thread.State NO existe el estado RUNNING.
    // Los estados válidos son: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
    println(lento.state) // -> TIMED_WAITING

    // ------------------------------------------------------------------------
    // 4. ESPERA DE TERMINACIÓN CON join() (PDF 2.5)
    // ------------------------------------------------------------------------
    // lento.join(): bloquea al hilo que lo invoca (en este caso, 'main')
    // hasta que el hilo 'lento' haya terminado completamente su ejecución.
    // Mientras 'main' espera en el join(), el estado de 'main' es WAITING.
    // Si 'lento' ya hubiera terminado, join() retorna inmediatamente sin esperar.
    lento.join()

    // ------------------------------------------------------------------------
    // 5. ESTADO TRAS FINALIZAR (PDF 2.8)
    // ------------------------------------------------------------------------
    // Una vez superado el join(), 'lento' ha concluido y su estado es TERMINATED.
    println(lento.state) // -> TERMINATED

    // OJO EXAMEN (PDF 2.8): Un hilo SOLO se arranca una vez.
    // Si ahora intentáramos hacer 'lento.start()' de nuevo, la JVM lanzaría
    // IllegalThreadStateException. Para repetir la tarea hay que crear un new Thread.
    println("main termina")
}
