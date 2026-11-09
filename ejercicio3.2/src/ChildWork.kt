fun main(args: Array<String>) {
    val nombre = args[0]
    val retardo = args[1].toLong()

    val texto = readln()

    Thread.sleep(retardo)

    println("$nombre -> ${texto.uppercase()}")
    System.err.println("$nombre: trabajo terminado")
}