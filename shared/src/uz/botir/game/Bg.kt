package uz.botir.game

import uz.botir.game.G.circle
import uz.botir.game.G.halfDisc
import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Layered parallax backgrounds for every world. Static layers are painted once into cached strips
 * (Plat.gfx.cached) and scrolled; animated details (clouds, birds, trains, wheels, lights) are drawn live.
 */
object Bg {
    private fun m(a: Float, b: Float) = ((a % b) + b) % b

    // ---------- building blocks ----------
    /** Vertical gradient with dithered seams, cached per key. */
    private fun sky(key: String, stops: Array<String>, h: Int = H) {
        Plat.gfx.cached("bg:sky:$key", W, h, 0f, 0f) {
            val n = stops.size - 1; val band = h.toFloat() / n
            for (i in 0 until n) {
                val steps = 6
                for (k in 0 until steps) {
                    val c = G.mix(stops[i], stops[i + 1], k / steps.toFloat())
                    val y0 = i * band + k * band / steps
                    px(c, 0, y0.roundToInt(), W, (band / steps).roundToInt() + 1)
                    if (k > 0) { val pc = G.mix(stops[i], stops[i + 1], (k - 1) / steps.toFloat()); var x = (y0.roundToInt() % 2); while (x < W) { px(pc, x, y0.roundToInt(), 1, 1); x += 2 } }
                }
            }
        }
    }

    /** A horizontally tiling strip of width w, painted once, scrolled at [speed]. */
    private fun strip(key: String, w: Int, h: Int, y: Float, camX: Float, speed: Float, paint: () -> Unit) {
        val off = m(camX * speed, w.toFloat()).roundToInt()
        var x = -off
        while (x < W) { Plat.gfx.cached("bg:$key", w, h, x.toFloat(), y, paint); x += w }
    }

    private fun cloud(x: Float, y: Float, s: Int, col: String = "#ffffff", shade: String = "#dcecf6") {
        Plat.gfx.cached("bg:cloud:$s:$col", 16 * s, 7 * s, x, y) {
            val r = s * 2f
            circle(shade, 4 * s, 4.5f * s, r); circle(shade, 8 * s, 4.5f * s, r * 1.2f); circle(shade, 12 * s, 4.5f * s, r)
            circle(col, 4 * s, 4f * s, r); circle(col, 8 * s, 3.3f * s, r * 1.35f); circle(col, 12 * s, 4f * s, r * 1.05f)
            px(col, 3 * s, 4 * s, 10 * s, 2 * s)
        }
    }

    private fun clouds(frame: Int, camX: Float, y0: Int, n: Int, col: String = "#ffffff", shade: String = "#dcecf6") {
        for (i in 0 until n) {
            val s = 1 + (hash(i, 5) % 2)
            val span = W + 60f
            val x = m(hash(i, 6) % 300 - frame * (0.05f + 0.03f * s) - camX * 0.05f * s, span) - 40
            cloud(x, (y0 + hash(i, 7) % 40).toFloat(), s, col, shade)
        }
    }

    private fun birds(frame: Int, y: Int) {
        for (i in 0 until 4) {
            val x = m(hash(i, 9) % 200 + frame * 0.6f, W + 40f) - 20; val yy = y + hash(i, 10) % 20 + sin(frame / 20f + i) * 3
            val up = ((frame / 8 + i) % 2) == 0
            px("#2b2f3a", x, yy, 1, 1); px("#2b2f3a", x - 2, yy + (if (up) -1 else 1), 2, 1); px("#2b2f3a", x + 1, yy + (if (up) -1 else 1), 2, 1)
        }
    }

    private fun sun(x: Int, y: Int, r: Int, c: String, glow: String) {
        G.alpha(0.25f) { circle(glow, x, y, r * 2.2f) }; G.alpha(0.4f) { circle(glow, x, y, r * 1.5f) }; circle(c, x, y, r)
    }

    private fun window(x: Int, y: Int, w: Int, h: Int, frame: String, glass: String) {
        px(frame, x - 1, y - 1, w + 2, h + 2); px(glass, x, y, w, h); px(G.shade(glass, 1.3f), x, y, w, 1); px(frame, x + w / 2, y, 1, h)
    }

    private fun dome(cx: Int, base: Int, r: Int, c: String, ribs: Boolean = true) {
        halfDisc(G.shade(c, 0.75f), cx, base, r); halfDisc(c, cx - 1, base, r - 1)
        if (ribs) for (k in -2..2) { val a = k * 0.32f; px(G.shade(c, 0.7f), cx + (sin(a) * r * 0.85f).roundToInt(), base - (cos(a) * r * 0.9f).roundToInt(), 1, (r * 0.8f).roundToInt()) }
        px(G.shade(c, 1.4f), cx - r / 2, base - r + 2, 2, r / 2)
        px("#c9a227", cx, base - r - 5, 1, 5); circle("#f4c430", cx, base - r - 5, 1)
    }

    private fun minaret(x: Int, top: Int, bot: Int, w: Int, body: String, band: String) {
        px(G.shade(body, 0.8f), x, top, w, bot - top); px(body, x + 1, top, w - 2, bot - top)
        var y = top + 6
        while (y < bot) { px(band, x, y, w, 2); px(G.shade(band, 1.3f), x, y, w, 1); y += 10 }
        px(band, x - 2, top - 4, w + 4, 4); px(G.shade(body, 0.8f), x - 1, top - 9, w + 2, 5); px(body, x, top - 9, w, 4)
        halfDisc(band, x + w / 2, top - 9, w / 2 + 1)
    }

    private fun poplar(x: Int, base: Int, h: Int, c: String) {
        px("#5a4030", x, base - h / 3, 2, h / 3)
        for (k in 0 until h * 2 / 3 step 3) { val ww = (4 + 3 * sin(k / (h * 0.66f) * PI).toFloat()).roundToInt(); px(if (k % 6 == 0) G.shade(c, 1.15f) else c, x + 1 - ww / 2, base - h + k, ww, 3) }
    }

    private fun tree(x: Int, base: Int, r: Int, c: String, blossom: String? = null) {
        px("#5a4030", x - 1, base - r - 4, 3, r + 4)
        circle(G.shade(c, 0.8f), x + 2, base - r - 4, r); circle(c, x - 2, base - r - 7, r * 0.9f); circle(G.shade(c, 1.2f), x - 3, base - r - 9, r * 0.5f)
        if (blossom != null) for (k in 0 until 10) { val h = hash(x + k, base); px(blossom, x - r + h % (2 * r), base - 2 * r - 6 + (h shr 5) % r, 1, 1) }
    }

    // ---------- worlds ----------
    fun draw(g: Game, i: Int) {
        when (i) { 0 -> city(g); 1 -> bazaar(g); 2 -> metro(g); 3 -> park(g); 4 -> tower(g); 5 -> skyCity(g); 6 -> samarkand(g); 7 -> bukhara(g); else -> khiva(g) }
    }

    fun city(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("city", arrayOf("#6fc0ec", "#9fd8f0", "#cfeefa", "#eaf7fb"))
        sun(150, 36, 9, "#fff6c8", "#fff0a0")
        clouds(f, cx, 20, 5)
        birds(f, 50)
        strip("city:mtn", 360, 90, 92f, cx, 0.06f) {  // Chimgan mountains
            val pts = intArrayOf(0, 60, 40, 22, 70, 40, 110, 8, 150, 36, 190, 18, 230, 44, 270, 14, 310, 38, 360, 60)
            val xs = FloatArray(pts.size / 2) { pts[it * 2].toFloat() }; val ys = FloatArray(pts.size / 2) { pts[it * 2 + 1].toFloat() }
            G.tri("#9db8d6", xs + floatArrayOf(360f, 0f), ys + floatArrayOf(90f, 90f))
            for (k in 1 until xs.size - 1) if (ys[k] < ys[k - 1] && ys[k] < ys[k + 1]) G.tri("#f4f8ff", floatArrayOf(xs[k] - 9, xs[k], xs[k] + 9), floatArrayOf(ys[k] + 12, ys[k], ys[k] + 12))
            px("#b4cbe2", 0, 70, 360, 20)
        }
        strip("city:tower", 420, 140, 40f, cx, 0.1f) {   // TV tower far away
            val x = 60; px("#8aa0be", x, 30, 3, 110); px("#8aa0be", x - 4, 60, 11, 6); px("#9fb3cf", x - 3, 61, 9, 3); px("#8aa0be", x - 1, 14, 5, 16); px("#e24b4a", x, 10, 3, 3)
            G.tri("#8aa0be", floatArrayOf(x - 12f, x + 1f, x + 1f), floatArrayOf(140f, 100f, 140f)); G.tri("#8aa0be", floatArrayOf(x + 2f, x + 2f, x + 15f), floatArrayOf(100f, 140f, 140f))
        }
        strip("city:houses", 288, 110, 128f, cx, 0.3f) {   // mahalla houses
            for (k in 0 until 3) {
                val x = k * 96
                poplar(x + 82, 100, 70, "#4f9a3a")
                px("#c9ae86", x + 2, 30, 66, 80); px("#e3c9a0", x + 3, 31, 64, 78); px("#b39670", x + 2, 28, 66, 3)
                for (r in 0 until 2) for (c in 0 until 3) window(x + 9 + c * 20, 40 + r * 26, 10, 13, "#2a7f9f", "#86c5ef")
                px("#8b5a2b", x + 2, 60, 66, 2); for (c in 0 until 9) px("#8b5a2b", x + 4 + c * 8, 54, 1, 6)        // balcony
                px("#e8c440", x + 2, 74, 66, 2); px("#e8c440", x + 60, 74, 2, 36); px("#b8960a", x + 2, 76, 66, 1)  // gas pipe
                px("#d9d9e2", x + 50, 20, 7, 7); px("#a7b0c2", x + 53, 27, 1, 3)                                        // satellite dish
                for (c in 0 until 8) { px("#4f9a3a", x + 4 + c * 8, 24 - (c % 2) * 2, 7, 5) }                           // grapevine
                px("#2457c5", x + 28, 84, 14, 26); px("#3a6fd8", x + 29, 85, 12, 24); px("#f4c430", x + 34, 96, 2, 2)   // blue gate
            }
        }
        strip("city:near", 240, 60, 186f, cx, 0.55f) {   // chinar trees + wall
            px("#b8956a", 0, 40, 240, 20); px("#d0ad80", 0, 40, 240, 2)
            for (k in 0 until 24) px("#a3825a", k * 10 + (k % 2) * 5, 46 + (k % 3) * 4, 6, 1)
            tree(40, 44, 14, "#3f8a33"); tree(160, 44, 16, "#46963a"); tree(210, 44, 11, "#3f8a33")
        }
    }

    fun bazaar(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("bazaar", arrayOf("#f6b26b", "#ffd9a8", "#ffe8c8", "#fff4e0"))
        sun(40, 40, 10, "#fff1c0", "#ffd080")
        clouds(f, cx, 18, 3, "#fff3e6", "#f4d9c0")
        strip("baz:dome", 440, 140, 50f, cx, 0.15f) {   // Chorsu blue dome + madrasa
            val x = 90
            px("#c9a77a", x - 70, 100, 140, 40); px("#e9d3a6", x - 69, 101, 138, 39)
            halfDisc("#1f5aa8", x, 100, 62); halfDisc("#2f7fc1", x - 2, 100, 58)
            for (k in -6..6) { val a = k * 0.21f; px("#1f5aa8", x + (sin(a) * 52).roundToInt(), 100 - (cos(a) * 55).roundToInt(), 2, (cos(a) * 50).roundToInt()) }
            px("#86c5ef", x - 30, 58, 6, 30); halfDisc("#e9d3a6", x, 42, 10); px("#f4c430", x - 1, 26, 2, 8)
            for (k in 0 until 10) { px("#2f7fc1", x - 64 + k * 13, 118, 8, 12); halfDisc("#2f7fc1", x - 60 + k * 13, 118, 4) }
            minaret(300, 40, 140, 8, "#c9a77a", "#2fb3c9"); px("#c9a77a", 260, 70, 90, 70); px("#e9d3a6", 262, 72, 86, 68)
            for (k in 0 until 4) { px("#2457c5", 268 + k * 20, 90, 12, 30); halfDisc("#2457c5", 274 + k * 20, 90, 6) }
        }
        strip("baz:stalls", 240, 90, 150f, cx, 0.35f) {   // market stalls with awnings and goods
            val cols = arrayOf("#d8373b", "#f5f0e6", "#2e9b6b", "#f4c430")
            for (k in 0 until 4) {
                val x = k * 60
                for (s in 0 until 7) px(cols[(s + k) % 4], x + s * 8, 20, 8, 10); for (s in 0 until 7) halfDisc(cols[(s + k) % 4], x + s * 8 + 4, 34, 4)
                px("#8b5a2b", x + 2, 30, 3, 60); px("#8b5a2b", x + 51, 30, 3, 60); px("#a8702a", x, 58, 56, 8); px("#c98a4a", x, 58, 56, 2)
                for (q in 0 until 5) { val c = arrayOf("#e9c94a", "#d8373b", "#2e9b6b", "#f08a24", "#7b3fa0")[(q + k) % 5]; circle(G.shade(c, 0.8f), x + 6 + q * 10, 54, 5); circle(c, x + 5 + q * 10, 53, 4) }
                for (q in 0 until 4) { px("#a0522d", x + 10 + q * 10, 36, 1, 8); circle("#c0632d", x + 10 + q * 10, 46, 2) }   // hanging dried fruit
                for (q in 0 until 3) { val px0 = x + 8 + q * 16; px("#3a2c24", px0, 66, 6, 24); circle("#e8ad7e", px0 + 3, 63, 3); px(arrayOf("#15151c", "#f5f5f5", "#d8373b")[q], px0, 60, 6, 2) }  // shoppers
            }
        }
        strip("baz:near", 200, 40, 200f, cx, 0.6f) {
            for (k in 0 until 4) { val x = k * 50; px("#8b5a2b", x + 4, 14, 30, 26); px("#a8702a", x + 5, 15, 28, 3); px("#6b3f1f", x + 4, 26, 30, 1)
                for (q in 0 until 3) { circle("#b8282a", x + 10 + q * 9, 12, 5); circle("#e24b4a", x + 9 + q * 9, 11, 4); px("#2e9b6b", x + 9 + q * 9, 6, 2, 2) } }
        }
        // tandir smoke
        for (k in 0 until 3) { val t = (f + k * 40) % 120; G.alpha((1f - t / 120f) * 0.5f) { circle("#ffffff", 20 + sin(t / 15f) * 4 - m(cx * 0.35f, 240f) + 60, 150 - t * 0.5f, 3 + t / 30f) } }
    }

    fun metro(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("metro", arrayOf("#141a2e", "#1f2a4a", "#24305a", "#1b2238"))
        strip("metro:wall", 192, 210, 0f, cx, 0.6f) {
            // vaulted ceiling with arches
            px("#2c3a6b", 0, 0, 192, 40); for (k in 0 until 4) { halfDisc("#3a4f80", k * 48 + 24, 40, 24); halfDisc("#2c3a6b", k * 48 + 24, 40, 20) }
            for (k in 0 until 4) { val x = k * 48
                px("#e9e1cc", x + 2, 40, 10, 160); px("#fffaf0", x + 3, 40, 2, 160); px("#c9bfa6", x + 10, 40, 2, 160)   // marble column
                px("#f4c430", x, 36, 14, 5); px("#c9a227", x, 41, 14, 2); px("#f4c430", x, 196, 14, 4)
                // cosmic mosaic panel (Kosmonavtlar)
                px("#0e1a3a", x + 16, 70, 28, 70); px("#c9a227", x + 15, 69, 30, 1); px("#c9a227", x + 15, 140, 30, 1)
                circle("#2e6fd8", x + 30, 96, 9); circle("#86c5ef", x + 28, 94, 4); for (q in 0 until 8) { val h = hash(x + q, 3); px("#fff6c8", x + 17 + h % 26, 72 + (h shr 6) % 66, 1, 1) }
                px("#f4c430", x + 26, 118, 8, 14); px("#e9e1cc", x + 28, 112, 4, 6)
            }
            px("#0e1322", 0, 200, 192, 10)
        }
        // chandeliers (flicker)
        for (i in -1 until 4) {
            val x = (i * 96 - m(cx * 0.6f, 96f)).roundToInt() + 48
            px("#c9a227", x, 0, 1, 16)
            halfDisc("#c9a227", x, 26, 11); G.c.save(); G.c.clipRect(x - 12f, 16f, x + 12f, 26f); circle("#f4c430", x, 26, 10); G.c.restore()
            for (k in 0 until 5) { val on = (f / 12 + k + i) % 7 != 0; px(if (on) "#fff6c8" else "#c9a227", x - 9 + k * 4, 27 + (k % 2) * 3, 2, 4) }
            G.alpha(0.12f) { circle("#fff6c8", x, 30, 24) }
        }
    }

    fun park(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("park", arrayOf("#7cc8f0", "#a8dcf5", "#d6f0fa", "#eef9fc"))
        sun(30, 32, 8, "#fffbe0", "#fff4b0")
        clouds(f, cx, 14, 5)
        strip("park:sky", 400, 80, 104f, cx, 0.08f) {   // skyline
            val hs = intArrayOf(40, 60, 30, 75, 50, 35, 68, 45, 55, 30)
            for (k in hs.indices) { val x = k * 40; px("#a9c6e0", x, 80 - hs[k], 30, hs[k]); for (r in 0 until hs[k] / 8) px("#c4dbef", x + 4, 84 - hs[k] + r * 8, 22, 2) }
        }
        // ferris wheel (rotates)
        val fx = (120 - m(cx * 0.12f, 360f)).roundToInt(); val fy = 104; val r = 46
        G.ring("#d8373b", fx, fy, r, 2f); G.ring("#f08a24", fx, fy, r - 6, 1f)
        for (k in 0 until 10) {
            val a = k / 10.0 * PI * 2 + f / 500.0
            val ex = fx + cos(a) * r; val ey = fy + sin(a) * r
            G.line("#c0c4cc", fx, fy, ex, ey)
            val col = arrayOf("#f4c430", "#2e6fd8", "#2e9b6b", "#d8373b", "#7b3fa0")[k % 5]
            px(OUT, ex.roundToInt() - 4, ey.roundToInt() - 1, 9, 8); px(col, ex.roundToInt() - 3, ey.roundToInt(), 7, 6); px("#cfe8ff", ex.roundToInt() - 2, ey.roundToInt() + 1, 5, 2)
        }
        circle("#8a8f99", fx, fy, 4); G.line("#8a8f99", fx, fy, fx - 22, fy + 86, 3f); G.line("#8a8f99", fx, fy, fx + 22, fy + 86, 3f)
        strip("park:trees", 200, 80, 120f, cx, 0.35f) {
            for (k in 0 until 5) { val x = k * 40 + 20; if (k % 2 == 0) tree(x, 80, 14, "#3f8a33", "#f7c6d9") else poplar(x, 80, 66, "#4f9a3a") }
        }
        // Anhor canal with ripples and reflections
        px("#4f9ed0", 0, 196, W, 12); px("#6fb6e0", 0, 196, W, 2)
        for (i in 0 until 10) px("#d6f0fa", m(i * 23 - f * 0.4f - cx * 0.4f, W.toFloat()), 199 + (i % 3) * 3, 6, 1)
        strip("park:near", 240, 30, 192f, cx, 0.6f) {
            for (k in 0 until 4) { val x = k * 60; px("#3a3f4a", x + 30, 0, 2, 30); px("#3a3f4a", x + 27, 0, 8, 2); circle("#fff6c8", x + 31, 4, 2) }   // lamps
        }
    }

    fun tower(g: Game) {
        val cy = g.camY; val tot = (g.rows * T - H).coerceAtLeast(1); val prog = 1 - cy / tot; val f = g.frame
        val top = G.mix("#9fd8f0", "#2f4f9a", prog.coerceIn(0f, 1f)); val bot = G.mix("#d6f0fa", "#7fa7dc", prog.coerceIn(0f, 1f))
        for (k in 0 until 9) px(G.mix(top, bot, k / 8f), 0, k * 30, W, 31)
        if (prog > 0.75f) for (i in 0 until 20) { val h = hash(i, 41); if ((f / 20 + i) % 6 != 0) px("#ffffff", h % W, (h shr 8) % 120, 1, 1) }
        for (i in 0 until 7) { val y = m(i * 90 - cy * 0.3f, 600f).roundToInt() - 60; cloud(((i * 53) % 140 - 10).toFloat(), y.toFloat(), 1 + i % 2) }
        val cityY = (g.rows * T - cy - 10).roundToInt()
        if (cityY < H + 140) for (i in 0 until 12) { val hh = 20 + hash(i, 9) % 50; px("#7a90b0", i * 16, cityY - hh, 15, hh + 30); for (r in 0 until hh / 8) px("#b8cce4", i * 16 + 3, cityY - hh + 4 + r * 8, 9, 2) }
        // lattice beams scrolling vertically
        val off = m(cy * 0.8f, 24f).roundToInt()
        Plat.gfx.cached("bg:tower:beams", W, 24, 0f, -24f - off + 24) { lattice() }
        var y = -off; while (y < H) { Plat.gfx.cached("bg:tower:beams", W, 24, 0f, y.toFloat()) { lattice() }; y += 24 }
    }

    private fun lattice() {
        px("#9fb0c4", 3, 0, 3, 24); px("#9fb0c4", W - 6, 0, 3, 24); px("#c9d3de", 3, 0, 1, 24); px("#c9d3de", W - 6, 0, 1, 24)
        G.alpha(0.35f) { G.line("#c9d3de", 5, 0, W - 5, 24); G.line("#c9d3de", W - 5, 0, 5, 24) }
        px("#8a9bb0", 3, 0, W - 6, 1)
    }

    fun skyCity(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("sunset", arrayOf("#ff9f5a", "#ffb36b", "#f08a6a", "#b0638f", "#5b3f86", "#3c2f6a"))
        sun(132, 150, 16, "#ffe28a", "#ffcf6a")
        clouds(f, cx, 30, 4, "#ffd2b0", "#e89a8a")
        strip("sc:far", 360, 140, 80f, cx, 0.12f) {
            for (k in 0 until 9) { val x = k * 40; val h = 50 + hash(k + 20, 2) % 70; px("#4a3a78", x, 140 - h, 34, h); px("#5a4a8a", x, 140 - h, 3, h) }
        }
        strip("sc:mid", 300, 170, 60f, cx, 0.3f) {
            val hs = intArrayOf(150, 110, 130, 95, 160)
            for (k in hs.indices) {
                val x = k * 60; val h = hs[k]
                px("#22355e", x, 170 - h, 46, h); px("#2e4a7a", x + 2, 170 - h, 6, h); px("#3f5f86", x, 170 - h, 46, 3)
                if (k % 2 == 0) { G.tri("#22355e", floatArrayOf(x.toFloat(), x + 23f, x + 46f), floatArrayOf(170f - h, 150f - h, 170f - h)) }
                var r = 0; while (r < h - 12) { for (c in 0 until 4) if (hash(k * 7 + c, r) % 3 != 0) px(if (hash(c, r + k) % 5 == 0) "#fff1c0" else "#ffd56b", x + 6 + c * 10, 176 - h + r, 5, 4); r += 9 }
            }
        }
        // aircraft warning lights
        for (k in 0 until 3) { val x = (k * 100 + 30 - m(cx * 0.3f, 300f)).roundToInt(); if ((f / 30 + k) % 2 == 0) px("#ff3b3b", x, 62 + k * 12, 2, 2) }
    }

    fun samarkand(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("sam", arrayOf("#3a9fd8", "#6fc4e8", "#a8dcf0", "#e6f4f8"))
        sun(150, 30, 8, "#fffbe0", "#fff4b0")
        clouds(f, cx, 12, 4)
        strip("sam:mtn", 360, 60, 120f, cx, 0.06f) { G.tri("#a8bfd4", floatArrayOf(0f, 60f, 120f, 200f, 260f, 360f, 360f, 0f), floatArrayOf(40f, 10f, 34f, 4f, 30f, 20f, 60f, 60f)) }
        strip("sam:registan", 420, 150, 56f, cx, 0.2f) { registan() }
        strip("sam:near", 200, 40, 182f, cx, 0.5f) { for (k in 0 until 5) tree(k * 40 + 18, 40, 9, "#3f8a33") ; px("#e9d3a6", 0, 34, 200, 6) }
    }

    /** Three madrasa facades with pishtaq portals, ribbed domes and minarets. */
    private fun registan() {
        val base = 150
        for (k in 0 until 3) {
            val x = k * 140 + 10; val w = 120
            px("#c9a77a", x, 50, w, 100); px("#d9b98a", x + 1, 51, w - 2, 98)
            // side niches
            for (q in 0 until 2) for (side in 0..1) { val nx = x + 6 + side * (w - 24) + q * 0; px("#1f4ea8", nx, 70 + q * 36, 12, 26); halfDisc("#1f4ea8", nx + 6, 70 + q * 36, 6); px("#2fb3c9", nx + 2, 72 + q * 36, 8, 1) }
            // pishtaq portal
            val px0 = x + 30
            px("#2e6fd8", px0, 30, 60, 120); px("#1f4ea8", px0 + 2, 32, 56, 2)
            for (q in 0 until 10) { px("#2fb3c9", px0 + 3 + q * 6, 36, 3, 3); px("#f4c430", px0 + 4 + q * 6, 37, 1, 1) }
            px("#c9a77a", px0 + 10, 50, 40, 100); halfDisc("#1f4ea8", px0 + 30, 74, 20); px("#1f4ea8", px0 + 10, 74, 40, 76)
            halfDisc("#2fb3c9", px0 + 30, 70, 12); px("#0f2f6a", px0 + 18, 80, 24, 70)
            if (k == 1) { circle("#f4c430", px0 + 30, 58, 6); px("#e8ad7e", px0 + 27, 56, 6, 4) }     // Sher-Dor sun face
            if (k != 1) for (q in 0 until 3) for (r in 0 until 3) px(if ((q + r) % 2 == 0) "#2fb3c9" else "#f5f0e6", px0 + 22 + q * 5, 86 + r * 5, 3, 3)
            dome(x + 15, 50, 12, "#2fb3c9"); dome(x + w - 15, 50, 12, "#2fb3c9")
            minaret(x - 4, 18, base, 8, "#c9a77a", "#2e6fd8"); minaret(x + w - 4, 18, base, 8, "#c9a77a", "#2e6fd8")
        }
    }

    fun bukhara(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("buk", arrayOf("#f2c27a", "#f6d9a0", "#f9e6bf", "#fbf0d8"))
        sun(140, 38, 12, "#fff6d0", "#ffe08a")
        // heat shimmer band
        strip("buk:dunes", 330, 70, 150f, cx, 0.08f) { for (k in 0 until 4) { halfDisc("#e2b878", k * 90 + 40, 70, 56); halfDisc("#ecc890", k * 90 + 36, 70, 50) } }
        strip("buk:kalon", 460, 170, 40f, cx, 0.2f) {
            val x = 80
            // Kalon minaret with brick bands
            px("#c99a5a", x, 20, 18, 150); px("#d2a86e", x + 2, 20, 14, 150)
            var y = 26; while (y < 170) { for (q in 0 until 4) px(if ((y / 7 + q) % 2 == 0) "#b8894e" else "#e0b880", x + 2 + q * 4, y, 3, 2); y += 7 }
            px("#2fb3c9", x - 2, 40, 22, 3); px("#b8894e", x - 3, 12, 24, 8); px("#d2a86e", x - 1, 4, 20, 8); halfDisc("#d2a86e", x + 9, 4, 8)
            // Mir-i-Arab domes and portal
            px("#d2a86e", x + 40, 100, 120, 70); px("#e0b880", x + 42, 102, 116, 68)
            dome(x + 70, 100, 18, "#2fb3c9"); dome(x + 130, 100, 18, "#2fb3c9")
            px("#2e6fd8", x + 88, 80, 24, 90); halfDisc("#1f4ea8", x + 100, 104, 10); px("#1f4ea8", x + 90, 104, 20, 66)
            // Chor-Minor
            val cm = x + 220; px("#d2a86e", cm, 120, 50, 50); px("#8b6b4a", cm + 19, 140, 12, 30)
            for (q in 0 until 4) { val tx = cm - 4 + q * 16; px("#d2a86e", tx, 104, 10, 18); halfDisc("#2fb3c9", tx + 5, 104, 6) }
            dome(cm + 25, 120, 9, "#2fb3c9")
        }
        strip("buk:houses", 210, 60, 176f, cx, 0.45f) {
            for (k in 0 until 3) { val x = k * 70; px("#d9b98a", x + 4, 20, 56, 40); halfDisc("#c9a77a", x + 32, 20, 14); px("#8b6b4a", x + 26, 36, 12, 24); window(x + 10, 30, 8, 8, "#8b6b4a", "#5a4030"); window(x + 46, 30, 8, 8, "#8b6b4a", "#5a4030") }
        }
        // storks circling
        for (k in 0 until 2) { val a = f / 90f + k * PI.toFloat(); val x = 60 + cos(a) * 30 - m(cx * 0.2f, 460f) + 100; val y = 50 + sin(a) * 8
            px("#ffffff", x, y, 5, 2); px("#1c1410", x - 2, y + (if ((f / 10) % 2 == 0) -1 else 1), 3, 1); px("#1c1410", x + 5, y + (if ((f / 10) % 2 == 0) -1 else 1), 3, 1); px("#f08a24", x + 5, y, 2, 1) }
    }

    fun khiva(g: Game) {
        val cx = g.camX; val f = g.frame
        sky("khiva", arrayOf("#f08a4a", "#f4b06a", "#f7c88a", "#f9dcae"))
        sun(40, 60, 13, "#fff0c0", "#ffc070")
        clouds(f, cx, 20, 3, "#ffe0c0", "#f0b890")
        strip("khv:towers", 400, 170, 10f, cx, 0.18f) {
            val x = 60
            // Kalta-Minor: wide, unfinished, glazed turquoise bands
            px("#1f8aa0", x, 80, 46, 90); var y = 84; while (y < 170) { px("#2fb3c9", x, y, 46, 6); px("#f4c430", x, y + 6, 46, 1); for (q in 0 until 9) px("#f5f0e6", x + 2 + q * 5, y + 2, 2, 2); y += 10 }
            px("#1f8aa0", x - 3, 74, 52, 6); px("#2fb3c9", x - 2, 72, 50, 2)
            // Islam-Khodja: tall slim minaret
            minaret(230, 6, 170, 12, "#d2a86e", "#2fb3c9")
            dome(290, 150, 16, "#2e9b6b")   // Pahlavon Mahmud
        }
        strip("khv:wall", 240, 80, 160f, cx, 0.4f) {   // Ichan-Kala crenellated wall with round towers
            px("#c48e58", 0, 24, 240, 56); px("#d39d66", 0, 24, 240, 3)
            for (k in 0 until 24) { val x = k * 10; px("#c48e58", x, 16, 7, 8); halfDisc("#b47e48", x + 3, 16, 3) }
            for (k in 0 until 3) { val x = k * 80 + 30; px("#b47e48", x, 10, 22, 70); px("#c48e58", x + 2, 12, 18, 68); halfDisc("#b47e48", x + 11, 10, 11); for (q in 0 until 4) px("#5a3a20", x + 9, 20 + q * 14, 4, 6) }
            for (k in 0 until 30) px("#a8743e", hash(k, 2) % 240, 30 + hash(k, 3) % 46, 4, 1)
        }
        // drifting sand grains
        for (i in 0 until 14) { val x = m(hash(i, 50) % W - f * (0.6f + (i % 3) * 0.3f), W.toFloat()); px("#f0d8a8", x, 120 + hash(i, 51) % 110, 2, 1) }
    }

    @Suppress("unused") fun label(s: String, x: Float, y: Float) = txt(s, x, y, "#f4c430", SM)
    @Suppress("unused") private fun dummy() = abs(0)
}
