package com.example.soutezapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.utils.UIUtils

data class PuzzlePiece(
    val imageView: ImageView,
    val originalX: Float,
    val originalY: Float,
    val correctX: Float,
    val correctY: Float
)

data class PuzzlePieceData(
    val imageView: ImageView,
    var snappedX: Float,
    var snappedY: Float
)

class SkullActivity : AppCompatActivity() {

    private var selectedPiece: ImageView? = null
    private lateinit var gameBoard: FrameLayout
    private lateinit var puzzlePiecesContainer: GridLayout
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
        screenSett(this)
        UIUtils.hideSystemUI(this);

        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.kelt)
        val puzzlePieces = splitImage(originalBitmap, rows, columms)

        gameBoard = findViewById(R.id.gameBoard)
        puzzlePiecesContainer = findViewById<GridLayout>(R.id.puzzlePiecesContainer)

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

            // Set original positions based on the grid indices
            val originalX = (index % columms) * Data.widthPX.toFloat() // Use column index to calculate original X
            val originalY = (index / columms) * Data.heightPX.toFloat() // Use row index to calculate original Y

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
    // List to keep track of snapped positions
    private val snappedPiecesList = mutableListOf<PuzzlePieceData>()

    private fun handlePieceDrop(event: MotionEvent) {
        if (selectedPiece != null) {
            val x = event.rawX
            val y = event.rawY

            val location = IntArray(2)
            gameBoard.getLocationOnScreen(location)

            // Zkontrolujeme, zda je puclík umístěn v hracím poli
            if (x >= location[0] && x <= (location[0] + gameBoard.width) && y >= location[1] && y <= (location[1] + gameBoard.height)) {
                // Správné snapování pozic
                val snappedX = ((x - location[0]) / Data.widthPX).toInt() * Data.widthPX
                val snappedY = ((y - location[1]) / Data.heightPX).toInt() * Data.heightPX

                // Přesune puclík na pozici, kde byl uvolněn
                selectedPiece!!.animate()
                    .x(snappedX.toFloat())
                    .y(snappedY.toFloat())
                    .setDuration(200)
                    .start()

                // Pokud je puclík již na herní desce, aktualizujte jeho pozici v snappedPiecesList
                val snappedPieceData = snappedPiecesList.find { it.imageView == selectedPiece }
                if (snappedPieceData != null) {
                    // Aktualizace pozice
                    snappedPieceData.snappedX = snappedX.toFloat()
                    snappedPieceData.snappedY = snappedY.toFloat()
                } else {
                    // Přidání puclíku na hrací plochu
                    if (selectedPiece!!.parent != gameBoard) {
                        // Odstraníme puclík z původní polohy, pokud není již v herním poli
                        val parentView = selectedPiece!!.parent as? ViewGroup
                        parentView?.removeView(selectedPiece)

                        // Přidání puclíku na hrací plochu
                        val layoutParams = FrameLayout.LayoutParams(Data.widthPX, Data.heightPX)
                        selectedPiece!!.layoutParams = layoutParams
                        selectedPiece!!.elevation = 10f
                        gameBoard.addView(selectedPiece)

                        // Uložíme snapnutou pozici kousku
                        snappedPiecesList.add(PuzzlePieceData(selectedPiece!!, snappedX.toFloat(), snappedY.toFloat()))
                    }
                }

                selectedPiece = null // Nastavit selectedPiece na null, aby se zamezilo dalšímu zpracování
            } else {
                // Pokud puclík není uvnitř hracího pole, vraťte ho na snapnutou pozici
                val snappedPieceData = snappedPiecesList.find { it.imageView == selectedPiece }
                if (snappedPieceData != null) {
                    // Vrať puclík na snapnutou pozici
                    selectedPiece!!.animate()
                        .x(snappedPieceData.snappedX)
                        .y(snappedPieceData.snappedY)
                        .setDuration(200)
                        .start()
                } else {
                    // Pokud není snapnutý, vrať ho na původní pozici
                    val puzzlePieceData = puzzlePiecesList.find { it.imageView == selectedPiece }
                    puzzlePieceData?.let {
                        selectedPiece!!.animate()
                            .x(it.originalX)
                            .y(it.originalY)
                            .setDuration(200)
                            .start()
                    }
                }
            }
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

    private fun screenSett(context: Context) {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var displayHeight = displayMetrics.heightPixels // Výška displeje v pixelech

        val widthRatio = 0.74f // Poměr, jak velká má být šířka vůči výšce, například 1.5x výška

// Nastav herní desku na nejbližší výšku dělitelné 5 a přepočítej šířku
        Data.heightPX = nearestMultipleOfFive(displayHeight) / 5
        Data.widthPX = (Data.heightPX * widthRatio).toInt()

    }

    private fun nearestMultipleOfFive(number: Int): Int {
        val remainder = number % 5
        return if (remainder != 0) {
            // Pokud je číslo liché, přidejte nebo odečtěte potřebnou hodnotu, aby se dostalo na nejbližší číslo dělitelné 5
            if (number % 2 != 0) {
                if (remainder < 3) {
                    number - remainder // Odebíráme zbytek, abychom se dostali na nižší číslo dělitelné 5
                } else {
                    number + (5 - remainder) // Přidáváme potřebnou hodnotu k dosažení vyššího čísla dělitelného 5
                }
            } else {
                number // Pokud je číslo sudé, vraťte ho beze změny
            }
        } else {
            number // Pokud je číslo již dělitelné 5, vraťte ho
        }
    }

}
