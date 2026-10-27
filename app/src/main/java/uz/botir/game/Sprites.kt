package uz.botir.game

import uz.botir.game.G.px

/** Character art: Botir (street variant), enemies, NPCs and item sprites. */
object Spr {
    private val LEGS = mapOf(
        "idle" to listOf(arrayOf(8, 24, 3, 5, 0), arrayOf(13, 24, 3, 5, 0), arrayOf(7, 29, 5, 2, 1), arrayOf(13, 29, 5, 2, 1)),
        "run0" to listOf(arrayOf(8, 24, 3, 3, 0), arrayOf(6, 26, 3, 2, 0), arrayOf(3, 26, 4, 3, 1), arrayOf(13, 24, 3, 3, 0), arrayOf(15, 26, 3, 3, 0), arrayOf(16, 29, 4, 2, 1)),
        "run1" to listOf(arrayOf(10, 24, 3, 5, 0), arrayOf(13, 24, 3, 3, 0), arrayOf(14, 26, 3, 2, 0), arrayOf(9, 29, 5, 2, 1), arrayOf(15, 27, 4, 2, 1)),
        "jump" to listOf(arrayOf(8, 24, 3, 3, 0), arrayOf(6, 26, 4, 2, 1), arrayOf(13, 24, 3, 2, 0), arrayOf(15, 25, 3, 2, 0), arrayOf(17, 26, 3, 2, 1)),
        "hang" to listOf(arrayOf(8, 24, 3, 6, 0), arrayOf(13, 24, 3, 6, 0), arrayOf(8, 29, 4, 2, 1), arrayOf(13, 29, 4, 2, 1)),
        "swim" to listOf(arrayOf(6, 24, 3, 3, 0), arrayOf(3, 25, 4, 2, 1), arrayOf(13, 24, 3, 3, 0), arrayOf(15, 26, 4, 2, 1))
    )
    private val ARMS_B = mapOf(
        "idle" to listOf(arrayOf(7, 16, 3, 7), arrayOf(7, 23, 3, 2)),
        "run0" to listOf(arrayOf(5, 16, 3, 5), arrayOf(4, 21, 3, 2)),
        "jump" to listOf(arrayOf(5, 10, 3, 6), arrayOf(5, 8, 3, 2)),
        "hang" to listOf(arrayOf(8, 8, 3, 8), arrayOf(8, 6, 3, 2)),
        "swim" to listOf(arrayOf(3, 16, 6, 3), arrayOf(1, 16, 2, 3))
    )
    private val ARMS_F = mapOf(
        "idle" to listOf(arrayOf(12, 16, 3, 7), arrayOf(12, 23, 3, 2)),
        "run0" to listOf(arrayOf(14, 16, 3, 3), arrayOf(15, 18, 3, 2), arrayOf(18, 18, 2, 2)),
        "jump" to listOf(arrayOf(16, 11, 3, 5), arrayOf(16, 9, 3, 2)),
        "hang" to listOf(arrayOf(13, 8, 3, 8), arrayOf(13, 6, 3, 2)),
        "swim" to listOf(arrayOf(15, 16, 6, 3), arrayOf(21, 16, 2, 3))
    )

    private fun legsFor(k: String) = LEGS[k] ?: LEGS[if (k == "dance") "idle" else "idle"]!!
    private fun armKey(k: String) = when (k) { "run1" -> "idle"; "dance" -> "jump"; else -> k }

    fun hero(lk: String, gold: Boolean): List<Part> {
        val doppi = if (gold) "#f4c430" else "#15151c"; val dw = if (gold) "#b8860b" else "#f5f5f5"
        val skin = "#e8ad7e"; val skinS = "#bf8255"; val hair = "#1c1410"; val body = "#f08a24"
        val out = ArrayList<Part>()
        val legKey = if (lk == "dance") "idle" else lk
        for (l in legsFor(legKey)) {
            if (l[4] == 0) out.add(P(l[0], l[1], l[2], l[3], "#3561a8"))
            else out.add(P(l[0], l[1], l[2], l[3], "#f5f5f5") { p -> px("#e03a3e", p.x + 1, p.y, p.w - 2, 1) })
        }
        out.add(P(3, 15, 4, 9, "#2e9b6b", true) { px("#1f6e4b", 3, 19, 4, 1); px("#f4c430", 4, 20, 1, 1) })
        val ak = armKey(lk)
        val b = ARMS_B[ak]!!
        b.forEachIndexed { i, r -> out.add(P(r[0], r[1], r[2], r[3], if (i == b.size - 1) skin else body, true)) }
        out.add(P(7, 15, 10, 10, body, true) {
            val ik = arrayOf("#5b2a86", "#f5f0e6", "#2457c5")
            for (rr in 0 until 3) for (i in 0 until 10) px(ik[(i + rr * 2) % 3], 7 + i, 18 + rr)
            px("#c96a12", 9, 22, 6, 2); px("#ffffff", 12, 15, 1, 3); px("#ffffff", 14, 15, 1, 3); px("#33000000", 7, 15, 1, 10)
        })
        out.add(P(6, 13, 6, 3, body, true))
        out.add(P(8, 5, 9, 9, skin, true) {
            px(hair, 8, 5, 3, 6); px(hair, 8, 11, 1, 2); px(skinS, 10, 9, 2, 2); px("#000000", 14, 8, 2, 2); px("#ffffff", 14, 8, 1, 1)
            px(hair, 13, 7, 3, 1); px("#8a3324", 15, 12, 2, 1); px("#e58a7a", 13, 11, 1, 1); px(skinS, 9, 13, 8, 1); px(hair, 11, 5, 4, 1)
        })
        out.add(P(17, 9, 1, 2, skin, true))
        out.add(P(7, 3, 11, 3, doppi, true))
        out.add(P(9, 1, 7, 2, doppi, true) {
            for (pt in arrayOf(8 to 4, 9 to 3, 9 to 4, 12 to 4, 13 to 3, 13 to 4, 16 to 4, 15 to 3, 11 to 1, 12 to 2, 14 to 1)) px(dw, pt.first, pt.second)
            px("#59000000", 7, 5, 11, 1)
        })
        val f = ARMS_F[ak]!!
        f.forEachIndexed { i, r -> out.add(P(r[0], r[1], r[2], r[3], if (i == f.size - 1) skin else body, true)) }
        return out
    }

    fun enemy(t: Char, f: Int): List<Part> = when (t) {
        'g' -> { val lo = if (f == 1) 1 else 0; listOf(
            P(5, 11, 1, 3 - lo, "#f08a24"), P(8, 11, 1, 2 + lo, "#f08a24"),
            P(2, 5, 10, 6, "#f2f2f2") { px("#cfcfd6", 4, 6, 5, 2); px("#cfcfd6", 2, 10, 10, 1) },
            P(10, 1, 2, 6, "#f2f2f2"), P(14, 1, 3, 2, "#f08a24") { px("#c25e10", 14, 2, 3, 1) },
            P(10, 0, 4, 3, "#f2f2f2") { px("#000000", 12, 1); px("#000000", 11, 0, 2, 1) }) }
        'k' -> listOf(
            P(1, 19, 13, 2, "#6b7280"), P(1, 21, 3, 2, "#222222"), P(11, 21, 3, 2, "#222222"), P(12, 7, 1, 12, "#9aa1ab"), P(11, 7, 3, 1, "#2b2b33"),
            P(5, 14, 3, 5, "#3561a8"), P(8, 14 - f, 3, 5, "#2c4f8c"), P(4, 8, 7, 6, "#d8373b") { px("#f5f5f5", 6, 10, 3, 1) },
            P(5, 2, 6, 6, "#e8ad7e") { px("#000000", 9, 4); px("#8a3324", 8, 6, 2, 1) }, P(4, 1, 7, 2, "#2457c5"), P(11, 2, 3, 1, "#2457c5"), P(9, 9, 3, 2, "#e8ad7e"))
        's' -> listOf(
            P(5, 14, 3, 7, "#3a2c24"), P(8, 14 - f, 3, 7, "#3a2c24"), P(4, 7, 7, 8, "#f5f0e6") { px("#cfc8b8", 4, 11, 7, 1) },
            P(5, 1, 6, 6, "#e8ad7e") { px("#000000", 9, 3); px("#1c1410", 7, 5, 4, 1) }, P(4, 0, 8, 2, "#15151c") { px("#f5f5f5", 6, 0, 1, 1) },
            P(12, 2 + f, 1, 15, "#8b5a2b"), P(11, 16 + f, 4, 5, "#e9c94a") { p -> px("#b89a2a", p.x + 1, p.y, 1, p.h) }, P(9, 9, 3, 2, "#e8ad7e"))
        'c' -> listOf(
            P(5, 15, 3, 6, "#1f2430"), P(8, 15 - f, 3, 6, "#1f2430"), P(4, 8, 7, 8, "#2b2f3a") { px("#f5f5f5", 6, 8, 3, 3); px("#d8373b", 7, 9, 1, 4) },
            P(5, 2, 6, 6, "#e8ad7e") { px("#000000", 6, 4, 5, 1); px("#8a3324", 8, 6, 2, 1) }, P(5, 1, 6, 2, "#1c1410"),
            P(11, 12, 4, 4, "#6b3f1f") { px("#f4c430", 12, 12, 2, 1) })
        'b' -> listOf(
            P(1, 3, 9, 4, "#8a93a6") { px("#5f6b80", 3, 4, 5, 1) }, P(8, 1, 4, 3, "#6b7690") { px("#ff7a00", 10, 2); px("#2fb39a", 8, 3, 3, 1) },
            P(12, 2, 2, 1, "#e8b25a"), P(3, if (f == 1) 0 else 5, 5, 2, "#a7b0c2"))
        else -> listOf( // humo bird
            P(0, if (f == 1) 0 else 5, 7, 3, "#f4c430"), P(10, if (f == 1) 0 else 5, 7, 3, "#f4c430"),
            P(5, 3, 8, 5, "#e6a23c") { px("#000000", 11, 4) }, P(13, 4, 3, 2, "#d8373b"), P(2, 5, 4, 2, "#f4c430"))
    }

    fun enemyH(t: Char) = when (t) { 'g' -> 14f; 'k' -> 24f; 's' -> 22f; 'c' -> 22f; 'b' -> 8f; else -> 9f }

    fun person(xx: Float, yy: Float, body: String, hat: String, atlas: Boolean = false, scarf: Boolean = false, beard: Boolean = false, glasses: Boolean = false, arm: Int = 0, skin: String = "#e8ad7e") {
        val x = Math.round(xx).toFloat(); val y = Math.round(yy).toFloat()
        val l = arrayListOf(
            P(x + 2, y + 13, 2, 5, "#3a2c24"), P(x + 6, y + 13, 2, 5, "#3a2c24"),
            P(x + 1, y + 6, 8, 8, body) { if (atlas) { val cs = arrayOf("#d8373b", "#f4c430", "#2e9b6b", "#2457c5"); for (i in 0 until 4) px(cs[i], x + 1 + i * 2, y + 6, 2, 8) } },
            P(x + 2, y + 1, 6, 6, skin) { px("#000000", x + 6, y + 3); if (beard) px("#f2f2f2", x + 3, y + 5, 5, 2); if (glasses) px("#000000", x + 3, y + 3, 5, 1) },
            P(x + 1, y, 8, if (scarf) 5 else 2, hat))
        if (arm != 0) l.add(P(x + 8, y + arm, 3, 2, skin))
        G.drawParts(l, 0f)
    }

    val NON = arrayOf("..KKKKKK..", ".KNNNNNNK.", "KNNnNNnNNK", "KNNNLLNNNK", "KNnLnnLnNK", "KNNLnnLNNK", "KNNNLLNNNK", "KNnNNNNnNK", ".KNNNNNNK.", "..KKKKKK..")
    val SOM = arrayOf("....KK....", "...KQQK...", "..KQQqQK..", "..KQQQQK..", ".KQqQQQQK.", ".KQQQQqQK.", "KQQQQQQQQK", "KKKKKKKKKK")
    val DOP = arrayOf("...KKKKKK...", "..KGGgGGGK..", ".KGgGGGgGGK.", "KGGGGgGGGGGK", "KgGGgGGgGGgK", "KKKKKKKKKKKK")
    val HRT = arrayOf(".KK.KK.", "KRRKRRK", "KRRRRRK", ".KRRRK.", "..KRK..", "...K...")
    val QOZ = arrayOf("..KKKKKKKKKK..", ".KOOOOOOOOOOK.", "KKKKKKKKKKKKKK", "KDDDDDDDDDDDDK", "KDdDDDDDDDDdDK", ".KDDDDDDDDDDK.", "..KKKKKKKKKK..")
    val SX = mapOf(
        "sabzi" to arrayOf("...GG.", "..GG..", ".KKK..", "KYYYK.", "KYYK..", "KYK...", "KK...."),
        "jeton" to arrayOf(".KKKK.", "KCCCCK", "KCkkCK", "KCkkCK", "KCCCCK", ".KKKK."),
        "foto" to arrayOf("..KK..", "KKKKKK", "KXXXXK", "KX..XK", "KXXXXK", "KKKKKK"))
    val PAL = mapOf('N' to "#d9963f", 'n' to "#8a5422", 'L' to "#f0c27a", 'Q' to "#e8b25a", 'q' to "#a8702a", 'G' to "#f4c430", 'g' to "#b8860b",
        'O' to "#55556a", 'D' to "#2b2b33", 'd' to "#55556a", 'Y' to "#f4a722", 'C' to "#c98a3a", 'k' to "#8a5422", 'X' to "#3a3f4a", 'R' to "#e24b4a")
    fun pal(vararg over: Pair<Char, String>): Map<Char, String> { val m = HashMap(PAL); for (o in over) m[o.first] = o.second; return m }
}
