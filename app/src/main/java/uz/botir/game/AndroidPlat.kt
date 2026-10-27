package uz.botir.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import java.util.Calendar

/** Android implementations of the shared platform interfaces. */
class AndroidGfx(private val font: Typeface) : Gfx {
    var cv: Canvas? = null
    private val c get() = cv!!
    private val p = Paint().apply { isAntiAlias = false; isFilterBitmap = false; style = Paint.Style.FILL }
    private val tp = Paint().apply { isAntiAlias = false; isSubpixelText = false; typeface = font }

    override fun rect(c: Int, x: Float, y: Float, w: Float, h: Float) { p.color = c; this.c.drawRect(x, y, x + w, y + h, p) }
    override fun circle(c: Int, x: Float, y: Float, r: Float) { p.color = c; this.c.drawCircle(x, y, r, p) }
    override fun arcTop(c: Int, x: Float, y: Float, r: Float) { p.color = c; this.c.drawArc(x - r, y - r, x + r, y + r, 180f, 180f, true, p) }
    override fun ring(c: Int, x: Float, y: Float, r: Float, w: Float) {
        p.color = c; p.style = Paint.Style.STROKE; p.strokeWidth = w; this.c.drawCircle(x, y, r, p); p.style = Paint.Style.FILL
    }
    override fun line(c: Int, x0: Float, y0: Float, x1: Float, y1: Float, w: Float) { p.color = c; p.strokeWidth = w; this.c.drawLine(x0, y0, x1, y1, p) }
    override fun poly(c: Int, xs: FloatArray, ys: FloatArray) {
        p.color = c; val path = Path(); path.moveTo(xs[0], ys[0]); for (i in 1 until xs.size) path.lineTo(xs[i], ys[i]); path.close(); this.c.drawPath(path, p)
    }
    override fun text(s: String, x: Float, y: Float, c: Int, size: Float, align: Int) {
        tp.textSize = size; tp.color = c
        tp.textAlign = when (align) { 1 -> Paint.Align.CENTER; 2 -> Paint.Align.RIGHT; else -> Paint.Align.LEFT }
        this.c.drawText(s, x, y, tp)
    }
    override fun measure(s: String, size: Float): Float { tp.textSize = size; return tp.measureText(s) }
    override fun save() { c.save() }
    override fun restore() { c.restore() }
    override fun translate(x: Float, y: Float) = c.translate(x, y)
    override fun scale(x: Float, y: Float) = c.scale(x, y)
    override fun clip(l: Float, t: Float, r: Float, b: Float) { c.clipRect(l, t, r, b) }
    override fun saveAlpha(a: Float) { c.saveLayerAlpha(null, (a * 255).toInt()) }
    override fun identity() = c.setMatrix(null)

    private val pics = HashMap<String, Bitmap>()
    private val bp = Paint().apply { isFilterBitmap = false; isAntiAlias = false }
    private val dst = RectF()
    override fun cached(key: String, w: Int, h: Int, x: Float, y: Float, draw: () -> Unit) {
        val b = pics[key] ?: run {
            val bmp = Bitmap.createBitmap(w * 3, h * 3, Bitmap.Config.ARGB_8888)
            val prev = cv; cv = Canvas(bmp); cv!!.scale(3f, 3f)
            try { draw() } finally { cv = prev }
            pics[key] = bmp; bmp
        }
        dst.set(x, y, x + w, y + h); c.drawBitmap(b, null, dst, bp)
    }
    override fun dropCache(prefix: String) {
        val it = pics.entries.iterator()
        while (it.hasNext()) { val e = it.next(); if (e.key.startsWith(prefix)) { e.value.recycle(); it.remove() } }
    }
}

class AndroidStore(ctx: Context) : Store {
    private val p = ctx.getSharedPreferences("botir", Context.MODE_PRIVATE)
    override fun getInt(k: String, d: Int) = p.getInt(k, d)
    override fun putInt(k: String, v: Int) = p.edit().putInt(k, v).apply()
    override fun getBool(k: String, d: Boolean) = p.getBoolean(k, d)
    override fun putBool(k: String, v: Boolean) = p.edit().putBoolean(k, v).apply()
    override fun getString(k: String, d: String) = p.getString(k, d) ?: d
    override fun putString(k: String, v: String) = p.edit().putString(k, v).apply()
    override fun getSet(k: String): Set<String> = p.getStringSet(k, emptySet()) ?: emptySet()
    override fun putSet(k: String, v: Set<String>) = p.edit().putStringSet(k, HashSet(v)).apply()
}

object AndroidAudio : Audio {
    private val sfxCache = HashMap<String, AudioTrack>()
    @Volatile private var want = -1
    @Volatile private var running = false
    @Volatile private var musicOn = true
    private var thread: Thread? = null

    private fun pcm16(f: FloatArray) = ShortArray(f.size) { (f[it].coerceIn(-1f, 1f) * 32767).toInt().toShort() }

    private fun attrs(music: Boolean) = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(if (music) AudioAttributes.CONTENT_TYPE_MUSIC else AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    private val fmt = AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(Synth.RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()

    override fun play(key: String, pcm: () -> FloatArray) {
        try {
            val t = sfxCache.getOrPut(key) {
                val buf = pcm16(pcm())
                AudioTrack.Builder().setAudioAttributes(attrs(false)).setAudioFormat(fmt).setBufferSizeInBytes(buf.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC).build().also { it.write(buf, 0, buf.size) }
            }
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.stop()
            t.reloadStaticData(); t.play()
        } catch (_: Throwable) { }
    }

    override fun music(i: Int) { want = i }
    override fun setMusicEnabled(on: Boolean) { musicOn = on }

    fun start() {
        if (running) return
        running = true
        thread = Thread({ loop() }, "botir-music").apply { isDaemon = true; start() }
    }

    fun stop() { running = false; try { thread?.join(400) } catch (_: Throwable) { }; thread = null }

    fun release() { sfxCache.values.forEach { try { it.release() } catch (_: Throwable) { } }; sfxCache.clear() }

    private fun loop() {
        var track: AudioTrack? = null
        try {
            val minBuf = AudioTrack.getMinBufferSize(Synth.RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(4096)
            track = AudioTrack.Builder().setAudioAttributes(attrs(true)).setAudioFormat(fmt).setBufferSizeInBytes(minBuf * 2).setTransferMode(AudioTrack.MODE_STREAM).build()
            track.play()
            val chunk = ShortArray(1024); val silence = ShortArray(1024)
            var cur = -1; var pos = 0; var fade = 0f
            val songs = HashMap<Int, ShortArray>()
            while (running) {
                val w = want
                if (w != cur) { if (fade > 0f) fade -= 0.08f; if (fade <= 0f) { cur = w; pos = 0; fade = 0f } }
                else if (fade < 1f) fade += 0.04f
                val buf = if (cur in 0 until Synth.songCount) songs.getOrPut(cur) { pcm16(Synth.song(cur)) } else null
                if (buf == null || !musicOn) { track.write(silence, 0, silence.size); continue }
                for (i in chunk.indices) { chunk[i] = (buf[pos] * fade).toInt().toShort(); pos++; if (pos >= buf.size) pos = 0 }
                track.write(chunk, 0, chunk.size)
            }
        } catch (_: Throwable) {
        } finally {
            try { track?.stop() } catch (_: Throwable) { }
            try { track?.release() } catch (_: Throwable) { }
        }
    }
}

object AndroidPlatform {
    private val handler = Handler(Looper.getMainLooper())
    lateinit var gfx: AndroidGfx
    fun init(ctx: Context, font: Typeface) {
        if (::gfx.isInitialized) return
        gfx = AndroidGfx(font)
        Plat.gfx = gfx
        Plat.store = AndroidStore(ctx.applicationContext)
        Plat.audio = AndroidAudio
        Plat.later = { ms, f -> handler.postDelayed(f, ms) }
        Plat.today = { val c = Calendar.getInstance(); intArrayOf(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)) }
        Plat.platformName = "android"
    }
}
