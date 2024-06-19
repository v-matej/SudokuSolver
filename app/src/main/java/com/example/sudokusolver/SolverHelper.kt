package com.example.sudokusolver

object SolverHelper {
    fun solveSudoku(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    for (num in 1..9) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num
                            if (solveSudoku(board)) {
                                return true
                            }
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (d in 0..8) {
            if (board[row][d] == num) {
                return false
            }
        }

        for (r in 0..8) {
            if (board[r][col] == num) {
                return false
            }
        }

        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i + startRow][j + startCol] == num) {
                    return false
                }
            }
        }

        return true
    }
}