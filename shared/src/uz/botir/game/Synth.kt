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
            "C3 . G2 . C3 . G2 . F2 . C3 . F2 . C3 . G2 . D3 . G2 . D3 . C3 . G2 . C3 . . .", "D.t.DDt.")
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
        voice(mix, mel, steps, sps, 0.16f, square = true)
        voice(mix, bass, steps, sps, 0.22f, square = false)
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
                    'T' -> noise * 0.22 * exp(-t * 45)
                    else -> noise * 0.12 * exp(-t * 70)
                }
                mix[idx] += v.toFloat()
            }
        }
        for (i in 0 until n) mix[i] = (mix[i] * 0.85f).coerceIn(-1f, 1f) * 0.55f
        return mix
    }

    private fun voice(mix: FloatArray, notes: List<String>, steps: Int, sps: Int, amp: Float, square: Boolean) {
        if (notes.isEmpty()) return
        var st = 0
        while (st < steps) {
            val tok = notes[st % notes.size]
            if (tok == "-" || tok == ".") { st++; continue }
            var len = 1
            while (st + len < steps && notes[(st + len) % notes.size] == "-") len++
            val f = freq(midi(tok)); val start = st * sps; val total = len * sps
            var ph = 0.0
            for (i in 0 until total) {
                val idx = start + i; if (idx >= mix.size) break
                val t = i.toDouble() / RATE
                val vib = if (square && t > 0.18) 1 + 0.006 * sin(2 * PI * 5.5 * t) else 1.0
                ph += f * vib / RATE
                val p = ph - floor(ph)
                val w = if (square) (if (p < 0.25) 1.0 else -1.0) * 0.8 else 1 - 4 * abs(p - 0.5)
                val atk = (i / (RATE * 0.006)).coerceAtMost(1.0)
                val rel = ((total - i) / (RATE * 0.03)).coerceAtMost(1.0)
                val env = atk * rel * (0.65 + 0.35 * exp(-t * 6))
                mix[idx] += (w * env * amp).toFloat()
            }
            st += len
        }
    }
}
