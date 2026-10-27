package uz.botir.game

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Developer tools, only reachable with an intent extra (used by the CI emulator job):
 *  - selfTest: a simple bot plays every level, the endless mode and every menu, and reports crashes.
 *  - promo: renders Google Play screenshots, the 512px icon and the 1024x500 feature graphic.
 */
object Tools {
    private const val TAG = "BOTIR_TOOLS"

    fun selfTest(a: Activity, v: GameView) {
        Thread {
            val g = v.game
            val bmp = Bitmap.createBitmap(W * 3, H * 3, Bitmap.Config.ARGB_8888)
            try { Thread.sleep(1500) } catch (_: Throwable) { }
            val result = synchronized(g) { AndroidPlatform.gfx.cv = Canvas(bmp); SelfTest.run(g) }
            try { File(a.filesDir, "selftest.txt").writeText(result) } catch (_: Throwable) { }
            result.lines().forEach { Log.i(TAG, it) }
            a.runOnUiThread { a.finish() }
        }.start()
    }

    private fun save(b: Bitmap, f: File) { FileOutputStream(f).use { b.compress(Bitmap.CompressFormat.PNG, 100, it) } }

    fun promo(a: Activity, v: GameView) {
        Thread {
            try { Thread.sleep(1500) } catch (_: Throwable) { }
            val dir = File(a.filesDir, "promo").apply { mkdirs() }
            val g = v.game
            val shot = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            fun scene(name: String, setup: () -> Unit, frames: Int = 40) {
                try {
                    synchronized(g) { setup(); repeat(frames) { g.update() }; g.kL = false; g.kR = false; g.kJ = false }
                    a.runOnUiThread { try { synchronized(g) { v.renderTo(shot) }; save(shot, File(dir, "$name.png")) } catch (t: Throwable) { Log.e(TAG, "render $name", t) } }
                    Thread.sleep(900)
                } catch (t: Throwable) { Log.e(TAG, "scene $name", t) }
            }
            fun place(i: Int, tx: Float, ty: Float) { g.play(i); g.toasts.clear(); g.pl.x = tx * T; g.pl.y = ty * T - 26; g.pl.safeX = g.pl.x; g.pl.safeY = g.pl.y; g.camX = g.pl.x - 80; g.camY = g.pl.y - 150 }
            scene("01_title", { g.goTitle() }, 90)
            scene("02_mahalla", { place(0, 27f, 11f); g.tip(g.L("Курпача-батут! Подбрасывает высоко.", "Ko'rpacha-batut! Baland otadi.")); g.kR = true }, 30)
            scene("03_chorsu", { place(1, 41f, 10f); g.kR = true }, 30)
            scene("04_anhor", { place(3, 22f, 15f); g.kR = true }, 50)
            scene("05_boss", { place(5, 99f, 14f); g.pl.inv = 9999 }, 160)
            scene("06_samarkand", { place(6, 14f, 14f); g.kR = true }, 30)
            scene("07_bukhara", { place(7, 13f, 14f); g.kR = true }, 30)
            scene("08_levels", { g.goTitle(); g.show("levels") }, 30)
            scene("09_shop", { g.goTitle(); g.shopTab = 0; g.show("shop") }, 30)
            // 512x512 icon
            try {
                val icon = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888); val c = Canvas(icon)
                synchronized(g) {
                    AndroidPlatform.gfx.cv = c; c.scale(16f, 16f)
                    G.px("#2e6fd8", 0, 0, 32, 32); G.circle("#f08a24", 16, 17, 14)
                    c.save(); c.translate(6f, 4f); c.scale(0.84f, 0.84f); Hero.draw("jump", 0, Skins.ALL[0], false); c.restore()
                }
                save(icon, File(dir, "icon_512.png"))
            } catch (t: Throwable) { Log.e(TAG, "icon", t) }
            // 1024x500 feature graphic
            try {
                val fg = Bitmap.createBitmap(1024, 500, Bitmap.Config.ARGB_8888); val c = Canvas(fg)
                synchronized(g) {
                    g.goTitle(); AndroidPlatform.gfx.cv = c; c.scale(500f / 270f, 500f / 270f)
                    for (k in 0 until 4) { c.save(); c.translate(k * 180f, 0f); g.camX = k * 180f; Bg.city(g); c.restore() }
                    G.px("#4f9a3a", 0, 214, 560, 6); G.px("#7cc44a", 0, 214, 560, 2); G.px("#b9773d", 0, 220, 560, 50)
                    for (k in 0 until 6) Spr.item("non", 40f + k * 16, 150f - (k % 2) * 8)
                    c.save(); c.translate(60f, 136f); c.scale(2.5f, 2.5f); Hero.draw("jump", 0, Skins.ALL[0], false); c.restore()
                    G.px("#140c08", 248, 38, 294, 124); G.px("#1d2b53", 250, 40, 290, 120); G.px("#f4c430", 250, 40, 290, 3)
                    G.txt("BOTIR", 395f, 56f, "#f4c430", 32f, 1)
                    G.txt("O'zbekiston sarguzashtlari", 395f, 102f, "#fff1e8", 8f, 1)
                    G.txt("Приключения в Узбекистане", 395f, 120f, "#a8b3d9", 8f, 1)
                    c.save(); c.translate(360f, 132f); c.scale(2f, 2f); Spr.item("qozon", 0f, 0f); Spr.item("kafgir", 20f, 2f); Spr.item("lagan", 40f, 0f); c.restore()
                }
                save(fg, File(dir, "feature_1024x500.png"))
            } catch (t: Throwable) { Log.e(TAG, "feature", t) }
            File(dir, "done.txt").writeText("ok")
            Log.i(TAG, "PROMO DONE")
            a.runOnUiThread { a.finish() }
        }.start()
    }
}
