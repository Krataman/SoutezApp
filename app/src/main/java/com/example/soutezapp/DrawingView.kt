package com.example.soutezapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var path = Path() // For drawing lines
    private var paint = Paint() // To define the style of the drawing
    private var bitmap: Bitmap? = null // The image over which the user can draw
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    private var imageMatrix = Matrix() // For scaling and centering the image

    init {
        // Setup paint properties
        paint.color = Color.RED // Red color for drawing
        paint.isAntiAlias = true
        paint.strokeWidth = 10f // Thickness of the line
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
    }

    // This method allows setting an image to be drawn over
    fun setImage(image: Bitmap) {
        bitmap = image
        invalidate() // Redraw the view
    }

    // Called when the size of the view changes (e.g., when first displayed)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)

        // If a bitmap is set, calculate its scaled size and center it
        bitmap?.let {
            val scaleX = w.toFloat() / it.width
            val scaleY = h.toFloat() / it.height
            val scale = Math.min(scaleX, scaleY) // Maintain aspect ratio

            val offsetX = (w - it.width * scale) / 2f
            val offsetY = (h - it.height * scale) / 2f

            // Apply scaling and translation to center the image
            imageMatrix.reset()
            imageMatrix.postScale(scale, scale)
            imageMatrix.postTranslate(offsetX, offsetY)
        }
    }

    // Called to draw the content on the screen
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // First, draw the bitmap (if available) on the canvas using the matrix
        bitmap?.let {
            canvas.drawBitmap(it, imageMatrix, null)
        }

        // Draw the canvas bitmap where previous drawings are saved
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, null)

        // Then, draw the path that the user has drawn
        canvas.drawPath(path, paint)
    }

    // Handle touch events to draw on the view
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(touchX, touchY) // Start a new path
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(touchX, touchY) // Draw lines as the user moves their finger
            }
            MotionEvent.ACTION_UP -> {
                // Draw the path onto the canvas bitmap when the touch is released
                drawCanvas?.drawPath(path, paint)
                path.reset() // Reset the path for the next draw
            }
        }

        invalidate() // Redraw the view
        return true
    }

    // Clear the drawing
    fun clearDrawing() {
        path.reset() // Clear the path
        drawCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // Clear the canvas
        invalidate() // Redraw the view
    }
}