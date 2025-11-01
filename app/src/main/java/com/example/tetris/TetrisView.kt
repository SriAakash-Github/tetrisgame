package com.example.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TetrisView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val rows = 20
    private val cols = 10
    private var grid = Array(rows) { IntArray(cols) }
    private var score = 0
    private var linesCleared = 0
    private var gameOver = false

    private var currentPiece: Piece? = null
    private var currentX = 0
    private var currentY = 0

    private val paint = Paint()
    private var cellSize = 0f

    private val colors = listOf(
        Color.CYAN,    // I
        Color.BLUE,    // J
        Color.rgb(255, 165, 0), // L (Orange)
        Color.YELLOW,  // O
        Color.GREEN,   // S
        Color.MAGENTA, // T
        Color.RED      // Z
    )

    init {
        spawnNewPiece()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = minOf(w.toFloat() / cols, h.toFloat() / rows)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw grid
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (grid[row][col] > 0) {
                    paint.color = colors[grid[row][col] - 1]
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(
                        col * cellSize,
                        row * cellSize,
                        (col + 1) * cellSize,
                        (row + 1) * cellSize,
                        paint
                    )
                }
                // Draw grid lines
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRect(
                    col * cellSize,
                    row * cellSize,
                    (col + 1) * cellSize,
                    (row + 1) * cellSize,
                    paint
                )
            }
        }

        // Draw current piece
        currentPiece?.let { piece ->
            paint.style = Paint.Style.FILL
            paint.color = colors[piece.type]
            for (row in piece.shape.indices) {
                for (col in piece.shape[row].indices) {
                    if (piece.shape[row][col] == 1) {
                        val drawRow = currentY + row
                        val drawCol = currentX + col
                        if (drawRow >= 0 && drawRow < rows && drawCol >= 0 && drawCol < cols) {
                            canvas.drawRect(
                                drawCol * cellSize,
                                drawRow * cellSize,
                                (drawCol + 1) * cellSize,
                                (drawRow + 1) * cellSize,
                                paint
                            )
                        }
                    }
                }
            }
        }
    }

    fun update() {
        if (gameOver) return
        if (!moveDown()) {
            lockPiece()
            clearLines()
            spawnNewPiece()
        }
    }

    fun moveLeft() {
        if (canMove(currentX - 1, currentY)) {
            currentX--
            invalidate()
        }
    }

    fun moveRight() {
        if (canMove(currentX + 1, currentY)) {
            currentX++
            invalidate()
        }
    }

    fun moveDown(): Boolean {
        return if (canMove(currentX, currentY + 1)) {
            currentY++
            invalidate()
            true
        } else {
            false
        }
    }

    fun rotate() {
        currentPiece?.let { piece ->
            val rotated = piece.rotate()
            if (canMove(currentX, currentY, rotated)) {
                currentPiece = Piece(piece.type, rotated)
                invalidate()
            }
        }
    }

    private fun canMove(newX: Int, newY: Int, shape: Array<IntArray>? = null): Boolean {
        val pieceShape = shape ?: currentPiece?.shape ?: return false

        for (row in pieceShape.indices) {
            for (col in pieceShape[row].indices) {
                if (pieceShape[row][col] == 1) {
                    val gridRow = newY + row
                    val gridCol = newX + col

                    if (gridCol < 0 || gridCol >= cols || gridRow >= rows) {
                        return false
                    }
                    if (gridRow >= 0 && grid[gridRow][gridCol] > 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun lockPiece() {
        currentPiece?.let { piece ->
            for (row in piece.shape.indices) {
                for (col in piece.shape[row].indices) {
                    if (piece.shape[row][col] == 1) {
                        val gridRow = currentY + row
                        val gridCol = currentX + col
                        if (gridRow >= 0 && gridRow < rows && gridCol >= 0 && gridCol < cols) {
                            grid[gridRow][gridCol] = piece.type + 1
                        }
                    }
                }
            }
        }
    }

    private fun clearLines() {
        var clearedCount = 0
        for (row in rows - 1 downTo 0) {
            if (grid[row].all { it > 0 }) {
                // Shift all rows down
                for (r in row downTo 1) {
                    grid[r] = grid[r - 1].clone()
                }
                grid[0] = IntArray(cols)
                clearedCount++
                score += 100
            }
        }
        if (clearedCount > 0) {
            linesCleared += clearedCount
            score += clearedCount * 50 // Bonus for multiple lines
        }
    }

    private fun spawnNewPiece() {
        currentPiece = Piece.random()
        currentX = cols / 2 - 1
        currentY = 0

        if (!canMove(currentX, currentY)) {
            gameOver = true
        }
        invalidate()
    }

    fun getScore() = score
    fun getLinesCleared() = linesCleared
    fun isGameOver() = gameOver

    fun reset() {
        // Reset grid
        grid = Array(rows) { IntArray(cols) }
        score = 0
        linesCleared = 0
        gameOver = false
        currentX = 0
        currentY = 0
        spawnNewPiece()
        invalidate()
    }
}

class Piece(val type: Int, val shape: Array<IntArray>) {
    companion object {
        private val shapes = listOf(
            // I piece
            arrayOf(
                intArrayOf(1, 1, 1, 1)
            ),
            // J piece
            arrayOf(
                intArrayOf(1, 0, 0),
                intArrayOf(1, 1, 1)
            ),
            // L piece
            arrayOf(
                intArrayOf(0, 0, 1),
                intArrayOf(1, 1, 1)
            ),
            // O piece
            arrayOf(
                intArrayOf(1, 1),
                intArrayOf(1, 1)
            ),
            // S piece
            arrayOf(
                intArrayOf(0, 1, 1),
                intArrayOf(1, 1, 0)
            ),
            // T piece
            arrayOf(
                intArrayOf(0, 1, 0),
                intArrayOf(1, 1, 1)
            ),
            // Z piece
            arrayOf(
                intArrayOf(1, 1, 0),
                intArrayOf(0, 1, 1)
            )
        )

        fun random(): Piece {
            val type = (0 until shapes.size).random()
            return Piece(type, shapes[type])
        }
    }

    fun rotate(): Array<IntArray> {
        val rows = shape.size
        val cols = shape[0].size
        val rotated = Array(cols) { IntArray(rows) }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                rotated[col][rows - 1 - row] = shape[row][col]
            }
        }
        return rotated
    }
}