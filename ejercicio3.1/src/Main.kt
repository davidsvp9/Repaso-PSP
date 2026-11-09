fun main() {
    val lento = Thread{
        println("Lento empieza")
        Thread.sleep(3000)
        println("Lento termina")
    }

    lento.start()
    println("main continua")
    Thread.sleep(1000)
    println(lento.state)
    lento.join()
    println(lento.state)
    println("main termina")
}