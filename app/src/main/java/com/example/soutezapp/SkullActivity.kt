package com.example.soutezapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
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
    val originalY: Float
)

class SkullActivity : AppCompatActivity() {

    private var selectedPiece: ImageView? = null
    private lateinit var gameBoard: FrameLayout
    private var dX = 0f
    private var dY = 0f

    val gridSize = 4 // Počet řádků a sloupců
    val pieceSize = 200 // Velikost jednoho puclíku
    private val puzzlePiecesList = mutableListOf<PuzzlePiece>() // Seznam puclíků

    //region onCreate
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_skull)

        // Načti obrázek z drawable
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.skull)

        // Rozděl obrázek na puclíky
        val puzzlePieces = splitImage(originalBitmap, gridSize, gridSize)
        val puzzlePiecesContainer = findViewById<LinearLayout>(R.id.puzzlePiecesContainer)

        gameBoard = findViewById(R.id.gameBoard)

        // Dynamicky nastav velikost hracího pole podle počtu puclíků a jejich velikosti
        val boardLayoutParams = gameBoard.layoutParams
        boardLayoutParams.width = gridSize * pieceSize
        boardLayoutParams.height = gridSize * pieceSize
        gameBoard.layoutParams = boardLayoutParams

        for (piece in puzzlePieces) {
            val imageView = ImageView(this)
            imageView.setImageBitmap(piece)

            // Uložení původní pozice
            val originalX = (pieceSize * puzzlePieces.indexOf(piece)) + 10 * puzzlePieces.indexOf(piece)
            val originalY = 0f // nebo nastavte na nějakou vhodnou hodnotu

            puzzlePiecesList.add(PuzzlePiece(imageView, originalX.toFloat(), originalY))

            // Nastavíme čtvercovou velikost dílku
            val layoutParams = LinearLayout.LayoutParams(pieceSize, pieceSize)
            layoutParams.setMargins(10, 10, 10, 10)
            imageView.layoutParams = layoutParams

            // Ujistíme se, že obraz se správně vejde do čtvercového view
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.adjustViewBounds = true
            imageView.elevation = 4f

            imageView.setOnTouchListener { v, event ->
                when (event.action) {
                    //region ACTION_DOWN
                    MotionEvent.ACTION_DOWN -> {
                        (puzzlePiecesContainer.parent as HorizontalScrollView).requestDisallowInterceptTouchEvent(true)
                        dX = v.x - event.rawX
                        dY = v.y - event.rawY
                        selectedPiece = v as ImageView
                        true
                    }
                    //endregion
                    //region ACTION_MOVE
                    MotionEvent.ACTION_MOVE -> {
                        if (selectedPiece != null) {
                            val newX = event.rawX + dX
                            val newY = event.rawY + dY
                            selectedPiece!!.x = newX
                            selectedPiece!!.y = newY
                        }
                        true
                    }
                    //endregion
                    //region ACTION_UP
                    MotionEvent.ACTION_UP -> {
                        if (selectedPiece != null) {
                            val x = event.rawX
                            val y = event.rawY

                            val location = IntArray(2)
                            gameBoard.getLocationOnScreen(location)

                            // Zkontrolujeme, zda je puclík umístěn v hracím poli
                            if (x >= location[0] && x <= (location[0] + gameBoard.width) &&
                                y >= location[1] && y <= (location[1] + gameBoard.height)) {

                                val column = ((x - location[0]) / pieceSize).toInt()
                                val row = ((y - location[1]) / pieceSize).toInt()

                                val snappedX = location[0] + column * pieceSize
                                val snappedY = location[1] + row * pieceSize

                                val gamePiece = ImageView(this)
                                gamePiece.setImageBitmap((selectedPiece!!.drawable as BitmapDrawable).bitmap)
                                gamePiece.layoutParams = FrameLayout.LayoutParams(pieceSize, pieceSize)
                                gamePiece.elevation = 10f

                                gamePiece.x = snappedX.toFloat()
                                gamePiece.y = snappedY.toFloat()

                                gameBoard.addView(gamePiece)
                            } else {
                                // Najdeme původní pozici puclíku
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
                        true
                    }
                    //endregion
                    else -> false
                }
            }
            puzzlePiecesContainer.addView(imageView)
        }
    }
    //endregion
    //region splitImage
    // Funkce pro rozdělení obrázku na řádky a sloupce
    private fun splitImage(image: Bitmap, rows: Int, cols: Int): List<Bitmap> {
        val pieceWidth = image.width / cols
        val pieceHeight = image.height / rows
        val pieces = mutableListOf<Bitmap>()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val piece = Bitmap.createBitmap(
                    image,
                    col * pieceWidth,
                    row * pieceHeight,
                    pieceWidth,
                    pieceHeight
                )
                pieces.add(piece)
            }
        }
        return pieces
    }
    //endregion
}
