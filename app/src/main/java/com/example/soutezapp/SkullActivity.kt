package com.example.soutezapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop

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
    private val puzzlePiecesList = mutableListOf<PuzzlePiece>() // Seznam puclíků

    //region onCreate
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //screenHeight(this)
        enableEdgeToEdge()

        setContentView(R.layout.activity_skull)


        hideSystemUI()

        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.kelt)
        val puzzlePieces = splitImage(originalBitmap, rows, columms)

        gameBoard = findViewById(R.id.gameBoard)
        val puzzlePiecesContainer = findViewById<GridLayout>(R.id.puzzlePiecesContainer)

        // Update GridLayout row and column counts based on the number of pieces
        puzzlePiecesContainer.rowCount = rows // Set the correct row count
        puzzlePiecesContainer.columnCount = columms // Set the correct column count

        // Adjust game board size
        val boardLayoutParams = gameBoard.layoutParams
        boardLayoutParams.width = (Data.widthPX * columms)
        boardLayoutParams.height = (Data.heightPX * rows)
        gameBoard.layoutParams = boardLayoutParams

        // Add puzzle pieces to GridLayout
        for ((index, pieceData) in puzzlePieces.withIndex()) {
            val imageView = ImageView(this)
            val pieceBitmap = pieceData.first
            val correctPosition = pieceData.second

            imageView.setImageBitmap(pieceBitmap)

            val originalX: Float = index * (0f)
            val originalY: Float = 0f

            // Set layout parameters for GridLayout
            val layoutParams = GridLayout.LayoutParams().apply {
                width = Data.widthPX
                height = Data.heightPX

                // Ensure we do not exceed the defined row count
                val rowIndex = index / columms
                if (rowIndex >= rows) {
                    throw IllegalArgumentException("Row index $rowIndex exceeds the row count $rows.")
                }
                rowSpec = GridLayout.spec(rowIndex) // Calculate row based on index

                // Ensure we do not exceed the defined column count
                val columnIndex = index % columms
                if (columnIndex >= columms) {
                    throw IllegalArgumentException("Column index $columnIndex exceeds the column count $columms.")
                }
                columnSpec = GridLayout.spec(columnIndex) // Calculate column based on index
            }

            imageView.layoutParams = layoutParams
            puzzlePiecesContainer.addView(imageView)

            puzzlePiecesList.add(PuzzlePiece(imageView, originalX, originalY, correctPosition.first, correctPosition.second))

            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.adjustViewBounds = true
            imageView.elevation = 4f

            imageView.setOnTouchListener { v, event ->
                handleTouch(event, v as ImageView) // Use a method to handle touch
            }
        }
    }
    //endregion

    //region splitImage

    // Funkce pro rozdělení obrázku na řádky a sloupce
    private fun splitImage(image: Bitmap, rows: Int, cols: Int): List<Pair<Bitmap, Pair<Float, Float>>> {
        val pieceWidth = image.width / Data.rows
        val pieceHeight = image.height / Data.columms
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
                pieces.add(Pair(piece, Pair(col * Data.widthPX.toFloat(), row * Data.heightPX.toFloat())))
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
                val column = ((x - location[1]) / Data.widthPX).toInt()
                val row = ((y - location[1]) / Data.heightPX).toInt()

                // Správné snapování pozic
                val snappedX = column * Data.widthPX
                val snappedY = row * Data.heightPX

                // Najděte datový objekt puclíku
                val puzzlePieceData = puzzlePiecesList.find { it.imageView == selectedPiece }

                // Zkontrolujte, zda je puclík umístěn na správné pozici
                if (puzzlePieceData != null && snappedX == puzzlePieceData.correctX.toInt() && snappedY == puzzlePieceData.correctY.toInt()) {
                    // Přidání puclíku na hrací pole
                    val gamePiece = ImageView(this)
                    gamePiece.setImageBitmap((selectedPiece!!.drawable as BitmapDrawable).bitmap)
                    gamePiece.layoutParams = FrameLayout.LayoutParams(Data.widthPX, Data.heightPX)
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

    private fun handleTouch(event: MotionEvent, imageView: ImageView): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = imageView.x - event.rawX
                dY = imageView.y - event.rawY
                selectedPiece = imageView
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                selectedPiece?.let {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    it.x = newX
                    it.y = newY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                handlePieceDrop(event)
                return true
            }
            else -> return false
        }
    }

    private fun screenHeight(context: Context) {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var displayHeight =  displayMetrics.heightPixels // Vrátí výšku displeje v pixelech

        Data.heightPX = displayHeight / 5

    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun snapAllPiecesToCorrectPositions() {
        for (piece in puzzlePiecesList) {
            val snappedX = piece.correctX.toFloat()
            val snappedY = piece.correctY.toFloat()

            // Umístění kousku na hrací plochu
            piece.imageView.animate()
                .x(snappedX)
                .y(snappedY)
                .setDuration(200)
                .start()
        }
    }


}
