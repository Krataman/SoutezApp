package com.example.soutezapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

data class PuzzlePiece(
    val imageView: ImageView,
    val originalX: Float,
    val originalY: Float,
    val correctX: Float,
    val correctY: Float
)

class SkullActivity : AppCompatActivity() {

    private var selectedPiece: ImageView? = null
    private lateinit var gameBoard: FrameLayout
    private var dX = 0f
    private var dY = 0f

    val rows = Data.rows // rady
    val columms = Data.columms //
    var pieceSize = 0 // Velikost jednoho puclíku
    private val puzzlePiecesList = mutableListOf<PuzzlePiece>() // Seznam puclíků

    //region onCreate
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_skull)

        calculateScreenWidth()

        // Načti obrázek z drawable
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.skull)

        // Rozděl obrázek na puclíky
        val puzzlePieces = splitImage(originalBitmap, rows, columms)
        val puzzlePiecesContainer1 = findViewById<LinearLayout>(R.id.puzzlePiecesContainer1)
        val puzzlePiecesContainer2 = findViewById<LinearLayout>(R.id.puzzlePiecesContainer2)

        val scrollView1 = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val scrollView2 = findViewById<HorizontalScrollView>(R.id.horizontalScrollView2)

        gameBoard = findViewById(R.id.gameBoard)

        // Nastavení rozměrů hracího pole
        val boardLayoutParams = gameBoard.layoutParams
        boardLayoutParams.width = rows * pieceSize
        boardLayoutParams.height = columms * pieceSize
        gameBoard.layoutParams = boardLayoutParams

        for ((index, pieceData) in puzzlePieces.withIndex()) {
            val imageView = ImageView(this)
            val pieceBitmap = pieceData.first // The Bitmap
            val correctPosition = pieceData.second // The correct position (Pair<Float, Float>)

            imageView.setImageBitmap(pieceBitmap)

            // The correct X and Y positions from the shuffled list
            val correctX = correctPosition.first
            val correctY = correctPosition.second

            val originalX: Float
            val originalY: Float

            // Distribute pieces into containers
            if (index < puzzlePieces.size / 2) {
                originalX = index * (pieceSize + 20.5f)
                originalY = 10f
                puzzlePiecesContainer1.addView(imageView)
            } else {
                originalX = (index - puzzlePieces.size / 2) * (pieceSize + 20.5f)
                originalY = 10f
                puzzlePiecesContainer2.addView(imageView)
            }

            // Add the piece data (with shuffled correct positions) to the list
            puzzlePiecesList.add(PuzzlePiece(imageView, originalX, originalY, correctX, correctY))

            // Set the layout parameters and touch listener
            val layoutParams = LinearLayout.LayoutParams(pieceSize, pieceSize)
            layoutParams.setMargins(10, 10, 10, 20)
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.adjustViewBounds = true
            imageView.elevation = 4f

            imageView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        scrollView1.requestDisallowInterceptTouchEvent(true)
                        scrollView2.requestDisallowInterceptTouchEvent(true)

                        dX = v.x - event.rawX
                        dY = v.y - event.rawY
                        selectedPiece = v as ImageView
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        selectedPiece?.let {
                            val newX = event.rawX + dX
                            val newY = event.rawY + dY
                            it.x = newX
                            it.y = newY
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        handlePieceDrop(event)
                        scrollView1.requestDisallowInterceptTouchEvent(false)
                        scrollView2.requestDisallowInterceptTouchEvent(false)
                        true
                    }
                    else -> false
                }
            }
        }
    }
    //endregion

    //region splitImage

    // Funkce pro rozdělení obrázku na řádky a sloupce
    private fun splitImage(image: Bitmap, rows: Int, cols: Int): List<Pair<Bitmap, Pair<Float, Float>>> {
        val pieceWidth = image.width / cols
        val pieceHeight = image.height / rows
        val pieces = mutableListOf<Pair<Bitmap, Pair<Float, Float>>>()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val piece = Bitmap.createBitmap(
                    image,
                    col * pieceWidth,
                    row * pieceHeight,
                    pieceWidth,
                    pieceHeight
                )
                // Pair the piece with its correct (X, Y) position
                pieces.add(Pair(piece, Pair(col * pieceSize.toFloat(), row * pieceSize.toFloat())))
            }
        }
        pieces.shuffle() // Shuffle both the pieces and their positions
        return pieces
    }

    //endregion

    //region handlePieceDrop
    private fun handlePieceDrop(event: MotionEvent) {
        if (selectedPiece != null) {
            val x = event.rawX
            val y = event.rawY

            val location = IntArray(2)
            gameBoard.getLocationOnScreen(location)

            // Zkontrolujeme, zda je puclík umístěn v hracím poli
            if (x >= location[0] && x <= (location[0] + gameBoard.width) && y >= location[1] && y <= (location[1] + gameBoard.height)) {
                val column = ((x - location[0]) / pieceSize).toInt()
                val row = ((y - location[1]) / pieceSize).toInt()

                // Správné snapování pozic
                val snappedX = column * pieceSize
                val snappedY = row * pieceSize

                // Najděte datový objekt puclíku
                val puzzlePieceData = puzzlePiecesList.find { it.imageView == selectedPiece }

                // Zkontrolujte, zda je puclík umístěn na správné pozici
                if (puzzlePieceData != null && snappedX == puzzlePieceData.correctX.toInt() && snappedY == puzzlePieceData.correctY.toInt()) {
                    // Přidání puclíku na hrací pole
                    val gamePiece = ImageView(this)
                    gamePiece.setImageBitmap((selectedPiece!!.drawable as BitmapDrawable).bitmap)
                    gamePiece.layoutParams = FrameLayout.LayoutParams(pieceSize, pieceSize)
                    gamePiece.elevation = 10f

                    gamePiece.x = snappedX.toFloat()
                    gamePiece.y = snappedY.toFloat()

                    gameBoard.addView(gamePiece)
                } else {
                    // Pokud není umístěn správně, vraťte puclík zpět na jeho původní pozici
                    puzzlePieceData?.let {
                        selectedPiece!!.animate()
                            .x(it.originalX)
                            .y(it.originalY)
                            .setDuration(200)
                            .start()
                    }
                }
            } else {
                // Pokud puclík není uvnitř hracího pole, vraťte ho na původní pozici
                val puzzlePieceData = puzzlePiecesList.find { it.imageView == selectedPiece }
                puzzlePieceData?.let {
                    selectedPiece!!.animate()
                        .x(it.originalX)
                        .y(it.originalY)
                        .setDuration(200)
                        .start()
                }
            }
            selectedPiece = null
        }
    }
    //endregion

    //region screenWidth
    private fun calculateScreenWidth() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        // Get the width of the screen
        val screenWidth = displayMetrics.widthPixels
        pieceSize = screenWidth / 6
    }
    //endregion
}
