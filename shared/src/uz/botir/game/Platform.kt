package uz.botir.game

/** Drawing backend: Android Canvas or HTML5 canvas. Colors are ARGB ints. */
interface Gfx {
    fun rect(c: Int, x: Float, y: Float, w: Float, h: Float)
    fun circle(c: Int, x: Float, y: Float, r: Float)
    fun arcTop(c: Int, x: Float, y: Float, r: Float)
    fun ring(c: Int, x: Float, y: Float, r: Float, w: Float)
    fun line(c: Int, x0: Float, y0: Float, x1: Float, y1: Float, w: Float)
    fun poly(c: Int, xs: FloatArray, ys: FloatArray)
    /** y is the text baseline; align 0 left, 1 center, 2 right */
    fun text(s: String, x: Float, y: Float, c: Int, size: Float, align: Int)
    fun measure(s: String, size: Float): Float
    fun save(); fun restore()
    fun translate(x: Float, y: Float); fun scale(x: Float, y: Float)
    fun clip(l: Float, t: Float, r: Float, b: Float)
    /** save() + multiply opacity; paired with restore() */
    fun saveAlpha(a: Float)
    fun identity()
}

/** Key-value persistence: SharedPreferences, or localStorage + Telegram CloudStorage. */
interface Store {
    fun getInt(k: String, d: Int): Int
    fun putInt(k: String, v: Int)
    fun getBool(k: String, d: Boolean): Boolean
    fun putBool(k: String, v: Boolean)
    fun getString(k: String, d: String): String
    fun putString(k: String, v: String)
    fun getSet(k: String): Set<String>
    fun putSet(k: String, v: Set<String>)
}

/** Sound backend. PCM is produced by [Synth] on both platforms. */
interface Audio {
    fun sfx(f: Float, d: Float, type: Int, v: Float, slide: Float)
    fun music(i: Int)
    fun setMusicEnabled(on: Boolean)
}

object Plat {
    lateinit var gfx: Gfx
    lateinit var store: Store
    lateinit var audio: Audio
    var vibrate: (Long) -> Unit = {}
    var later: (Long, () -> Unit) -> Unit = { _, f -> f() }
    /** [year, month(1-12), day] */
    var today: () -> IntArray = { intArrayOf(2026, 1, 1) }
    var rnd: () -> Double = { kotlin.random.Random.nextDouble() }
    /** Optional: share a text (Telegram). Null when the platform can't share. */
    var share: ((String) -> Unit)? = null
    /** Optional: default language from the platform (e.g. Telegram user language). */
    var defaultLang: String = "ru"
    var platformName = "android"
}

object NoAudio : Audio {
    override fun sfx(f: Float, d: Float, type: Int, v: Float, slide: Float) {}
    override fun music(i: Int) {}
    override fun setMusicEnabled(on: Boolean) {}
}

/** Short sound effects. type: 0 square, 1 triangle, 2 saw, 3 sine */
object Sfx {
    var enabled = true
    fun play(freq: Float, dur: Float = 0.08f, type: Int = 0, vol: Float = 0.25f, slide: Float = 1.5f) {
        if (enabled && freq > 0f) try { Plat.audio.sfx(freq, dur, type, vol, slide) } catch (_: Throwable) { }
    }
    fun tune(notes: IntArray, step: Long = 130, type: Int = 0) {
        notes.forEachIndexed { i, f -> if (f > 0) Plat.later(i * step) { play(f.toFloat(), 0.12f, type, 0.22f, 1.01f) } }
    }
}
