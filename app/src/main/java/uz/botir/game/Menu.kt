package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt

/** All menu screens are drawn in pixel-art right on the game bitmap; buttons are tap rectangles in game units. */
object Menu {
    private fun btn(g: Game, x: Number, y: Number, w: Number, h: Number, label: String, sub: String = "", style: Int = 0, act: () -> Unit) =
        g.btns.add(Btn(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), label, sub, style, act))

    fun layout(g: Game) {
        g.btns.clear()
        val L = g::L
        when (g.state) {
            "title" -> {
                btn(g, 22, 48, 34, 14, "UZ", style = if (g.lang == "uz") 3 else 1) { g.changeLang("uz") }
                btn(g, 60, 48, 34, 14, "RU", style = if (g.lang == "ru") 3 else 1) { g.changeLang("ru") }
                btn(g, 98, 48, 60, 14, L("ЗВУК", "OVOZ") + (if (Sfx.enabled) " +" else " -"), style = 1) { Sfx.enabled = !Sfx.enabled; layout(g) }
                g.levels.forEachIndexed { i, l ->
                    val locked = i > g.unlock
                    val x = if (i % 2 == 0) 10 else 94; val y = 128 + (i / 2) * 40
                    btn(g, x, y, 76, 32, L(l.n[0], l.n[1]),
                        if (locked) L("закрыто", "yopiq") else "* ${g.savedEggs(i).size}/${g.eggTotals[i]}", if (locked) 2 else 1) { if (!locked) g.goIntro(i) }
                }
            }
            "intro" -> {
                btn(g, 40, 214, 100, 20, L("ИГРАТЬ", "O'YNASH")) { g.play(g.introI) }
                btn(g, 40, 240, 100, 18, L("НАЗАД", "ORQAGA"), style = 1) { g.show("title") }
            }
            "pause" -> {
                btn(g, 40, 186, 100, 18, L("ПРОДОЛЖИТЬ", "DAVOM")) { g.state = "play"; g.btns.clear() }
                btn(g, 40, 210, 100, 18, L("ЗАНОВО", "QAYTA"), style = 1) { g.play(g.LVI) }
                btn(g, 40, 234, 100, 18, L("МЕНЮ", "MENYU"), style = 1) { g.loadLevel(0); g.show("title") }
            }
            "over" -> {
                btn(g, 40, 210, 100, 18, L("ЕЩЁ РАЗ", "YANA")) { g.play(g.LVI) }
                btn(g, 40, 234, 100, 18, L("МЕНЮ", "MENYU"), style = 1) { g.loadLevel(0); g.show("title") }
            }
            "win" -> {
                g.winUnlock()
                val nx = g.LVI + 1 < g.levels.size
                if (nx) btn(g, 20, 190, 140, 18, L("ДАЛЬШЕ", "KEYINGI") + ": " + L(g.levels[g.LVI + 1].n[0], g.levels[g.LVI + 1].n[1])) { g.goIntro(g.LVI + 1) }
                btn(g, 40, 214, 100, 18, L("ЕЩЁ РАЗ", "YANA"), style = 1) { g.play(g.LVI) }
                btn(g, 40, 238, 100, 18, L("МЕНЮ", "MENYU"), style = 1) { g.loadLevel(0); g.show("title") }
            }
            "final" -> {
                g.winUnlock()
                btn(g, 40, 238, 100, 18, L("МЕНЮ", "MENYU")) { g.loadLevel(0); g.show("title") }
            }
        }
    }

    private fun para(s: String, y: Float, col: String = "#a8b3d9", w: Float = 160f): Float {
        var yy = y
        for (l in G.wrap(s, w, SM)) { txt(l, W / 2f, yy, col, SM, 1); yy += 9 }
        return yy
    }

    private fun eggList(g: Game, y0: Float): Float {
        var y = y0
        val saved = g.savedEggs(g.LVI)
        for (z in g.zones) {
            val s = when {
                g.found.contains(z.id) -> Pair("* " + g.L(z.n[0], z.n[1]), "#fff1e8")
                saved.contains(z.id) -> Pair("* " + g.L(z.n[0], z.n[1]), "#7f89b3")
                else -> Pair("- ???", "#5c6690")
            }
            txt(s.first, 18f, y, s.second, SM); y += 9
        }
        return y
    }

    fun draw(g: Game) {
        px("#e00f1631", 0, 0, W, H)
        val L = g::L
        when (g.state) {
            "title" -> {
                txt("BOTIR", W / 2f, 12f, "#f4c430", 16f, 1)
                txt(L("Приключения в Ташкенте", "Toshkent sarguzashti"), W / 2f, 34f, "#fff1e8", SM, 1)
                val y = para(L("Жадина-ака украл казан дедушки! Верни его до тоя.", "Ochko'z aka bobomning qozonini o'g'irladi! To'ygacha qaytar."), 70f)
                val tot = g.eggTotals.sum()
                g.star(58f, y + 6); txt(L("Пасхалки", "Sirlar") + " ${g.allEggs()}/$tot", W / 2f + 4, y + 5, "#f4c430", SM, 1)
            }
            "intro" -> {
                val l = g.levels[g.introI]
                txt(L("Уровень", "Bosqich") + " ${g.introI + 1}", W / 2f, 22f, "#f4c430", SM, 1)
                txt(L(l.n[0], l.n[1]), W / 2f, 34f, "#f4c430", BIG, 1)
                var y = para(L(l.intro[0], l.intro[1]), 58f)
                y = para(L(l.mech[0], l.mech[1]), y + 8, "#86c5ef")
                txt("* ${g.savedEggs(g.introI).size}/${g.eggTotals[g.introI]}", W / 2f, y + 8, "#f4c430", SM, 1)
            }
            "pause" -> {
                txt(L("Пауза", "Pauza"), W / 2f, 14f, "#f4c430", BIG, 1)
                txt(L("Пасхалки уровня", "Bosqich sirlari"), W / 2f, 30f, "#a8b3d9", SM, 1)
                eggList(g, 44f)
            }
            "over" -> {
                txt(L("Ой!", "Voy!"), W / 2f, 14f, "#f4c430", 16f, 1)
                txt(L("Ноны", "Nonlar") + ": ${g.coins}/${g.total}  * ${g.found.size}/${g.zones.size}", W / 2f, 36f, "#fff1e8", SM, 1)
                eggList(g, 52f)
            }
            "win" -> {
                txt(L("Уровень пройден!", "Bosqich tugadi!"), W / 2f, 14f, "#f4c430", BIG, 1)
                txt(L("Ноны", "Nonlar") + ": ${g.coins}/${g.total}  * ${g.found.size}/${g.zones.size}", W / 2f, 30f, "#fff1e8", SM, 1)
                eggList(g, 46f)
            }
            "final" -> {
                txt(L("КАЗАН", "QOZON"), W / 2f, 14f, "#f4c430", 16f, 1)
                txt(L("ВЕРНУЛСЯ!", "QAYTDI!"), W / 2f, 34f, "#f4c430", BIG, 1)
                val y = para(L("Ботир принёс казан домой. Вечером вся махалля ела плов на тое. Жадина-ака пришёл извиниться, попросил добавки и закрыл свой «Plov-Express».",
                    "Botir qozonni uyga olib keldi. Kechqurun butun mahalla to'yda osh yedi. Ochko'z aka kechirim so'rab keldi, yana osh so'radi va «Plov-Express»ini yopdi."), 56f, "#fff1e8")
                txt(L("Пасхалки", "Sirlar") + " ${g.allEggs()}/${g.eggTotals.sum()}", W / 2f, y + 8, "#f4c430", SM, 1)
                Spr.person(80f, y + 26, "#d8373b", "#f4c430", atlas = true, arm = if (g.frame % 30 < 15) 2 else 6)
                G.spr(Spr.QOZ, 90f, y + 36, Spr.pal('O' to "#f0c27a"))
            }
        }
        for (b in g.btns) {
            val fill = when (b.style) { 0 -> "#2e6fd8"; 2 -> "#3a4060"; 3 -> "#f4c430"; else -> "#29366f" }
            px(OUT, b.x - 1, b.y - 1, b.w + 2, b.h + 4); px(fill, b.x, b.y, b.w, b.h)
            val tc = if (b.style == 3) "#140c08" else if (b.style == 2) "#8a90b0" else "#fff1e8"
            if (b.sub.isEmpty()) txt(b.label, b.x + b.w / 2, b.y + (b.h - SM) / 2, tc, SM, 1, shadow = b.style != 3)
            else { txt(b.label, b.x + 4, b.y + 5, if (b.style == 2) tc else "#f4c430", SM); txt(b.sub, b.x + 4, b.y + 18, tc, SM) }
        }
    }
}
