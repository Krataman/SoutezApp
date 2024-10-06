package com.example.soutezapp

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.utils.UIUtils

class TGMActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tgmactivity)
        UIUtils.hideSystemUI(this);

        var knoflik: ImageView = findViewById(R.id.a1);
        knoflik.setOnTouchListener(DragTouchListener());

        // Najdi hlavní layout
        val mainLayout: ImageView = findViewById(R.id.tgm_bez_knofliku)

        // Přidej OnTouchListener k 16 knoflíkům
        for (i in 1..16) {
            val buttonId = resources.getIdentifier("a$i", "id", packageName)
            val button: ImageView = findViewById(buttonId)

            // Nastav přetahování pro každý knoflík
            button.setOnTouchListener(DragTouchListener())
        }

    }

    private class DragTouchListener : View.OnTouchListener {

        private var dX = 0f
        private var dY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
            }
            return true
        }
    }
}