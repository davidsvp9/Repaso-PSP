/**
 * ============================================================================
 * EJERCICIO COMPLEMENTARIO: CUENTA BANCARIA Y DATOS COMPARTIDOS (PDF PARTE 3)
 * ============================================================================
 * TEMA: Estado mutable compartido, condición de carrera (race condition),
 * actualización perdida (lost update), sección crítica y synchronized(lock).
 *
 * (¡ESTA PARTE ESTABA COMPLETAMENTE AUSENTE EN LOS ARCHIVOS ORIGINALES DE LA CARPETA!)
 */

// ============================================================================
// CLASE CUENTA BANCARIA
// ============================================================================
class Cuenta {
    // 1. ESTADO MUTABLE COMPARTIDO:
    // 'saldo' reside en el montón (heap). Cuando varios hilos reciben la referencia
    // a la misma instancia de 'Cuenta', ambos acceden y modifican esta misma variable.
    // 'private set' garantiza el principio de encapsulación: nadie desde fuera puede
    // alterar 'saldo' directamente saltándose los mecanismos de sincronización (PDF 3.8).
    var saldo = 0
        private set

    // 2. MONITOR / OBJETO DE BLOQUEO ESTABLE:
    // 'private val lock = Any()': objeto estable único por cada instancia de 'Cuenta'.
    // Su monitor interno se utiliza como cerradura para coordinar la exclusión mutua.
    // Todos los hilos que llamen a 'ingresar()' competirán exactamente por ESTE MISMO monitor.
    private val lock = Any()

    // ------------------------------------------------------------------------
    // VERSIÓN CORRECTA: CON EXCLUSIÓN MUTUA MEDIANTE synchronized(lock)
    // ------------------------------------------------------------------------
    fun ingresar(cantidad: Int) {
        // synchronized(lock): adquiere el monitor del objeto 'lock'.
        // EXCLUSIÓN MUTUA (PDF 3.7): como máximo UN solo hilo puede ejecutar el bloque protegido a la vez.
        // Si el hilo A está dentro, el hilo B que intente entrar queda en estado Thread.State.BLOCKED.
        // Cuando A sale, libera el monitor y B puede entrar.
        synchronized(lock) {
            // SECCIÓN CRÍTICA (PDF 3.7):
            // La operación 'saldo += cantidad' NO ES ATÓMICA (PDF 3.3).
            // A nivel de bytecode/procesador consta de 3 pasos independientes:
            //   1. LEER: actual = saldo
            //   2. CALCULAR: nuevo = actual + cantidad
            //   3. ESCRIBIR: saldo = nuevo
            // Al protegerla con synchronized, garantizamos que ningún otro hilo pueda
            // intercalar sus pasos entre medias, preservando la ATOMICIDAD y VISIBILIDAD.
            saldo += cantidad
        }
    }

    // ------------------------------------------------------------------------
    // VERSIÓN INCORRECTA 1: SIN SINCRONIZACIÓN (PROVOCA CONDICIÓN DE CARRERA)
    // ------------------------------------------------------------------------
    fun ingresarSinSincronizar(cantidad: Int) {
        // ¡PELIGRO! Sin protección:
        // El hilo A lee saldo (0), la CPU conmuta al hilo B que lee saldo (0),
        // B calcula y escribe 5, y luego A calcula y escribe 10 sobrescribiendo a B.
        // Resultado: ACTUALIZACIÓN PERDIDA (Lost Update, PDF 3.4).
        saldo += cantidad
    }

    // ------------------------------------------------------------------------
    // VERSIÓN INCORRECTA 2: ERROR TÍPICO DE EXAMEN - LOCK DISTINTO (PDF 3.9)
    // ------------------------------------------------------------------------
    fun ingresarConLockErroneo(cantidad: Int) {
        // ❌ ERROR TÍPICO DE EXAMEN (PDF 3.9):
        // Crear el objeto lock dentro de la función provoca que CADA llamada cree un NUEVO monitor.
        val lockLocal = Any()
        synchronized(lockLocal) {
            // Cada hilo adquiere un lock distinto: NO compiten por el mismo cerrojo.
            // Por tanto, ambos entran a la vez y la exclusión mutua NO EXISTE.
            saldo += cantidad
        }
    }
}

fun main() {
    println("=== PARTE 3: DEMOSTRACIÓN DE HILOS CON ESTADO COMPARTIDO ===")

    // ========================================================================
    // CASO A: EJECUCIÓN SINCRONIZADA (CORRECTA)
    // ========================================================================
    val cuenta = Cuenta()

    // mete10 hace 1.000.000 de ingresos de 10
    val mete10 = Thread {
        for (i in 1..1_000_000) {
            cuenta.ingresar(10)
        }
    }

    // mete5 hace 1.000.000 de ingresos de 5
    val mete5 = Thread {
        for (i in 1..1_000_000) {
            cuenta.ingresar(5)
        }
    }

    // ESTRUCTURA ESTÁNDAR DEL MAIN (PDF 3.2 y 3.10):
    // 1. Arrancar ambos hilos concurrentemente
    mete10.start()
    mete5.start()

    // 2. Esperar a que ambos hilos terminen con join()
    // OJO TRAMPA EXAMEN (PDF 3.11): join() vs synchronized()
    // - synchronized(lock): garantiza que las actualizaciones no se intercalen erróneamente.
    // - join(): garantiza que los hilos han terminado antes de que el padre compruebe el resultado.
    // join() NO sustituye a synchronized.
    mete10.join()
    mete5.join()

    // 3. Comprobar la invariante del saldo final (PDF 3.2):
    // Esperado: 1.000.000 * 10 + 1.000.000 * 5 = 15.000.000
    println("Saldo final con synchronized: ${cuenta.saldo} (Esperado: 15000000)")

    // ========================================================================
    // AVISO EXPLICITO DE CONTENIDO FUERA DE TEMARIO (PDF 3.6):
    // ========================================================================
    // [NO ENTRA EN EL EXAMEN]:
    // - Modificador 'volatile' (en Kotlin @Volatile).
    // - Clases atómicas de java.util.concurrent.atomic (AtomicInteger, AtomicLong, etc.).
    // - Formalismos detallados del Java Memory Model (JMM), cachés L1/L2/L3 y RAM física.
    // - Semáforos (Semaphore) y Locks explícitos (ReentrantLock).
    // - Corrutinas (kotlinx.coroutines).
    // El examen exige ÚNICAMENTE dominar 'synchronized(lock)', la condición de carrera,
    // y la exclusión mutua con el monitor adecuado.
}
