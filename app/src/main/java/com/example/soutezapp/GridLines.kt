package com.example.soutezapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridLinesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()

    init {
        paint.color = Color.BLACK // Barva mřížky
        paint.strokeWidth = 5f // Tloušťka čar
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val numColumns = Data.columms
        val numRows = Data.rows

        val cellWidth = width / numColumns
        val cellHeight = height / numRows

        // Vykreslení svislých čar
        for (i in 1 until numColumns) {
            canvas.drawLine((i * cellWidth).toFloat(), 0f, (i * cellWidth).toFloat(), height.toFloat(), paint)
        }

        // Vykreslení vodorovných čar
        for (i in 1 until numRows) {
            canvas.drawLine(0f, (i * cellHeight).toFloat(), width.toFloat(), (i * cellHeight).toFloat(), paint)
        }

        // Vykreslení okrajových čar
        // Horní okraj
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paint)
        // Spodní okraj
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), paint)
        // Levý okraj
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        // Pravý okraj
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), paint)
    }
}
