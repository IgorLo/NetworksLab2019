package igorlo.ind2

import igorlo.TextColors
import igorlo.util.Exchange.readMessage
import igorlo.util.Exchange.sendMessage
import org.apache.log4j.BasicConfigurator
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import org.apache.log4j.Logger

class Client {
    private val logger: Logger
    private val socket: Socket
    private val readingThread: Thread

    companion object {
        private const val PORT = 1488
        private const val CONSOLE_WIDTH = 100
        private const val INFO = "\tЭто клиентское приложение для индивидуального задания\n" +
                "\tпо курсу \"Основы компьютерных сетей\".\n\n" +
                "\tАвтор - Игорь Лопатинский"
        private val COMMAND_LIST = listOf(
                Command(
                        "A <ID> <NAME> <SHORT NAME> <COURCE>",
                        "Добавить новую валюту"
                ),
                Command(
                        "D <ID>",
                        "Удалить валюту"
                ),
                Command(
                        "R",
                        "Посмотреть текущие состояния валют"
                ),
                Command(
                        "C <ID> <COURCE>",
                        "Изменить курс валюты"
                ),
                Command(
                        "H <ID>",
                        "Посмотреть историю курса валюты"
                ),
                Command(
                        "EXIT",
                        "Выйти из программы"
                ),
                Command(
                        "HELP",
                        "Список команд"
                ),
                Command(
                        "INFO",
                        "Информация о проекте"
                )
        )
    }

    init {
        Thread.currentThread().name = "Main-client"
        BasicConfigurator.configure()
        logger = Logger.getLogger(Client::class.java)
        logger.info("Инициализация клиента")
        socket = Socket("localhost", PORT)
        logger.info("Сокет инициализирован")
        readingThread = Thread(Runnable {
            while (true) {
                colorPrint("\n${readMessage(socket)}\n", TextColors.ANSI_YELLOW)
            }
        }, "Reader-client")
    }

    fun run() {
        logger.info("Начало работы клиента")
        readingThread.start()
        logger.info("Запустил отдельный поток на чтение сообщений")
        val scanner = Scanner(System.`in`)
        logger.info("Сканер консоли инициализирован")
        var command: String
        loop@ while (true) {
            Thread.sleep(100)
            colorPrint("\nВведите команду: \n", TextColors.ANSI_CYAN)
            command = scanner.nextLine()
            when (parseInput(command)) {
                Action.TO_SERVER -> {
                    sendMessage(socket, command)
                }
                Action.HELP -> {
                    printHelp()
                }
                Action.INFO -> {
                    printInfo()
                }
                Action.EXIT -> {
                    colorPrint("До свидания!", TextColors.ANSI_CYAN)
                    break@loop
                }
            }

        }
        close()
    }

    private fun printInfo() {
        colorPrint("\n$INFO\n", TextColors.ANSI_PURPLE)
    }

    private fun printHelp() {
        colorPrint("\n-".padEnd(CONSOLE_WIDTH, '-'), TextColors.ANSI_WHITE)
        colorPrint("\nСписок команд".padStart(CONSOLE_WIDTH/2 - 6), TextColors.ANSI_WHITE)
        colorPrint("\n-".padEnd(CONSOLE_WIDTH, '-'), TextColors.ANSI_WHITE)
        for (command in COMMAND_LIST){
            colorPrint(
                    "\n${command.mnemonic.padEnd(CONSOLE_WIDTH/2 - 2)}| ${command.description}",
                    TextColors.ANSI_BLUE
            )
        }
        colorPrint("\n-".padEnd(CONSOLE_WIDTH, '-'), TextColors.ANSI_WHITE)
    }

    private fun colorPrint(text: String, color: String) {
        print("${color}$text${TextColors.ANSI_RESET}")
    }

    private fun parseInput(command: String): Action {
        when (command.toLowerCase()) {
            "info" -> return Action.INFO
            "exit" -> return Action.EXIT
            "help" -> return Action.HELP
            else -> return Action.TO_SERVER
        }
    }

    private fun close() {
        logger.info("Закрываю сокет")
        socket.close()
        logger.info("Завершаю работу")
    }
}

data class Command(val mnemonic: String, val description: String)

enum class Action {
    TO_SERVER, HELP, EXIT, INFO
}
