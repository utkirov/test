package uz.botir.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

/** Low-level pixel drawing on the 180x270 game bitmap. */
object G {
    lateinit var c: Canvas
    var font: Typeface = Typeface.MONOSPACE
    private val p = Paint().apply { isAntiAlias = false; isFilterBitmap = false; style = Paint.Style.FILL }
    private val tp = Paint().apply { isAntiAlias = false; isSubpixelText = false }
    private val cache = HashMap<String, Int>()

    fun col(s: String): Int = cache.getOrPut(s) { Color.parseColor(s) }

    fun px(col: String, x: Number, y: Number, w: Number = 1, h: Number = 1) {
        p.color = col(col)
        val fx = x.toFloat(); val fy = y.toFloat()
        c.drawRect(fx, fy, fx + w.toFloat(), fy + h.toFloat(), p)
    }

    fun alpha(a: Float, block: () -> Unit) {
        c.saveLayerAlpha(0f, 0f, W.toFloat() * 4, H.toFloat() * 40, (a * 255).toInt())
        block()
        c.restore()
    }

    fun circle(col: String, x: Number, y: Number, r: Number) {
        p.color = col(col); c.drawCircle(x.toFloat(), y.toFloat(), r.toFloat(), p)
    }

    fun halfDisc(col: String, x: Number, y: Number, r: Number) {
        p.color = col(col)
        val fx = x.toFloat(); val fy = y.toFloat(); val fr = r.toFloat()
        c.drawArc(fx - fr, fy - fr, fx + fr, fy + fr, 180f, 180f, true, p)
    }

    fun line(col: String, x0: Number, y0: Number, x1: Number, y1: Number, w: Float = 1f) {
        p.color = col(col); p.strokeWidth = w
        c.drawLine(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), p)
    }

    fun ring(col: String, x: Number, y: Number, r: Number, w: Float = 1f) {
        p.color = col(col); p.style = Paint.Style.STROKE; p.strokeWidth = w
        c.drawCircle(x.toFloat(), y.toFloat(), r.toFloat(), p)
        p.style = Paint.Style.FILL
    }

    fun tri(col: String, xs: FloatArray, ys: FloatArray) {
        p.color = col(col)
        val path = android.graphics.Path()
        path.moveTo(xs[0], ys[0]); for (i in 1 until xs.size) path.lineTo(xs[i], ys[i]); path.close()
        c.drawPath(path, p)
    }

    fun measure(s: String, size: Float): Float { tp.typeface = font; tp.textSize = size; return tp.measureText(s) }

    /** align: 0 left, 1 center, 2 right. y is the top of the text. */
    fun txt(s: String, x: Number, y: Number, col: String = "#fff1e8", size: Float = 7f, align: Int = 0, shadow: Boolean = true) {
        tp.typeface = font; tp.textSize = size
        tp.textAlign = when (align) { 1 -> Paint.Align.CENTER; 2 -> Paint.Align.RIGHT; else -> Paint.Align.LEFT }
        val fx = x.toFloat(); val fy = y.toFloat() + size
        if (shadow) { tp.color = col("#140c08"); c.drawText(s, fx + 1, fy + 1, tp) }
        tp.color = col(col); c.drawText(s, fx, fy, tp)
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
                if (ch != '.') { px(if (ch == 'K') OUT else (pal[ch] ?: "#ff00ff"), fx + j, fy + i) }
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
}

const val OUT = "#140c08"

class Part(val x: Float, val y: Float, val w: Float, val h: Float, val c: String, val u: Boolean = false, val d: ((Part) -> Unit)? = null)

fun P(x: Number, y: Number, w: Number, h: Number, c: String, u: Boolean = false, d: ((Part) -> Unit)? = null) =
    Part(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), c, u, d)

fun hash(a: Int, b: Int): Int = ((a * 73856093) xor (b * 19349663)) and 0x7fffffff
