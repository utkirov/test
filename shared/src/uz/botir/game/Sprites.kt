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

    fun hero(lk: String, gold: Boolean, sk: Skin = Skins.ALL[0], sink: Int = 0): List<Part> {
        val doppi = if (gold) "#f4c430" else sk.doppi; val dw = if (gold) "#b8860b" else sk.dw
        val skin = "#e8ad7e"; val skinS = "#bf8255"; val hair = "#1c1410"; val body = sk.body
        val out = ArrayList<Part>()
        val legKey = if (lk == "dance") "idle" else lk
        for (l in legsFor(legKey)) {
            if (l[1] + l[3] > 31 - sink && sink > 0) { val h = (31 - sink - l[1]).coerceAtLeast(0); if (h > 0) out.add(P(l[0], l[1], l[2], h, if (l[4] == 0) sk.pants else sk.shoe)); continue }
            if (l[4] == 0) out.add(P(l[0], l[1], l[2], l[3], sk.pants))
            else out.add(P(l[0], l[1], l[2], l[3], sk.shoe) { p -> px(sk.stripe, p.x + 1, p.y, p.w - 2, 1) })
        }
        if (sk.pack) out.add(P(3, 15, 4, 9, "#2e9b6b", true) { px("#1f6e4b", 3, 19, 4, 1); px("#f4c430", 4, 20, 1, 1) })
        val ak = armKey(lk)
        val b = ARMS_B[ak]!!
        b.forEachIndexed { i, r -> out.add(P(r[0], r[1], r[2], r[3], if (i == b.size - 1) skin else body, true)) }
        out.add(P(7, 15, 10, 10, body, true, sk.torso))
        if (sk.hood) out.add(P(6, 13, 6, 3, body, true))
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
        if (sk.season == Season.WINTER && !gold) out.add(P(11, -2, 3, 3, "#ffffff", true))
        val f = ARMS_F[ak]!!
        f.forEachIndexed { i, r -> out.add(P(r[0], r[1], r[2], r[3], if (i == f.size - 1) skin else body, true)) }
        return out
    }

    /** Enemy art. f = animation frame 0..3. Local box ~16 wide, feet at the bottom of [enemyH]. */
    fun enemy(t: Char, f0: Int): List<Part> {
        val f = ((f0 % 4) + 4) % 4
        val sw = if (f % 2 == 0) 0 else 1          // two-step bob
        val alt = if (f < 2) 1 else -1              // alternating limbs
        return when (t) {
            'g' -> { // angry mahalla goose, waddling
                val nb = if (f == 1 || f == 3) 1 else 0
                listOf(
                    P(5, 11, 1, 3 + (if (alt > 0) 0 else -1), "#f08a24"), P(9, 11, 1, 3 + (if (alt > 0) -1 else 0), "#f08a24"),
                    P(4 - alt, 13, 3, 1, "#e07a14"), P(8 + alt, 13, 3, 1, "#e07a14"),
                    P(1, 5 + sw, 11, 7, "#f4f4f4") { px("#d9d9e2", 3, 7 + sw, 6, 3); px("#c4c4cf", 2, 8 + sw, 2, 2); px("#ffffff", 5, 5 + sw, 4, 1); px("#c4c4cf", 1, 11 + sw, 11, 1) },
                    P(0, 6 + sw, 2, 3, "#e6e6ee"),
                    P(10 + nb, 1 + sw, 2, 6, "#f4f4f4") { px("#d9d9e2", 10 + nb, 4 + sw, 1, 3) },
                    P(10 + nb, 0 + sw, 4, 3, "#f4f4f4") { px("#000000", 12 + nb, 1 + sw); px("#5a0000", 11 + nb, 0 + sw, 2, 1) },
                    P(14 + nb, 1 + sw, 3, 2, "#f08a24") { px("#c25e10", 14 + nb, 2 + sw, 3, 1) })
            }
            'k' -> { // kid on a scooter
                val wheel: (Part) -> Unit = { pp -> px("#9aa1ab", pp.x + 1, pp.y + 1, 1, 1); if (f % 2 == 0) px("#444444", pp.x, pp.y + 1, 3, 1) else px("#444444", pp.x + 1, pp.y, 1, 3) }
                val push = if (f < 2) 2 else -1
                listOf(
                    P(0, 20, 4, 3, "#222222", d = wheel), P(11, 20, 4, 3, "#222222", d = wheel),
                    P(1, 19, 13, 2, "#6b7280") { px("#d8373b", 2, 19, 11, 1) },
                    P(12, 7, 1, 12, "#9aa1ab"), P(10, 6, 5, 1, "#2b2b33"),
                    P(6, 14, 3, 5, "#2c4f8c"), P(4 + push, 14, 3, 4 + (if (push > 0) 1 else 3), "#3561a8"),
                    P(3, 8 + sw, 8, 7, "#d8373b") { px("#f5f5f5", 5, 10 + sw, 4, 1); px("#b02a2e", 3, 8 + sw, 1, 7) },
                    P(5, 1 + sw, 7, 7, "#e8ad7e") { px("#2a1810", 10, 3 + sw); px("#ffffff", 9, 3 + sw); px("#8a3324", 9, 6 + sw, 2, 1); px("#1c1410", 5, 3 + sw, 2, 3) },
                    P(3, 0 + sw, 8, 2, "#2457c5") { px("#173b8a", 3, 1 + sw, 8, 1) }, P(2, 1 + sw, 2, 1, "#2457c5"),
                    P(10, 9 + sw, 3, 2, "#e8ad7e"))
            }
            's' -> { // bazaar seller with a broom
                val br = if (f < 2) 0 else 1
                listOf(
                    P(5, 15, 3, 6 + (if (alt > 0) 0 else -1), "#3a2c24"), P(8, 15, 3, 6 + (if (alt > 0) -1 else 0), "#3a2c24"),
                    P(4, 20 + (if (alt > 0) 0 else -1), 4, 1, "#1c1410"), P(8, 20 + (if (alt > 0) -1 else 0), 4, 1, "#1c1410"),
                    P(3, 7 + sw, 9, 9, "#f5f0e6") { px("#d9d2c2", 3, 7 + sw, 1, 9); px("#2457c5", 4, 12 + sw, 7, 4); px("#173b8a", 4, 12 + sw, 7, 1) },
                    P(5, 1 + sw, 6, 6, "#e8ad7e") { px("#2a1810", 9, 3 + sw); px("#1c1410", 7, 5 + sw, 4, 1); px("#c98a5e", 5, 4 + sw, 1, 2) },
                    P(4, 0 + sw, 8, 2, "#15151c") { px("#f5f5f5", 6, 0 + sw, 1, 1); px("#f5f5f5", 9, 0 + sw, 1, 1) },
                    P(12 + br, 2 + sw, 1, 14, "#8b5a2b"),
                    P(11 + br * 2, 15 + sw, 5, 6, "#e9c94a") { pp -> for (k in 0 until 5) px(if (k % 2 == 0) "#c9a227" else "#f4dc7a", pp.x + k, pp.y + 1, 1, 5) },
                    P(10, 8 + sw, 3, 2, "#e8ad7e"))
            }
            'c' -> { // office clerk with a briefcase
                val bc = if (alt > 0) 0 else 1
                listOf(
                    P(5, 15, 3, 6 + (if (alt > 0) 0 else -1), "#1f2430"), P(8, 15, 3, 6 + (if (alt > 0) -1 else 0), "#1f2430"),
                    P(4, 20 + (if (alt > 0) 0 else -1), 4, 1, "#0e0e12"), P(8, 20 + (if (alt > 0) -1 else 0), 4, 1, "#0e0e12"),
                    P(3, 8 + sw, 9, 8, "#2b2f3a") { px("#f5f5f5", 6, 8 + sw, 3, 3); px("#d8373b", 7, 9 + sw, 1, 4 + (f % 2)); px("#3a4050", 11, 8 + sw, 1, 8) },
                    P(5, 2 + sw, 6, 6, "#e8ad7e") { px("#000000", 6, 4 + sw, 5, 1); px("#9ad0ff", 9, 4 + sw, 1, 1); px("#8a3324", 8, 6 + sw, 2, 1) },
                    P(5, 1 + sw, 6, 2, "#1c1410") { px("#3a2a20", 6, 1 + sw, 3, 1) },
                    P(11 + bc, 13 + sw, 5, 4, "#6b3f1f") { px("#f4c430", 13 + bc, 13 + sw, 1, 1); px("#4a2a14", 11 + bc, 16 + sw, 5, 1) },
                    P(11, 9 + sw, 2, 4, "#2b2f3a"))
            }
            'b' -> { // pigeon flapping
                val wy = intArrayOf(-3, 0, 4, 0)[f]
                listOf(
                    P(1, 3, 9, 4, "#8a93a6") { px("#5f6b80", 3, 5, 5, 1); px("#a7b0c2", 2, 3, 6, 1); px("#454d5e", 0, 4, 2, 2) },
                    P(8, 1, 4, 4, "#6b7690") { px("#ff7a00", 10, 2); px("#2fb39a", 8, 3, 2, 1); px("#9a5ab8", 8, 4, 2, 1) },
                    P(12, 2, 2, 1, "#e8b25a"),
                    P(3, 3 + wy.coerceAtMost(0), 6, 2 + kotlin.math.abs(wy).coerceAtMost(2), "#a7b0c2") { px("#5f6b80", 3, 3 + wy.coerceAtMost(0), 1, 2) })
            }
            'z' -> { // lizard (kaltakesak), tail wiggling
                val tw = intArrayOf(0, 1, 0, -1)[f]
                listOf(
                    P(0, 4 + tw, 3, 1, "#6b7a2a"), P(2, 4, 2, 1, "#7a8a3a"),
                    P(3, 3, 9, 4, "#9aaa4a") { px("#6b7a2a", 5, 4, 1, 1); px("#6b7a2a", 8, 4, 1, 1); px("#c4d06a", 4, 3, 7, 1) },
                    P(11, 2, 5, 4, "#9aaa4a") { px("#000000", 14, 3); px("#e24b4a", 16, 4, 1, 1) },
                    P(4, 7, 2, 1 + (if (alt > 0) 0 else 1), "#6b7a2a"), P(9, 7, 2, 1 + (if (alt > 0) 1 else 0), "#6b7a2a"))
            }
            else -> { // Humo, the golden bird of happiness
                val wy = intArrayOf(-5, -1, 4, -1)[f]
                listOf(
                    P(-3, 5, 5, 2, "#f4c430") { px("#d8373b", -3, 6, 3, 1) }, P(-5, 6, 3, 1, "#e6a23c"),
                    P(3, 3 + wy.coerceAtMost(0), 9, 3 + kotlin.math.abs(wy).coerceAtMost(3), "#f4c430") { px("#fff1a0", 4, 3 + wy.coerceAtMost(0), 6, 1); px("#e6a23c", 3, 5 + wy.coerceAtMost(0), 9, 1) },
                    P(4, 3, 9, 5, "#e6a23c") { px("#f4c430", 5, 4, 6, 2) },
                    P(12, 1, 4, 4, "#e6a23c") { px("#000000", 14, 2); px("#ffffff", 14, 2 - 0, 0, 0) },
                    P(16, 2, 3, 2, "#d8373b"), P(12, -1, 3, 2, "#d8373b"))
            }
        }
    }

    fun enemyH(t: Char) = when (t) { 'z' -> 8f; 'g' -> 15f; 'k' -> 24f; 's' -> 22f; 'c' -> 22f; 'b' -> 8f; else -> 9f }

    fun person(xx: Float, yy: Float, body: String, hat: String, atlas: Boolean = false, scarf: Boolean = false, beard: Boolean = false, glasses: Boolean = false, arm: Int = 0, skin: String = "#e8ad7e") {
        val x = G.round(xx); val y = G.round(yy)
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
    val KAF = arrayOf("KKKKK.........", "KGGGK.........", "KGgGKKKKKKKKKK", "KGGGGGGGGGGGGK", "KKKKKKKKKKKKKK")
    val SX = mapOf(
        "sabzi" to arrayOf("...GG.", "..GG..", ".KKK..", "KYYYK.", "KYYK..", "KYK...", "KK...."),
        "jeton" to arrayOf(".KKKK.", "KCCCCK", "KCkkCK", "KCkkCK", "KCCCCK", ".KKKK."),
        "foto" to arrayOf("..KK..", "KKKKKK", "KXXXXK", "KX..XK", "KXXXXK", "KKKKKK"),
        "samnon" to arrayOf(".KKKKKK.", "KNNNNNNK", "KNLnnLNK", "KNnLLnNK", "KNLnnLNK", "KNNNNNNK", ".KKKKKK."),
        "qogoz" to arrayOf("KKKKKK", "KWWWWK", "KWkkWK", "KWWWWK", "KWkkWK", "KKKKKK"),
        "uzuk" to arrayOf("..KK..", ".KRRK.", "KGKKGK", "KG..GK", "KGKKGK", ".KGGK."))
    val PAL = mapOf('N' to "#d9963f", 'n' to "#8a5422", 'L' to "#f0c27a", 'Q' to "#e8b25a", 'q' to "#a8702a", 'G' to "#f4c430", 'g' to "#b8860b",
        'O' to "#55556a", 'D' to "#2b2b33", 'd' to "#55556a", 'Y' to "#f4a722", 'C' to "#c98a3a", 'k' to "#8a5422", 'X' to "#3a3f4a", 'R' to "#e24b4a", 'W' to "#f5f0e6")
    fun pal(vararg over: Pair<Char, String>): Map<Char, String> { val m = HashMap(PAL); for (o in over) m[o.first] = o.second; return m }
}
