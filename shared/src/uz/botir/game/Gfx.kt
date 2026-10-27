package uz.botir.game

import kotlin.math.roundToInt

/** Canvas-like facade so game code can call c.save()/c.translate() on any platform. */
object Cv {
    fun save() = Plat.gfx.save()
    fun restore() = Plat.gfx.restore()
    fun translate(x: Float, y: Float) = Plat.gfx.translate(x, y)
    fun scale(x: Float, y: Float) = Plat.gfx.scale(x, y)
    fun clipRect(l: Float, t: Float, r: Float, b: Float) = Plat.gfx.clip(l, t, r, b)
}

/** Pixel drawing helpers on the 180x270 logical screen. */
object G {
    val c = Cv
    private val cache = HashMap<String, Int>()

    fun col(s: String): Int = cache.getOrPut(s) {
        val h = s.removePrefix("#")
        val v = h.toLong(16)
        if (h.length == 6) (0xFF000000L or v).toInt() else v.toInt()
    }

    fun px(col: String, x: Number, y: Number, w: Number = 1, h: Number = 1) =
        Plat.gfx.rect(col(col), x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat())

    fun alpha(a: Float, block: () -> Unit) { Plat.gfx.saveAlpha(a); block(); Plat.gfx.restore() }

    fun circle(col: String, x: Number, y: Number, r: Number) = Plat.gfx.circle(col(col), x.toFloat(), y.toFloat(), r.toFloat())
    fun halfDisc(col: String, x: Number, y: Number, r: Number) = Plat.gfx.arcTop(col(col), x.toFloat(), y.toFloat(), r.toFloat())
    fun line(col: String, x0: Number, y0: Number, x1: Number, y1: Number, w: Float = 1f) =
        Plat.gfx.line(col(col), x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), w)
    fun ring(col: String, x: Number, y: Number, r: Number, w: Float = 1f) = Plat.gfx.ring(col(col), x.toFloat(), y.toFloat(), r.toFloat(), w)
    fun tri(col: String, xs: FloatArray, ys: FloatArray) = Plat.gfx.poly(col(col), xs, ys)

    fun measure(s: String, size: Float): Float = Plat.gfx.measure(s, size)

    /** align: 0 left, 1 center, 2 right. y is the top of the text. */
    fun txt(s: String, x: Number, y: Number, col: String = "#fff1e8", size: Float = 7f, align: Int = 0, shadow: Boolean = true) {
        val fx = x.toFloat(); val fy = y.toFloat() + size
        if (shadow) Plat.gfx.text(s, fx + 1, fy + 1, col(OUT), size, align)
        Plat.gfx.text(s, fx, fy, col(col), size, align)
    }

    fun wrap(s: String, maxW: Float, size: Float): List<String> {
        val out = ArrayList<String>(); var line = ""
        for (w in s.split(' ')) {
            val t = if (line.isEmpty()) w else "$line $w"
            if (measure(t, size) > maxW && line.isNotEmpty()) { out.add(line); line = w } else line = t
        }
        if (line.isNotEmpty()) out.add(line)
        return out
    }

    fun spr(rows: Array<String>, x: Number, y: Number, pal: Map<Char, String>) {
        val fx = x.toFloat(); val fy = y.toFloat()
        for (i in rows.indices) {
            val r = rows[i]
            for (j in r.indices) {
                val ch = r[j]
                if (ch != '.') px(if (ch == 'K') OUT else (pal[ch] ?: "#ff00ff"), fx + j, fy + i)
            }
        }
    }

    fun drawParts(list: List<Part>, ub: Float) {
        for (p in list) px(OUT, p.x - 1, p.y - 1 + (if (p.u) ub else 0f), p.w + 2, p.h + 2)
        for (p in list) {
            c.save(); if (p.u) c.translate(0f, ub)
            px(p.c, p.x, p.y, p.w, p.h)
            p.d?.invoke(p)
            c.restore()
        }
    }

    fun rnd(): Double = Plat.rnd()
    fun round(v: Float): Float = v.roundToInt().toFloat()
}

const val OUT = "#140c08"

class Part(val x: Float, val y: Float, val w: Float, val h: Float, val c: String, val u: Boolean = false, val d: ((Part) -> Unit)? = null)

fun P(x: Number, y: Number, w: Number, h: Number, c: String, u: Boolean = false, d: ((Part) -> Unit)? = null) =
    Part(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), c, u, d)

fun hash(a: Int, b: Int): Int = ((a * 73856093) xor (b * 19349663)) and 0x7fffffff
