package uz.botir.game

import uz.botir.game.G.px

/** Outfits for Botir sold in Grandpa's shop. Torso painters draw in hero-local pixels (torso is x 7..16, y 15..24). */
class Skin(
    val id: String, val n: Array<String>, val price: Int,
    val body: String, val pants: String, val shoe: String, val stripe: String,
    val doppi: String, val dw: String,
    val hood: Boolean = false, val pack: Boolean = false, val season: Int = 0,
    val torso: (Part) -> Unit
)

object Skins {
    val ALL = listOf(
        Skin("street", arrayOf("Уличный", "Ko'cha"), 0, "#f08a24", "#3561a8", "#f5f5f5", "#e03a3e", "#15151c", "#f5f5f5", hood = true, pack = true) {
            val ik = arrayOf("#5b2a86", "#f5f0e6", "#2457c5")
            for (r in 0 until 3) for (i in 0 until 10) px(ik[(i + r * 2) % 3], 7 + i, 18 + r)
            px("#c96a12", 9, 22, 6, 2); px("#ffffff", 12, 15, 1, 3); px("#ffffff", 14, 15, 1, 3); px("#33000000", 7, 15, 1, 10)
        },
        Skin("chapan", arrayOf("Чапан", "Chopon"), 60, "#2457c5", "#3a2c24", "#5c3a1e", "#5c3a1e", "#15151c", "#f5f5f5") {
            val st = arrayOf("#2457c5", "#2f9e5a", "#d8373b")
            for (i in 0 until 10) px(st[(i / 2) % 3], 7 + i, 15, 1, 10)
            px("#f4efe2", 12, 15, 2, 3); px("#e03a3e", 7, 21, 10, 2); px("#33000000", 7, 15, 1, 10)
        },
        Skin("futbol", arrayOf("Футбольная форма", "Futbol formasi"), 90, "#2e9b6b", "#f5f5f5", "#111111", "#2e9b6b", "#15151c", "#f5f5f5") {
            px("#f5f5f5", 11, 15, 3, 1); px("#f5f5f5", 7, 15, 1, 10); px("#f5f5f5", 16, 15, 1, 10)
            // number 10
            px("#f5f5f5", 9, 17, 1, 5); px("#f5f5f5", 11, 17, 3, 1); px("#f5f5f5", 11, 21, 3, 1); px("#f5f5f5", 11, 17, 1, 5); px("#f5f5f5", 13, 17, 1, 5)
        },
        Skin("navruz", arrayOf("Навруз", "Navro'z"), 120, "#d8373b", "#5b2a86", "#5c3a1e", "#5c3a1e", "#f4c430", "#d8373b") {
            val cs = arrayOf("#d8373b", "#f4c430", "#2e9b6b", "#2457c5", "#f5f0e6")
            for (i in 0 until 10) for (r in 0 until 10) if ((i + r) % 4 == 0) px(cs[(i / 2 + r / 3) % 5], 7 + i, 15 + r)
            px("#f4c430", 7, 22, 10, 1)
        },
        Skin("batir", arrayOf("Батыр", "Botir"), 160, "#1f6b4a", "#2b2b33", "#6b3f1f", "#6b3f1f", "#1f6b4a", "#f2c14e") {
            px("#e9e1cc", 13, 15, 3, 3); px("#f2c14e", 12, 15, 1, 10); px("#d8a531", 7, 21, 10, 2); px("#8b6b1a", 11, 21, 2, 2)
            for (pt in arrayOf(9 to 17, 9 to 23, 15 to 23, 15 to 18)) px("#f2c14e", pt.first, pt.second)
        },
        Skin("qorbobo", arrayOf("Корбобо (зима)", "Qorbobo (qish)"), 0, "#d8373b", "#d8373b", "#111111", "#111111", "#d8373b", "#ffffff", season = Season.WINTER) {
            px("#ffffff", 7, 24, 10, 1); px("#ffffff", 11, 15, 2, 10); px("#111111", 7, 21, 10, 2); px("#f4c430", 11, 21, 2, 2)
        }
    )

    fun get(id: String) = ALL.firstOrNull { it.id == id } ?: ALL[0]
}

/** Upgrades sold in the shop. */
class Upgrade(val id: String, val n: Array<String>, val d: Array<String>, val price: Int)

object Upgrades {
    val ALL = listOf(
        Upgrade("heart", arrayOf("Лишнее сердце", "Qo'shimcha yurak"), arrayOf("Старт с 4 сердцами", "4 ta yurak bilan boshlash"), 120),
        Upgrade("magnet", arrayOf("Магнит для нонов", "Non magniti"), arrayOf("Ноны летят к тебе", "Nonlar o'zi uchib keladi"), 90),
        Upgrade("doppi", arrayOf("Крепкая дуппи", "Mustahkam do'ppi"), arrayOf("Дуппи-сила дольше", "Do'ppi kuchi uzoqroq"), 70)
    )
}
