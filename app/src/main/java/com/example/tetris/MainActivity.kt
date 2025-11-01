package com.example.tetris

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: TetrisView
    private lateinit var scoreText: TextView
    private lateinit var linesText: TextView
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var gameOverCard: CardView
    private lateinit var finalScoreText: TextView
    private lateinit var finalLinesText: TextView
    private lateinit var restartButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var gameRunnable: Runnable? = null
    private var isPaused = false
    private var gameSpeed = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        gameView = findViewById(R.id.gameView)
        scoreText = findViewById(R.id.scoreText)
        linesText = findViewById(R.id.linesText)
        pauseButton = findViewById(R.id.pauseButton)
        resetButton = findViewById(R.id.resetButton)
        gameOverCard = findViewById(R.id.gameOverCard)
        finalScoreText = findViewById(R.id.finalScoreText)
        finalLinesText = findViewById(R.id.finalLinesText)
        restartButton = findViewById(R.id.restartButton)

        val leftButton: Button = findViewById(R.id.leftButton)
        val rightButton: Button = findViewById(R.id.rightButton)
        val rotateButton: Button = findViewById(R.id.rotateButton)
        val downButton: Button = findViewById(R.id.downButton)

        // Control buttons
        leftButton.setOnClickListener { gameView.moveLeft() }
        rightButton.setOnClickListener { gameView.moveRight() }
        rotateButton.setOnClickListener { gameView.rotate() }
        downButton.setOnClickListener { gameView.moveDown() }

        pauseButton.setOnClickListener { togglePause() }
        resetButton.setOnClickListener { resetGame() }
        restartButton.setOnClickListener { resetGame() }

        startGame()
    }

    private fun startGame() {
        gameRunnable = object : Runnable {
            override fun run() {
                if (!isPaused && !gameView.isGameOver()) {
                    gameView.update()
                    updateUI()

                    if (gameView.isGameOver()) {
                        showGameOver()
                        return
                    }
                }
                handler.postDelayed(this, gameSpeed)
            }
        }
        handler.post(gameRunnable!!)
    }

    private fun updateUI() {
        scoreText.text = gameView.getScore().toString()
        linesText.text = gameView.getLinesCleared().toString()

        // Increase speed based on lines cleared
        val lines = gameView.getLinesCleared()
        gameSpeed = when {
            lines >= 30 -> 200L
            lines >= 20 -> 300L
            lines >= 10 -> 400L
            else -> 500L
        }
    }

    private fun togglePause() {
        isPaused = !isPaused
        pauseButton.text = if (isPaused) "RESUME" else "PAUSE"

        if (isPaused) {
            pauseButton.setBackgroundResource(R.drawable.button_primary)
        } else {
            pauseButton.setBackgroundResource(R.drawable.button_secondary)
        }
    }

    private fun resetGame() {
        // Remove callbacks
        gameRunnable?.let { handler.removeCallbacks(it) }

        // Reset game state
        isPaused = false
        gameSpeed = 500L
        pauseButton.text = "PAUSE"
        pauseButton.setBackgroundResource(R.drawable.button_secondary)

        // Hide game over screen
        gameOverCard.visibility = View.GONE

        // Reset game view
        gameView.reset()

        // Reset UI
        scoreText.text = "0"
        linesText.text = "0"

        // Restart game loop
        startGame()
    }

    private fun showGameOver() {
        finalScoreText.text = "Score: ${gameView.getScore()}"
        finalLinesText.text = "Lines: ${gameView.getLinesCleared()}"
        gameOverCard.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        gameRunnable?.let { handler.removeCallbacks(it) }
    }
}