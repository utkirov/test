package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Parallax backgrounds, one per location. */
object Bg {
    private fun m(a: Float, b: Float) = ((a % b) + b) % b

    fun city(g: Game) {
        val cx = g.camX
        px("#9fd8f0", 0, 0, W, H); px("#bfe6f6", 0, 120, W, 60)
        for (i in -1 until 4) {
            val bx = (i * 120 - m(cx * 0.1f, 120f)).roundToInt().toFloat()
            G.tri("#a9c6e0", floatArrayOf(bx, bx + 40, bx + 70, bx + 95, bx + 140), floatArrayOf(170f, 110f, 140f, 100f, 170f))
            px("#eef5ff", bx + 36, 114, 8, 4); px("#eef5ff", bx + 91, 104, 8, 4)
        }
        val tw = (150 - m(cx * 0.15f, 400f)).roundToInt()
        px("#8aa9c4", tw, 70, 2, 100); px("#8aa9c4", tw - 3, 95, 8, 6); px("#8aa9c4", tw - 1, 60, 4, 12); px("#e24b4a", tw, 58, 2, 2)
        for (i in -1 until 4) {
            val hx = (i * 96 - m(cx * 0.4f, 96f)).roundToInt()
            px("#d9c09a", hx, 150, 52, 70); px("#c4a87f", hx, 148, 52, 3); px("#3aa0c8", hx + 8, 165, 10, 12); px("#3aa0c8", hx + 32, 165, 10, 12)
            px("#2a7f9f", hx + 8, 165, 10, 2); px("#2a7f9f", hx + 32, 165, 10, 2); px("#e8c440", hx, 186, 52, 2); px("#4f9a3a", hx + 2, 144, 30, 6); px("#68b34a", hx + 4, 142, 10, 3)
            px("#6b4a2a", hx + 72, 168, 4, 50); px("#3f8a33", hx + 60, 140, 28, 30); px("#58a643", hx + 64, 142, 14, 10)
        }
    }

    fun bazaar(g: Game) {
        val cx = g.camX
        px("#ffd9a8", 0, 0, W, H); px("#ffe8c8", 0, 90, W, 70)
        for (i in -1 until 3) {
            val dx = (i * 220 - m(cx * 0.15f, 220f)).roundToInt() + 40
            G.halfDisc("#2f7fc1", dx + 50, 150, 52)
            px("#86c5ef", dx + 20, 106, 4, 44); px("#86c5ef", dx + 48, 98, 4, 52); px("#86c5ef", dx + 76, 106, 4, 44)
            px("#e9d8b4", dx - 6, 150, 112, 50); for (k in 0 until 6) px("#2f7fc1", dx + 2 + k * 17, 160, 9, 14); px("#f4c430", dx + 48, 90, 4, 8)
        }
        val cols = arrayOf("#d8373b", "#f5f0e6", "#2e9b6b", "#f4c430")
        for (i in -1 until 5) {
            val sx = (i * 60 - m(cx * 0.45f, 60f)).roundToInt()
            for (k in 0 until 6) px(cols[(k + i + 10) % 4], sx + k * 8, 176, 8, 8)
            px("#8b5a2b", sx + 2, 184, 3, 40); px("#8b5a2b", sx + 42, 184, 3, 40); px("#c98a4a", sx, 198, 48, 8)
            for (q in 0 until 4) { px("#e9c94a", sx + 4 + q * 11, 191, 8, 7); px("#2e9b6b", sx + 6 + q * 11, 192, 2, 5) }
        }
    }

    fun metro(g: Game) {
        val cx = g.camX
        px("#1b2238", 0, 0, W, H); px("#24305a", 0, 40, W, 150)
        var y = 40; while (y < 190) { px("#2c3a6b", 0, y, W, 1); y += 10 }
        for (i in -1 until 5) {
            val x = (i * 64 - m(cx * 0.6f, 64f)).roundToInt()
            px("#e9e1cc", x, 30, 14, 170); px("#f4c430", x - 3, 30, 20, 6); px("#c9a227", x - 3, 36, 20, 2); px("#d6cfba", x + 5, 40, 4, 160)
        }
        for (i in -1 until 4) {
            val x = (i * 90 - m(cx * 0.6f, 90f)).roundToInt() + 38
            px("#c9a227", x + 7, 0, 1, 18)
            G.c.save(); G.c.clipRect(x - 2f, 24f, x + 18f, 34f); G.circle("#f4c430", x + 8, 24, 9); G.c.restore()
            for (k in 0 until 4) px("#fff6c8", x + 1 + k * 4, 26 + (k % 2) * 3, 2, 4)
            if (g.frame % 30 < 15) px("#fffbe6", x + 7, 30, 2, 2)
        }
        px("#0e1322", 0, 200, W, 20)
    }

    fun park(g: Game) {
        val cx = g.camX
        px("#a8dcf5", 0, 0, W, H); px("#c9ecfa", 0, 110, W, 60)
        val fx = (120 - m(cx * 0.12f, 360f)).roundToInt(); val fy = 110; val r = 50
        G.ring("#d8373b", fx, fy, r, 2f)
        for (k in 0 until 8) {
            val a = k / 8.0 * PI * 2 + g.frame / 400.0
            val ex = fx + cos(a) * r; val ey = fy + sin(a) * r
            G.line("#d8373b", fx, fy, ex, ey)
            px(arrayOf("#f4c430", "#2e6fd8", "#2e9b6b", "#f08a24")[k % 4], ex.roundToInt() - 3, ey.roundToInt() - 2, 7, 6)
        }
        px("#8a8f99", fx - 2, fy, 4, 80); px("#8a8f99", fx - 20, fy + 70, 40, 4)
        px("#5aa9d6", 0, 196, W, 10)
        for (i in 0 until 8) px("#cfeeff", m(i * 29 - g.frame / 2f, W.toFloat()), 199, 6, 1)
        for (i in -1 until 5) {
            val tx = (i * 50 - m(cx * 0.45f, 50f)).roundToInt()
            px("#6b4a2a", tx + 20, 160, 4, 40); px("#3f8a33", tx + 6, 136, 32, 28); px("#58a643", tx + 10, 138, 14, 10)
            if (abs(i) % 2 == 1) for (k in 0 until 5) px("#f7c6d9", tx + 8 + k * 6, 140 + (k % 3) * 6, 2, 2)
        }
    }

    fun tower(g: Game) {
        val cy = g.camY; val tot = g.rows * T - H; val prog = 1 - cy / tot
        px(if (prog < 0.35f) "#9fd8f0" else if (prog < 0.7f) "#7fb7e8" else "#5a86cf", 0, 0, W, H)
        for (i in 0 until 6) {
            val y = m(i * 90 - cy * 0.3f, 560f).roundToInt() - 60
            px("#eef5ff", (i * 53) % 140, y, 36, 8); px("#eef5ff", (i * 53) % 140 + 8, y - 6, 18, 6)
        }
        val cityY = (g.rows * T - cy - 10).roundToInt()
        if (cityY < H + 120) for (i in 0 until 10) { val h = 20 + hash(i, 9) % 40; px("#8aa0b8", i * 20, cityY - h, 18, h + 20); px("#cfe0ef", i * 20 + 4, cityY - h + 5, 4, 4) }
        var y = -m(cy * 0.7f, 24f)
        while (y < H) {
            px("#b9c6d6", 4, y, 2, 24); px("#b9c6d6", W - 6, y, 2, 24)
            G.alpha(0.25f) { G.line("#cfd8e4", 5, y, W - 5, y + 24); G.line("#cfd8e4", W - 5, y, 5, y + 24) }
            y += 24
        }
    }

    fun sky(g: Game) {
        val cx = g.camX
        px("#ffb36b", 0, 0, W, 70); px("#f08a6a", 0, 70, W, 50); px("#b0638f", 0, 120, W, 50); px("#5b3f86", 0, 170, W, 100); px("#ffe28a", 130, 40, 14, 14)
        for (i in -1 until 6) { val x = (i * 44 - m(cx * 0.15f, 44f)).roundToInt(); val h = 60 + hash(i + 20, 2) % 60; px("#3c3568", x, 200 - h, 34, h + 80) }
        for (i in -1 until 5) {
            val x = (i * 60 - m(cx * 0.4f, 60f)).roundToInt(); val h = 90 + hash(i + 50, 3) % 70
            px("#2a3f6a", x, 220 - h, 40, h + 60); px("#3f5f86", x, 220 - h, 40, 3)
            var r = 0; while (r < h) { for (c in 0 until 4) if (hash(i * 7 + c, r) % 3 != 0) px("#ffd56b", x + 4 + c * 9, 224 - h + r, 4, 5); r += 10 }
        }
    }

    fun draw(g: Game, i: Int) {
        when (i) { 0 -> city(g); 1 -> bazaar(g); 2 -> metro(g); 3 -> park(g); 4 -> tower(g); 5 -> sky(g); 6 -> samarkand(g); 7 -> bukhara(g); else -> khiva(g) }
    }

    private fun minaret(x: Int, top: Int, bot: Int, w: Int, body: String, band: String) {
        px(body, x, top, w, bot - top); var y = top + 6
        while (y < bot) { px(band, x, y, w, 2); y += 9 }
        px(band, x - 2, top - 4, w + 4, 4); px(body, x + 1, top - 10, w - 2, 6)
    }

    fun samarkand(g: Game) {
        val cx = g.camX
        px("#7fc4e8", 0, 0, W, H); px("#a8dcf0", 0, 100, W, 60); px("#e9d3a6", 0, 196, W, 80)
        // Registan: three madrasa facades with portals, domes and minarets
        for (i in -1 until 3) {
            val bx = (i * 150 - m(cx * 0.2f, 150f)).roundToInt()
            px("#d9b98a", bx, 120, 120, 80)
            for (k in 0 until 3) {
                val px0 = bx + 6 + k * 38
                px("#c9a77a", px0, 96, 32, 104); px("#2e6fd8", px0 + 4, 100, 24, 6)
                G.halfDisc("#1f4ea8", px0 + 16, 128, 10); px("#1f4ea8", px0 + 6, 128, 20, 40); px("#2fb3c9", px0 + 8, 130, 16, 2)
                for (q in 0 until 4) px("#2fb3c9", px0 + 2, 108 + q * 22, 2, 8)
                if (k == 1) { G.halfDisc("#2fb3c9", px0 + 16, 94, 14); px("#f4c430", px0 + 15, 76, 2, 6) }
            }
            minaret(bx + 1, 70, 200, 6, "#c9a77a", "#2e6fd8"); minaret(bx + 113, 70, 200, 6, "#c9a77a", "#2e6fd8")
        }
        for (i in -1 until 6) { val tx = (i * 40 - m(cx * 0.45f, 40f)).roundToInt(); px("#6b4a2a", tx + 18, 182, 3, 20); G.circle("#3f8a33", tx + 19, 180, 9) }
    }

    fun bukhara(g: Game) {
        val cx = g.camX
        px("#f6d9a0", 0, 0, W, H); px("#f9e6bf", 0, 90, W, 60)
        G.circle("#fff1c0", 140, 40, 12)
        // dunes
        for (i in -1 until 4) { val dx = (i * 110 - m(cx * 0.1f, 110f)).roundToInt(); G.halfDisc("#e8c48a", dx + 55, 200, 60) }
        // Kalon minaret + Poi-Kalon dome
        for (i in -1 until 2) {
            val bx = (i * 260 - m(cx * 0.2f, 260f)).roundToInt() + 60
            px("#d2a86e", bx, 70, 14, 130); var y = 76; while (y < 200) { px("#b8894e", bx, y, 14, 2); y += 7 }
            px("#c99a5a", bx - 3, 64, 20, 8); px("#d2a86e", bx + 2, 56, 10, 8)
            G.halfDisc("#2fb3c9", bx + 70, 150, 22); px("#d2a86e", bx + 44, 150, 52, 50); px("#b8894e", bx + 62, 166, 16, 34)
        }
        for (i in -1 until 5) { val hx = (i * 70 - m(cx * 0.4f, 70f)).roundToInt(); px("#d9b98a", hx, 170, 50, 40); G.halfDisc("#c9a77a", hx + 25, 170, 12); px("#8b6b4a", hx + 20, 186, 10, 14) }
    }

    fun khiva(g: Game) {
        val cx = g.camX
        px("#f4b06a", 0, 0, W, 90); px("#f7c88a", 0, 90, W, 80); px("#e0b07a", 0, 170, W, 100)
        // Ichan-Kala crenellated wall
        val wy = 160
        px("#c48e58", 0, wy, W, 60)
        for (i in -1 until 20) { val x = (i * 12 - m(cx * 0.35f, 12f)).roundToInt(); px("#c48e58", x, wy - 6, 7, 6); G.halfDisc("#b47e48", x + 3, wy - 6, 3) }
        for (i in -1 until 3) {
            val bx = (i * 200 - m(cx * 0.2f, 200f)).roundToInt() + 30
            // Kalta-Minor: wide unfinished turquoise-striped tower
            px("#2fb3c9", bx, 92, 34, 70); var y = 96; while (y < 162) { px("#1f8aa0", bx, y, 34, 3); px("#f4c430", bx, y + 6, 34, 1); y += 12 }
            px("#1f8aa0", bx - 2, 88, 38, 5)
            // Islam-Khodja: tall slim minaret
            minaret(bx + 110, 30, 162, 8, "#d2a86e", "#2fb3c9")
        }
    }

    @Suppress("unused") fun label(s: String, x: Float, y: Float) = txt(s, x, y, "#f4c430", SM)
}
