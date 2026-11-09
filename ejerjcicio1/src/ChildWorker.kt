fun main(args: Array<String>) {
    val name = args[0]
    val text = readln()
    println("$name -> ${text.uppercase()}")
    System.err.println("$name: fin")
}