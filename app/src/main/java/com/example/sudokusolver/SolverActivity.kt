package com.example.sudokusolver

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.sudokusolver.databinding.ActivitySolverBinding

class SolverActivity : AppCompatActivity() {
    private val storageHelper = StorageHelper(this)
    private var mnistClassifier = MnistClassifier(this)
    private lateinit var binding: ActivitySolverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_solver
        )

        mnistClassifier.initialize().addOnSuccessListener {
            val cells = storageHelper.loadImages()

            val sudokuGrid = mnistClassifier.classifySudokuGrid(cells)

            displayGrid(sudokuGrid)

            findViewById<Button>(R.id.solve_button).setOnClickListener {
                if (SolverHelper.solveSudoku(sudokuGrid)) {
                    displayGrid(sudokuGrid)
                } else {
                    binding.textView2.text = "Sudoku cannot be solved."
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("My app", "Error initializing MnistClassifier", exception)
        }
    }

    private fun displayGrid(grid: Array<IntArray>) {
        val gridString = StringBuilder()
        for (row in grid) {
            val rowString = row.joinToString(" ")
            gridString.append(rowString).append("\n")
        }
        binding.textView2.text = gridString.toString()
    }
}
