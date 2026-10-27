package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt
import java.util.Calendar
import kotlin.math.sin

/** Seasonal events: New Year snow, Navruz petals, Ramadan crescent and lanterns. */
object Season {
    const val NONE = 0; const val WINTER = 1; const val NAVRUZ = 2; const val RAMADAN = 3

    // Approximate Ramadan dates (month, day) start..end for upcoming years.
    private val RAMADAN_DATES = mapOf(
        2026 to intArrayOf(2, 18, 3, 19), 2027 to intArrayOf(2, 8, 3, 9), 2028 to intArrayOf(1, 28, 2, 26),
        2029 to intArrayOf(1, 16, 2, 14), 2030 to intArrayOf(1, 6, 2, 4)
    )

    fun auto(): Int {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR); val m = c.get(Calendar.MONTH) + 1; val d = c.get(Calendar.DAY_OF_MONTH)
        val md = m * 100 + d
        if (md in 315..331) return NAVRUZ
        RAMADAN_DATES[y]?.let { r -> if (md in (r[0] * 100 + r[1])..(r[2] * 100 + r[3])) return RAMADAN }
        if (md >= 1215 || md <= 115) return WINTER
        return NONE
    }

    /** setting: 0 auto, 1 off, 2 winter, 3 navruz, 4 ramadan */
    fun resolve(setting: Int) = when (setting) { 0 -> auto(); 2 -> WINTER; 3 -> NAVRUZ; 4 -> RAMADAN; else -> NONE }

    fun name(s: Int, g: Game) = when (s) {
        WINTER -> g.L("Новый год", "Yangi yil"); NAVRUZ -> g.L("Навруз", "Navro'z"); RAMADAN -> g.L("Рамазан", "Ramazon"); else -> g.L("выкл", "o'chiq")
    }

    fun greeting(s: Int, g: Game) = when (s) {
        WINTER -> g.L("С Новым годом! Скин «Корбобо» в магазине бесплатно.", "Yangi yilingiz bilan! «Qorbobo» libosi do'konda tekin.")
        NAVRUZ -> g.L("Navro'z muborak! Весна пришла в Ташкент.", "Navro'z muborak! Toshkentga bahor keldi.")
        RAMADAN -> g.L("Ramazon muborak! Добрых вечеров и щедрых ифтаров.", "Ramazon muborak! Iftorlaringiz barakali bo'lsin.")
        else -> ""
    }

    /** Particle overlay drawn above the world. */
    fun overlay(s: Int, frame: Int) {
        when (s) {
            WINTER -> for (i in 0 until 40) {
                val sp = 0.25f + (i % 3) * 0.15f
                val x = (hash(i, 11) % W) + sin((frame + i * 20) / 40f) * 6
                val y = ((hash(i, 12) % H) + frame * sp) % H
                px("#ffffff", x, y, if (i % 4 == 0) 2 else 1, if (i % 4 == 0) 2 else 1)
            }
            NAVRUZ -> for (i in 0 until 24) {
                val x = ((hash(i, 21) % (W + 40)) - frame * (0.3f + (i % 3) * 0.1f)).let { ((it % (W + 40)) + W + 40) % (W + 40) - 20 }
                val y = ((hash(i, 22) % H) + frame * 0.35f + sin((frame + i * 13) / 25f) * 4) % H
                px(if (i % 3 == 0) "#f7c6d9" else if (i % 3 == 1) "#ffe28a" else "#ffffff", x, y, 2, 1)
            }
            RAMADAN -> {
                px("#14341c66", 0, 0, W, H)
                G.circle("#ffe9a8", W - 22, 22, 9); G.circle("#1d2b53", W - 18, 19, 8)
                for (i in 0 until 14) { val x = hash(i, 31) % W; val y = hash(i, 32) % 90; if ((frame / 20 + i) % 5 != 0) px("#fff6c8", x, y) }
            }
        }
    }

    /** Decorations on the title screen. */
    fun titleDecor(s: Int, frame: Int) {
        when (s) {
            WINTER -> { for (i in 0 until 18) { val c = arrayOf("#e24b4a", "#f4c430", "#2e9b6b", "#2e6fd8")[(i + frame / 15) % 4]; px("#2b2b33", i * 10, 3, 10, 1); px(c, i * 10 + 4, 4 + (i % 2) * 2, 3, 3) } }
            NAVRUZ -> { for (i in 0 until 9) { val x = 6 + i * 20; px("#2e9b6b", x + 2, 257, 1, 8); px(if (i % 2 == 0) "#e24b4a" else "#f4c430", x, 252, 5, 5) } }
            RAMADAN -> for (i in 0 until 5) { val x = 14 + i * 38; val sw = (sin((frame + i * 30) / 30f) * 2); px("#c9a227", x + 3, 0, 1, 10 + i % 2 * 6); G.c.save(); G.c.translate(sw, 0f)
                px(OUT, x, 10 + i % 2 * 6, 7, 10); px("#f4c430", x + 1, 11 + i % 2 * 6, 5, 8); px("#fff6c8", x + 2, 13 + i % 2 * 6, 3, 4); G.c.restore() }
        }
    }

    fun banner(s: Int, g: Game, y: Float) { if (s != NONE) txt(name(s, g).uppercase() + "!", W / 2f, y, "#ffe28a", SM, 1) }
}
