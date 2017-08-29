package gol.controller

import gol.domain.Greeting
import gol.domain.HelloMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

val ALIVE = 'O'
val DEAD = '.'
val MAX_LEN = 1000

@Controller
class GreetingController {
    var start: Array<Array<Char?>?> = arrayOfNulls(MAX_LEN)
    var maxRows = 0
    var maxColumns = 0

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    @Throws(Exception::class)
    fun greeting(message: HelloMessage): Greeting {
        populateStart(message.name)
        return Greeting(convertToString(computeNext()))
    }

    private fun convertToString(matrix: Array<Array<Char?>?>): String {
        var ret = ""
        for (i in 1..maxRows - 1) {
            for (j in 1..maxColumns - 1) {
                ret += matrix[i]!![j]
            }
            ret += "\n"
        }
        return ret

    }

    private fun computeNext(): Array<Array<Char?>?> {
        val next: Array<Array<Char?>?> = arrayOfNulls(maxRows)
        for (i in 1..maxRows - 1) {
            next[i] = arrayOfNulls(maxColumns)
            for (j in 1..maxColumns - 1) {
                if (underPopulation(start, i, j)) {
                    next[i]!![j] = DEAD
                    continue
                }
                if (hasNextGeneration(start, i, j)) {
                    next[i]!![j] = ALIVE
                    continue
                }
                if (overPopulation(start, i, j)) {
                    next[i]!![j] = DEAD
                    continue
                }
                if (reproduction(start, i, j)) {
                    next[i]!![j] = ALIVE
                    continue
                }
                next[i]!![j] = start[i]!![j]
            }

        }
        return next
    }

    private fun underPopulation(matrix: Array<Array<Char?>?>, row: Int, col: Int): Boolean {
        return isAlive(matrix, row, col) && countLiveNeighbours(matrix, row, col) < 2
    }

    private fun hasNextGeneration(matrix: Array<Array<Char?>?>, row: Int, col: Int): Boolean {
        val neighbours = countLiveNeighbours(matrix, row, col)
        return isAlive(matrix, row, col) && (neighbours == 2 || neighbours == 3)
    }

    private fun overPopulation(matrix: Array<Array<Char?>?>, row: Int, col: Int): Boolean {
        return isAlive(matrix, row, col) && (countLiveNeighbours(matrix, row, col) > 3)
    }

    private fun reproduction(matrix: Array<Array<Char?>?>, row: Int, col: Int): Boolean {
        return isDead(matrix, row, col) && countLiveNeighbours(matrix, row, col) == 3
    }

    private fun isAlive(matrix: Array<Array<Char?>?>, row: Int, col: Int): Boolean {

        return matrix[row]!![col] == ALIVE
    }

    private fun isDead(matrix: Array<Array<Char?>?>, row: Int, col: Int): Boolean = !isAlive(matrix, row, col)


    private fun countLiveNeighbours(matrix: Array<Array<Char?>?>, row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                if (matrix[row + i]!![col + j] == ALIVE) {
                    count++
                }
            }
        }
        return count
    }

    private fun populateStart(input: String) {
        val lines = input.trim().split("\n")
        for ((i, line) in lines.withIndex()) {
            var j = 1
            start[i + 1] = arrayOfNulls(MAX_LEN)
            for (c in line.trim()) {
                start[i + 1]?.set(j, c)
                j++
            }
            maxColumns = maxOf(maxColumns, j)
        }
        maxRows = lines.size + 1
        start[0] = arrayOfNulls(maxColumns + 1)
        start[maxRows] = arrayOfNulls(maxColumns + 1)
        for (i in 0..maxColumns) {
            start[0]!![i] = DEAD
            start[maxRows]!![i] = DEAD
        }

        for (i in 0..maxRows) {
            start[i]!![0] = DEAD
            start[i]!![maxColumns] = DEAD
        }
    }

}