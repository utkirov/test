package uz.botir.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/** Hosts the game: fixed 60 Hz simulation, 3x pixel bitmap scaled to fit, on-screen touch controls. */
class GameView(ctx: Context) : View(ctx), Choreographer.FrameCallback {
    private val font: Typeface = try { Typeface.createFromAsset(ctx.assets, "PressStart2P.ttf") } catch (_: Throwable) { Typeface.MONOSPACE }
    init { AndroidPlatform.init(ctx, font) }
    val game = Game()
    private val bmp: Bitmap = Bitmap.createBitmap(W * 3, H * 3, Bitmap.Config.ARGB_8888)
    private val bc = Canvas(bmp)
    private val blit = Paint().apply { isFilterBitmap = false; isAntiAlias = false }
    private val paint = Paint().apply { isAntiAlias = true }
    private val dst = RectF()
    private var ctrlTop = 0f; private var r = 60f
    private var cxL = 0f; private var cxR = 0f; private var cxJ = 0f; private var cy = 0f
    private var last = 0L; private var acc = 0.0; private var running = false
    private var layoutVer = -1
    private val zoneOf = HashMap<Int, Char>()
    init {
        isFocusable = true
        @Suppress("DEPRECATION")
        val vib = try { ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator } catch (_: Throwable) { null }
        Plat.vibrate = { ms ->
            if (vib != null && vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                else @Suppress("DEPRECATION") vib.vibrate(ms)
            }
        }
    }

    fun start() { if (!running) { running = true; last = 0L; Choreographer.getInstance().postFrameCallback(this) } }
    fun stop() { running = false; Choreographer.getInstance().removeFrameCallback(this) }

    override fun doFrame(t: Long) {
        if (!running) return
        if (last == 0L) last = t
        acc += (t - last) / 1e9; last = t
        if (acc > 0.1) acc = 0.1
        while (acc >= 1.0 / 60) { game.update(); acc -= 1.0 / 60 }
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) { relayout(w, h) }

    private fun relayout(w: Int, h: Int) {
        if (w == 0 || h == 0) return
        layoutVer = game.settingsVer
        val scale = arrayOf(0.82f, 1f, 1.2f)[game.btnSize]
        val ctrlH = (h * 0.24f * scale).coerceIn(200f, 600f)
        val avail = h - ctrlH
        val s = min(w / W.toFloat(), avail / H.toFloat())
        val gw = W * s; val gh = H * s
        val left = (w - gw) / 2f; val top = ((avail - gh) / 2f).coerceAtLeast(0f)
        dst.set(left, top, left + gw, top + gh)
        ctrlTop = top + gh
        val ch = h - ctrlTop
        r = min(w * 0.11f * scale, ch * 0.32f)
        cy = ctrlTop + ch * 0.5f
        if (!game.lefty) { cxL = r * 1.35f; cxR = cxL + r * 2.45f; cxJ = w - r * 1.6f }
        else { cxJ = r * 1.6f; cxR = w - r * 1.35f; cxL = cxR - r * 2.45f }
    }

    override fun onDraw(canvas: Canvas) {
        if (layoutVer != game.settingsVer) relayout(width, height)
        AndroidPlatform.gfx.cv = bc; game.render()
        canvas.drawColor(Color.parseColor("#0f1631"))
        canvas.drawBitmap(bmp, null, dst, blit)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 6f; paint.color = Color.BLACK
        canvas.drawRect(dst, paint); paint.style = Paint.Style.FILL
        drawControls(canvas)
    }

    private fun drawControls(c: Canvas) {
        val play = game.state == "play"
        val a = if (play) 255 else 90
        fun btn(x: Float, rr: Float, fill: String, pressed: Boolean) {
            paint.color = Color.BLACK; paint.alpha = a; c.drawCircle(x, cy + (if (pressed) 4f else 10f), rr, paint)
            paint.color = Color.parseColor(if (pressed) "#2e6fd8" else fill); paint.alpha = a; c.drawCircle(x, cy + (if (pressed) 4f else 0f), rr - 4, paint)
        }
        btn(cxL, r, "#1d2b53", game.kL); btn(cxR, r, "#1d2b53", game.kR); btn(cxJ, r * 1.2f, "#d8373b", game.kJ)
        paint.color = Color.parseColor("#fff1e8"); paint.alpha = a
        val t = r * 0.42f
        fun tri(x: Float, y: Float, dir: Int) {
            val p = Path()
            when (dir) {
                -1 -> { p.moveTo(x - t, y); p.lineTo(x + t * 0.8f, y - t); p.lineTo(x + t * 0.8f, y + t) }
                1 -> { p.moveTo(x + t, y); p.lineTo(x - t * 0.8f, y - t); p.lineTo(x - t * 0.8f, y + t) }
                else -> { p.moveTo(x, y - t); p.lineTo(x - t, y + t * 0.7f); p.lineTo(x + t, y + t * 0.7f) }
            }
            p.close(); c.drawPath(p, paint)
        }
        tri(cxL, cy + (if (game.kL) 4f else 0f), -1); tri(cxR, cy + (if (game.kR) 4f else 0f), 1); tri(cxJ, cy - r * 0.2f + (if (game.kJ) 4f else 0f), 0)
        paint.typeface = font; paint.textSize = r * 0.2f; paint.textAlign = Paint.Align.CENTER
        c.drawText(game.L("ПРЫЖОК", "SAKRASH"), cxJ, cy + r * 0.55f + (if (game.kJ) 4f else 0f), paint)
        paint.alpha = 255
    }

    private fun zone(x: Float, y: Float): Char {
        if (y < ctrlTop) return ' '
        val mid = (cxL + cxR) / 2
        return if (!game.lefty) when { x < mid -> 'L'; x < width * 0.55f -> 'R'; else -> 'J' }
        else when { x < width * 0.45f -> 'J'; x < mid -> 'L'; else -> 'R' }
    }

    private fun refreshKeys() {
        game.kL = zoneOf.containsValue('L'); game.kR = zoneOf.containsValue('R'); game.kJ = zoneOf.containsValue('J')
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = e.actionIndex; val x = e.getX(i); val y = e.getY(i); val z = zone(x, y)
                if (z == ' ' || game.state != "play") {
                    val gx = (x - dst.left) / dst.width() * W; val gy = (y - dst.top) / dst.height() * H
                    game.tap(gx, gy)
                } else {
                    zoneOf[e.getPointerId(i)] = z
                    if (z == 'J') game.jumpPressed()
                    refreshKeys()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until e.pointerCount) {
                    val id = e.getPointerId(i); val old = zoneOf[id] ?: continue
                    val z = zone(e.getX(i), e.getY(i))
                    if (z != ' ' && z != old && old != 'J' && z != 'J') zoneOf[id] = z
                }
                refreshKeys()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> { zoneOf.remove(e.getPointerId(e.actionIndex)); refreshKeys() }
            MotionEvent.ACTION_CANCEL -> { zoneOf.clear(); refreshKeys() }
        }
        return true
    }

    /** Off-screen render used for store screenshots. */
    fun renderTo(target: Bitmap) { val c = Canvas(target); layout(0, 0, target.width, target.height); relayout(target.width, target.height); draw(c) }
}
