package uz.botir.game

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

/** Song ids and the shared on/off switch. Playback is done by the platform's [Audio]. */
object Music {
    const val TITLE = 0; const val MAHALLA = 1; const val CHORSU = 2; const val METRO = 3; const val ANHOR = 4
    const val TOWER = 5; const val BOSS = 6; const val SAMARKAND = 7; const val BUKHARA = 8; const val KHIVA = 9; const val ENDLESS = 10
    const val FERGANA = 11; const val RISHTAN = 12; const val ARAL = 13
    var enabled = true
        set(v) { field = v; try { Plat.audio.setMusicEnabled(v) } catch (_: Throwable) { } }
    fun play(i: Int) { try { Plat.audio.music(i) } catch (_: Throwable) { } }
}

/**
 * Chiptune synthesizer shared by Android and Web. Melodies are original, written in modes
 * common in Uzbek music (hijaz, phrygian, pentatonic, 6/8 lazgi feel).
 */
object Synth {
    const val RATE = 22050

    private class Song(val sps: Float, val mel: String, val bass: String, val drums: String)

    private val SONGS = arrayOf(
        Song(4.2f, "D5 - Eb5 - F#5 - G5 - A5 - - - G5 F#5 Eb5 - D5 - - - A4 - D5 - Eb5 F#5 G5 - F#5 - - - Bb5 A5 G5 - F#5 G5 A5 - D6 - C6 Bb5 A5 - - - G5 F#5 Eb5 - D5 - - - . . . .",
            "D3 - - A2 D3 - - A2 G2 - - D3 G2 - - D3 Eb3 - - Bb2 Eb3 - - Bb2 D3 - - A2 D3 - . .", "D..T..T."),
        Song(5.5f, "G5 . B5 D6 . B5 C6 . A5 B5 . G5 A5 . F5 G5 - . G5 . B5 D6 . E6 D6 . B5 C6 . A5 G5 - - . D5 E5 F5 G5 A5 B5 C6 B5 A5 G5 F5 E5 D5 . G4 B4 D5 G5 - .",
            "G3 . D3 G3 . D3 C3 . G2 C3 . G2 D3 . A2 D3 . A2 G3 . D3 G3 . D3", "D.tD.t"),
        Song(5f, "A4 C5 D5 E5 D5 C5 A4 . G4 A4 C5 D5 E5 - - . E5 G5 A5 G5 E5 D5 C5 . D5 E5 D5 C5 A4 - - . C5 C5 D5 . E5 E5 G5 . A5 G5 E5 D5 C5 D5 A4 -",
            "A2 . E3 . A2 . E3 . G2 . D3 . G2 . D3 .", "D.t.D.tt"),
        Song(4.5f, "E5 - B4 - E5 F#5 G5 - F#5 - E5 - D#5 - B4 - E5 - G5 - B5 - A5 G5 F#5 - G5 F#5 E5 - D#5 E5 F#5 -",
            "E3 - B2 - E3 - B2 - C3 - G2 - B2 - F#2 -", "D.T.D.T."),
        Song(3.6f, "E5 - G5 - A5 - - - G5 E5 D5 - C5 - - - D5 - E5 - G5 - E5 - D5 - - - . . . . A4 - C5 - D5 - E5 - G5 - A5 G5 E5 - - - D5 C5 A4 - C5 - - - . . . .",
            "C3 - G2 - A2 - E2 - F2 - C3 - G2 - - -", "D...t..."),
        Song(5f, "C5 D5 E5 G5 A5 G5 E5 D5 D5 E5 G5 A5 C6 A5 G5 E5 E5 G5 A5 C6 D6 C6 A5 G5 A5 - G5 - E5 - D5 -",
            "C3 . C3 . A2 . A2 . F2 . F2 . G2 . G2 .", "D.t.D.t."),
        Song(6f, "D5 D5 . D5 F5 . E5 D5 C#5 - - . A4 - - . D5 D5 . D5 F5 . G5 A5 Bb5 - A5 - G5 F5 E5 -",
            "D3 D3 . D3 D3 . D3 . A2 A2 . A2 A2 . A2 . Bb2 Bb2 . Bb2 Bb2 . Bb2 . A2 A2 . A2 C#3 . E3 .", "D.tD.Dt."),
        Song(4f, "G4 - Ab4 - B4 - C5 - D5 - - - Eb5 D5 C5 B4 C5 - - - B4 Ab4 G4 - - - D5 - G5 - F5 Eb5 D5 - C5 - B4 C5 D5 - Ab4 - - - G4 - - - . . . .",
            "G2 - - D3 G2 - - D3 C3 - - G2 C3 - - G2 Ab2 - - Eb3 G2 - - D3 G2 - - D3 G2 - . .", "D..t.Dt."),
        Song(3.4f, "E5 - - F5 E5 - D5 - C5 - D5 - E5 - - - B4 - C5 - D5 - E5 F5 G5 - F5 E5 F5 - E5 - - - . .",
            "E2 - - - E2 - - - D2 - - - E2 - - -", "D..tD.t."),
        Song(6f, "A4 C5 E5 A5 G#5 A5 B5 A5 G#5 E5 D5 C5 B4 C5 D5 E5 F5 E5 D5 C5 B4 A4 G#4 B4 A4 - - . A5 - - .",
            "A2 . E3 . A2 . E3 . D3 . A2 . E3 . E2 .", "DtTtDtTt"),
        Song(6f, "C5 E5 G5 E5 C6 - G5 E5 F5 A5 C6 A5 F5 - . . G5 B5 D6 B5 G5 - F5 D5 E5 G5 C6 - - - . .",
            "C3 . G2 . C3 . G2 . F2 . C3 . F2 . C3 . G2 . D3 . G2 . D3 . C3 . G2 . C3 . . .", "D.t.DDt."),
        // Fergana: bright pentatonic dance in 6/8
        Song(5.5f, "C5 D5 E5 G5 . E5 D5 C5 . A4 C5 D5 E5 - - . G5 A5 G5 E5 D5 E5 C5 . D5 E5 D5 C5 A4 - - . E5 G5 A5 C6 - A5 G5 E5 . D5 E5 G5 E5 D5 C5 D5 . C5 - - .",
            "C3 . G2 . A2 . E2 . F2 . C3 . G2 . G2 .", "D.tD.t"),
        // Rishtan: dorian, steady potter's-wheel groove
        Song(4.8f, "D5 - F5 G5 A5 - G5 F5 E5 - D5 - C5 D5 E5 - F5 - E5 D5 C5 - A4 - D5 - - - . . . . A5 - C6 - B5 A5 G5 - A5 G5 F5 - E5 - D5 - F5 E5 D5 C5 D5 - - - . . . .",
            "D3 . A2 . C3 . G2 . Bb2 . F2 . A2 . A2 .", "D.t.DtT."),
        // Aral: wide, wistful minor
        Song(3.4f, "A4 - - C5 E5 - - D5 C5 - B4 - A4 - - - E5 - - F5 E5 - D5 - C5 - D5 - E5 - - - A5 - - G5 F5 - E5 - D5 - C5 - B4 - - - C5 - B4 - A4 - - - . . . .",
            "A2 - - - F2 - - - C3 - - - E2 - - -", "D...t.T.")
    )

    val songCount get() = SONGS.size
    private val cache = HashMap<Int, FloatArray>()

    /** One loop of the song as mono float PCM (-1..1) at [RATE]. */
    fun song(i: Int): FloatArray = cache.getOrPut(i) { render(SONGS[i]) }

    /** A short effect as mono float PCM. type: 0 square, 1 triangle, 2 saw, 3 sine */
    fun sfx(freq: Float, dur: Float, type: Int, vol: Float, slide: Float): FloatArray {
        val n = (RATE * dur).toInt().coerceAtLeast(64)
        val buf = FloatArray(n)
        var phase = 0.0
        val k = ln(slide.toDouble()) / n
        for (i in 0 until n) {
            val f = freq * exp(k * i)
            phase += f / RATE
            val ph = phase - floor(phase)
            val s = when (type) { 1 -> 1 - 4 * abs(ph - 0.5); 2 -> 2 * ph - 1; 3 -> sin(2 * PI * ph); else -> if (ph < 0.5) 1.0 else -1.0 }
            val env = (1.0 - i.toDouble() / n) * (if (i < 40) i / 40.0 else 1.0)
            buf[i] = (s * env * vol).toFloat()
        }
        return buf
    }

    // ---------- layered sound effects ----------
    private class Mix(dur: Float) {
        val buf = FloatArray((RATE * dur).toInt().coerceAtLeast(64))
        var seed = 777
        fun noise(): Double { seed = seed * 1103515245 + 12345; return ((seed shr 16) and 0x7fff) / 16384.0 - 1.0 }
        /** Tone from f0 to f1 (exponential), starting at t0 for d seconds. wave: 0 square(duty), 1 triangle, 2 saw, 3 sine */
        fun tone(t0: Float, d: Float, f0: Float, f1: Float, vol: Float, wave: Int = 0, duty: Float = 0.5f, vib: Float = 0f, atk: Float = 0.004f, decay: Float = 0f) {
            val s0 = (t0 * RATE).toInt(); val n = (d * RATE).toInt(); var ph = 0.0
            val k = ln((f1 / f0).toDouble()) / n
            for (i in 0 until n) {
                val idx = s0 + i; if (idx >= buf.size) break
                val t = i.toDouble() / RATE
                val f = f0 * exp(k * i) * (1 + vib * sin(2 * PI * 7 * t))
                ph += f / RATE; val p = ph - floor(ph)
                val w = when (wave) { 1 -> 1 - 4 * abs(p - 0.5); 2 -> 2 * p - 1; 3 -> sin(2 * PI * p); else -> if (p < duty) 1.0 else -1.0 }
                val a = (t / atk).coerceAtMost(1.0) * (if (decay > 0) exp(-t * decay) else (1 - i.toDouble() / n))
                buf[idx] += (w * a * vol).toFloat()
            }
        }
        /** Noise burst; lp in 0..1 = smoothing (higher → darker). */
        fun hiss(t0: Float, d: Float, vol: Float, lp: Float = 0.5f, decay: Float = 20f) {
            val s0 = (t0 * RATE).toInt(); val n = (d * RATE).toInt(); var y = 0.0
            for (i in 0 until n) {
                val idx = s0 + i; if (idx >= buf.size) break
                val t = i.toDouble() / RATE
                y = y * lp + noise() * (1 - lp)
                buf[idx] += (y * vol * exp(-t * decay) * (if (lp > 0.6f) 2.0 else 1.0)).toFloat()
            }
        }
        fun out(): FloatArray { for (i in buf.indices) buf[i] = buf[i].coerceIn(-1f, 1f); return buf }
    }

    /** Named effects. Unknown names fall back to a short blip. */
    fun fx(name: String): FloatArray = when (name) {
        "jump" -> Mix(0.18f).apply { tone(0f, 0.16f, 260f, 720f, 0.22f, 0, 0.25f); hiss(0f, 0.05f, 0.05f, 0.3f, 60f) }.out()
        "djump" -> Mix(0.22f).apply { tone(0f, 0.18f, 520f, 1300f, 0.18f, 0, 0.125f); tone(0.05f, 0.15f, 1040f, 1560f, 0.08f, 1) }.out()
        "coin" -> Mix(0.22f).apply { tone(0f, 0.06f, 988f, 988f, 0.18f, 0, 0.25f, decay = 8f); tone(0.055f, 0.16f, 1319f, 1319f, 0.2f, 0, 0.25f, decay = 14f) }.out()
        "stomp" -> Mix(0.25f).apply { tone(0f, 0.18f, 180f, 55f, 0.4f, 3, decay = 14f); hiss(0f, 0.08f, 0.25f, 0.55f, 40f); tone(0.02f, 0.08f, 900f, 500f, 0.07f, 0, 0.25f) }.out()
        "hurt" -> Mix(0.4f).apply { tone(0f, 0.36f, 420f, 110f, 0.22f, 2, vib = 0.06f); hiss(0f, 0.12f, 0.12f, 0.4f, 25f) }.out()
        "power" -> Mix(0.6f).apply { val n = intArrayOf(523, 659, 784, 1047, 1319, 1568); n.forEachIndexed { i, f -> tone(i * 0.07f, 0.16f, f.toFloat(), f.toFloat(), 0.16f, 0, 0.25f, decay = 10f) }; tone(0f, 0.5f, 262f, 262f, 0.08f, 1) }.out()
        "heal" -> Mix(0.45f).apply { tone(0f, 0.4f, 523f, 523f, 0.12f, 1, decay = 5f); tone(0.06f, 0.36f, 659f, 659f, 0.12f, 1, decay = 5f); tone(0.12f, 0.3f, 784f, 784f, 0.12f, 1, decay = 5f) }.out()
        "spring" -> Mix(0.45f).apply { tone(0f, 0.42f, 180f, 640f, 0.3f, 3, vib = 0.18f, decay = 5f); tone(0f, 0.1f, 90f, 60f, 0.25f, 3, decay = 20f) }.out()
        "splash" -> Mix(0.6f).apply { hiss(0f, 0.5f, 0.45f, 0.75f, 7f); listOf(0.12f, 0.2f, 0.31f).forEachIndexed { i, t -> tone(t, 0.05f, 600f + i * 200, 1100f + i * 250, 0.07f, 3) } }.out()
        "bubble" -> Mix(0.12f).apply { tone(0f, 0.1f, 500f, 1200f, 0.08f, 3) }.out()
        "crack" -> Mix(0.3f).apply { hiss(0f, 0.04f, 0.4f, 0.2f, 80f); hiss(0.07f, 0.05f, 0.35f, 0.3f, 70f); tone(0f, 0.2f, 140f, 70f, 0.2f, 2, decay = 18f) }.out()
        "land" -> Mix(0.1f).apply { tone(0f, 0.08f, 120f, 60f, 0.25f, 3, decay = 35f); hiss(0f, 0.05f, 0.08f, 0.7f, 60f) }.out()
        "zip" -> Mix(0.5f).apply { tone(0f, 0.45f, 400f, 900f, 0.08f, 2, vib = 0.03f); hiss(0f, 0.45f, 0.06f, 0.2f, 3f) }.out()
        "egg" -> Mix(0.8f).apply { val n = intArrayOf(659, 784, 988, 1319); n.forEachIndexed { i, f -> tone(i * 0.1f, 0.3f, f.toFloat(), f.toFloat(), 0.14f, 0, 0.25f, decay = 6f); tone(i * 0.1f, 0.3f, f / 2f, f / 2f, 0.08f, 1, decay = 6f) } }.out()
        "bosshit" -> Mix(0.5f).apply { tone(0f, 0.3f, 120f, 40f, 0.45f, 3, decay = 8f); hiss(0f, 0.2f, 0.3f, 0.5f, 15f); tone(0.01f, 0.4f, 1800f, 1700f, 0.06f, 3, decay = 9f); tone(0.01f, 0.4f, 2410f, 2400f, 0.04f, 3, decay = 9f) }.out()
        "roar" -> Mix(0.8f).apply { tone(0f, 0.75f, 110f, 70f, 0.25f, 2, vib = 0.12f, atk = 0.05f); hiss(0f, 0.7f, 0.15f, 0.8f, 3f) }.out()
        "honk" -> Mix(0.3f).apply { tone(0f, 0.12f, 330f, 300f, 0.14f, 0, 0.3f, vib = 0.05f); tone(0.15f, 0.12f, 330f, 290f, 0.14f, 0, 0.3f, vib = 0.05f) }.out()
        "bell" -> Mix(0.6f).apply { tone(0f, 0.55f, 2093f, 2093f, 0.1f, 3, decay = 7f); tone(0f, 0.55f, 2637f, 2637f, 0.07f, 3, decay = 8f); tone(0.18f, 0.4f, 2093f, 2093f, 0.08f, 3, decay = 8f) }.out()
        "throw" -> Mix(0.2f).apply { hiss(0f, 0.18f, 0.2f, 0.1f, 15f); tone(0f, 0.15f, 700f, 300f, 0.05f, 3) }.out()
        "door" -> Mix(0.5f).apply { tone(0f, 0.15f, 784f, 784f, 0.12f, 1, decay = 8f); tone(0.12f, 0.35f, 1175f, 1175f, 0.12f, 1, decay = 6f) }.out()
        "click" -> Mix(0.05f).apply { tone(0f, 0.035f, 1200f, 900f, 0.12f, 0, 0.5f, decay = 60f) }.out()
        "storm" -> Mix(1.2f).apply { hiss(0f, 1.15f, 0.3f, 0.85f, 1.2f) }.out()
        "win" -> Mix(1.0f).apply { val n = intArrayOf(523, 659, 784, 1047, 784, 1047); n.forEachIndexed { i, f -> tone(i * 0.11f, if (i == n.size - 1) 0.4f else 0.12f, f.toFloat(), f.toFloat(), 0.16f, 0, 0.25f); tone(i * 0.11f, 0.12f, f / 2f, f / 2f, 0.08f, 1) } }.out()
        "lose" -> Mix(1.0f).apply { val n = intArrayOf(392, 370, 349, 330); n.forEachIndexed { i, f -> tone(i * 0.2f, 0.22f, f.toFloat(), f * 0.97f, 0.16f, 2, vib = 0.02f) } }.out()
        else -> Mix(0.06f).apply { tone(0f, 0.05f, 660f, 880f, 0.15f) }.out()
    }

    private fun midi(tok: String): Int {
        var pc = when (tok[0]) { 'C' -> 0; 'D' -> 2; 'E' -> 4; 'F' -> 5; 'G' -> 7; 'A' -> 9; 'B' -> 11; else -> return 0 }
        var i = 1
        while (i < tok.length && (tok[i] == '#' || tok[i] == 'b')) { pc += if (tok[i] == '#') 1 else -1; i++ }
        return 12 * (tok.substring(i).toInt() + 1) + pc
    }

    private fun toks(s: String) = s.split(' ').filter { it.isNotBlank() && it != "|" }
    private fun freq(m: Int) = 440.0 * 2.0.pow((m - 69) / 12.0)

    private fun render(s: Song): FloatArray {
        val mel = toks(s.mel); val bass = toks(s.bass)
        val steps = mel.size
        val sps = (RATE / s.sps).toInt()
        val n = steps * sps
        val mix = FloatArray(n)
        // lead with a tape-style echo, a soft octave-down double, bass, and a chiptune arpeggio over the bass root
        val lead = FloatArray(n)
        voice(lead, mel, steps, sps, 0.15f, 0)
        voice(lead, mel, steps, sps, 0.05f, 2, -12)
        val dly = sps * 3
        for (i in 0 until n) { mix[i] += lead[i]; val j = i + dly; mix[j % n] += lead[i] * 0.22f; val k = i + dly * 2; mix[k % n] += lead[i] * 0.09f }
        voice(mix, bass, steps, sps, 0.22f, 1)
        val arp = ArrayList<String>(steps); var root = -1
        for (st in 0 until steps) {
            val b = bass[st % bass.size]; if (b != "-" && b != ".") root = midi(b)
            arp.add(if (root < 0) "." else "#" + (root + 12 + intArrayOf(0, 7, 12, 7)[st % 4]))
        }
        voice(mix, arp, steps, sps, 0.035f, 2, 0, 0.55)
        var seed = 12345
        for (st in 0 until steps) {
            val d = s.drums[st % s.drums.length]
            if (d == '.') continue
            val start = st * sps
            val len = when (d) { 'D' -> (RATE * 0.16).toInt(); 'T' -> (RATE * 0.07).toInt(); else -> (RATE * 0.04).toInt() }
            var ph = 0.0
            for (i in 0 until len) {
                val idx = start + i; if (idx >= n) break
                val t = i.toDouble() / RATE
                seed = seed * 1103515245 + 12345
                val noise = ((seed shr 16) and 0x7fff) / 16384.0 - 1.0
                val v = when (d) {
                    'D' -> { ph += (110.0 * exp(-t * 18) + 55) / RATE; (sin(2 * PI * ph) * 0.5 + noise * 0.05) * exp(-t * 16) }
                    // doira "tak": ringing frame-drum skin plus a jingle burst
                    'T' -> { ph += 330.0 / RATE; sin(2 * PI * ph) * 0.12 * exp(-t * 30) + noise * 0.16 * exp(-t * 40) }
                    else -> noise * 0.12 * exp(-t * 70)
                }
                mix[idx] += v.toFloat()
            }
        }
        for (i in 0 until n) mix[i] = (mix[i] * 0.85f).coerceIn(-1f, 1f) * 0.55f
        return mix
    }

    /** wave: 0 pulse 25%, 1 triangle, 2 pulse 12.5%. Tokens "#60" are raw midi numbers. */
    private fun voice(mix: FloatArray, notes: List<String>, steps: Int, sps: Int, amp: Float, wave: Int, tr: Int = 0, gate: Double = 1.0) {
        val square = wave != 1
        if (notes.isEmpty()) return
        var st = 0
        while (st < steps) {
            val tok = notes[st % notes.size]
            if (tok == "-" || tok == ".") { st++; continue }
            var len = 1
            while (st + len < steps && notes[(st + len) % notes.size] == "-") len++
            val f = freq((if (tok[0] == '#') tok.substring(1).toInt() else midi(tok)) + tr); val start = st * sps; val total = (len * sps * gate).toInt()
            var ph = 0.0
            for (i in 0 until total) {
                val idx = start + i; if (idx >= mix.size) break
                val t = i.toDouble() / RATE
                val vib = if (square && t > 0.18) 1 + 0.006 * sin(2 * PI * 5.5 * t) else 1.0
                ph += f * vib / RATE
                val p = ph - floor(ph)
                val w = when (wave) { 0 -> (if (p < 0.25) 1.0 else -1.0) * 0.8; 2 -> (if (p < 0.125) 1.0 else -1.0) * 0.7; else -> 1 - 4 * abs(p - 0.5) }
                val atk = (i / (RATE * 0.006)).coerceAtMost(1.0)
                val rel = ((total - i) / (RATE * 0.03)).coerceAtMost(1.0)
                val env = atk * rel * (0.65 + 0.35 * exp(-t * 6))
                mix[idx] += (w * env * amp).toFloat()
            }
            st += len
        }
    }
}
