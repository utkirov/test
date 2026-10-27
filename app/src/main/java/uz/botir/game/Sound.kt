package uz.botir.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

/** Tiny chiptune synthesizer: each effect is rendered once into a static AudioTrack. */
object Sfx {
    private const val RATE = 22050
    private val cache = HashMap<String, AudioTrack>()
    private val handler = Handler(Looper.getMainLooper())
    var enabled = true

    fun play(freq: Float, dur: Float = 0.08f, type: Int = 0, vol: Float = 0.25f, slide: Float = 1.5f) {
        if (!enabled || freq <= 0f) return
        val key = "$freq|$dur|$type|$vol|$slide"
        try {
            val t = cache.getOrPut(key) { build(freq, dur, type, vol, slide) }
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.stop()
            t.reloadStaticData(); t.play()
        } catch (_: Throwable) { }
    }

    /** type: 0 square, 1 triangle, 2 saw, 3 sine */
    private fun build(freq: Float, dur: Float, type: Int, vol: Float, slide: Float): AudioTrack {
        val n = (RATE * dur).toInt().coerceAtLeast(64)
        val buf = ShortArray(n)
        var phase = 0.0
        val k = ln(slide.toDouble()) / n
        for (i in 0 until n) {
            val f = freq * exp(k * i)
            phase += f / RATE
            val ph = phase - Math.floor(phase)
            val s = when (type) {
                1 -> 1 - 4 * Math.abs(ph - 0.5)
                2 -> 2 * ph - 1
                3 -> sin(2 * PI * ph)
                else -> if (ph < 0.5) 1.0 else -1.0
            }
            val env = (1.0 - i.toDouble() / n) * (if (i < 40) i / 40.0 else 1.0)
            buf[i] = (s * env * vol * 32767).toInt().toShort()
        }
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(n * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
        t.write(buf, 0, n)
        return t
    }

    fun tune(notes: IntArray, step: Long = 130, type: Int = 0) {
        notes.forEachIndexed { i, f -> if (f > 0) handler.postDelayed({ play(f.toFloat(), 0.12f, type, 0.22f, 1.01f) }, i * step) }
    }

    fun release() { cache.values.forEach { try { it.release() } catch (_: Throwable) { } }; cache.clear() }
}
