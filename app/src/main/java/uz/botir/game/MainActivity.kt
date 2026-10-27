package uz.botir.game

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager

class MainActivity : Activity() {
    private lateinit var view: GameView
    private var tool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        view = GameView(this)
        setContentView(view)
        hideBars()
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                if (!view.game.back()) finish()
            }
        }
        // Developer modes used by the CI emulator: self-test and store screenshots.
        when (intent?.getStringExtra("mode")) {
            "selftest" -> { tool = true; Tools.selfTest(this, view) }
            "promo" -> { tool = true; Tools.promo(this, view) }
            "play" -> view.game.play(0)
        }
    }

    @Suppress("DEPRECATION")
    private fun hideBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) hideBars() }

    override fun onResume() { super.onResume(); if (!tool) { view.start(); Music.start() } }

    override fun onPause() {
        super.onPause()
        if (view.game.state == "play") view.game.show("pause")
        view.game.kL = false; view.game.kR = false; view.game.kJ = false
        view.stop(); Music.stop()
    }

    override fun onDestroy() { super.onDestroy(); Sfx.release() }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() { if (!view.game.back()) super.onBackPressed() }
}
