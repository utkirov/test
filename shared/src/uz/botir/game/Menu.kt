package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.sin

/** All menu screens are drawn in pixel-art on the game bitmap; buttons are tap rectangles in game units. */
object Menu {
    private fun btn(g: Game, x: Number, y: Number, w: Number, h: Number, label: String, sub: String = "", style: Int = 0, tag: Int = -1, act: () -> Unit) =
        g.btns.add(Btn(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), label, sub, style, tag, act))

    private fun say(g: Game, s: String) { g.menuMsg = s; g.menuMsgF = 150 }

    fun layout(g: Game) {
        g.btns.clear()
        val L = g::L
        when (g.state) {
            "title" -> {
                btn(g, 30, 98, 120, 22, L("ИГРАТЬ", "O'YNASH")) { g.show("levels") }
                btn(g, 30, 126, 120, 20, L("БЕСКОНЕЧНЫЙ", "CHEKSIZ"), style = 1) { g.startEndless() }
                btn(g, 30, 152, 120, 20, L("ДОМ ДЕДА: МАГАЗИН", "BOBO UYI: DO'KON"), style = 1) { g.shopTab = 0; g.show("shop") }
                btn(g, 30, 178, 120, 20, L("НАСТРОЙКИ", "SOZLAMALAR"), style = 1) { g.show("settings") }
                btn(g, 50, 206, 38, 16, "UZ", style = if (g.lang == "uz") 3 else 1) { g.changeLang("uz") }
                btn(g, 92, 206, 38, 16, "RU", style = if (g.lang == "ru") 3 else 1) { g.changeLang("ru") }
            }
            "levels" -> {
                g.levels.forEachIndexed { i, l ->
                    val locked = i > g.unlock
                    val col = if (i < 6) i % 3 else (i - 6) % 3; val row = if (i < 6) i / 3 else if (i < 9) 2 else 3
                    val x = 6 + col * 57; val y = when (row) { 0 -> 24; 1 -> 66; 2 -> 120; else -> 176 }
                    btn(g, x, y, 54, 38, if (locked) "?" else L(l.sh[0], l.sh[1]), if (locked) L("закрыто", "yopiq") else "", if (locked) 2 else 4, i) { if (!locked) g.goIntro(i) else say(g, L("Сначала пройди прошлый уровень", "Avval oldingi bosqichni o'ting")) }
                }
                btn(g, 40, 238, 100, 18, L("НАЗАД", "ORQAGA"), style = 1) { g.show("title") }
            }
            "intro" -> {
                btn(g, 40, 206, 100, 20, L("ИГРАТЬ", "O'YNASH")) { g.play(g.introI) }
                if (g.levels[g.introI].cutIn != null) btn(g, 40, 230, 100, 16, L("ИСТОРИЯ", "HIKOYA"), style = 1) { g.replayCut(g.introI) }
                btn(g, 40, 250, 100, 16, L("НАЗАД", "ORQAGA"), style = 1) { g.show("levels") }
            }
            "cut" -> {
                btn(g, 118, 6, 56, 14, L("ПРОПУСК", "O'TKAZISH"), style = 1) { g.endCut() }
            }
            "pause" -> {
                btn(g, 40, 186, 100, 18, L("ПРОДОЛЖИТЬ", "DAVOM")) { g.state = "play"; g.btns.clear(); Music.play(if (g.boss != null || g.mini != null) Music.BOSS else g.LV.song) }
                btn(g, 40, 210, 100, 18, L("ЗАНОВО", "QAYTA"), style = 1) { if (g.endless) g.startEndless() else g.play(g.LVI) }
                btn(g, 40, 234, 100, 18, L("МЕНЮ", "MENYU"), style = 1) { g.goTitle() }
            }
            "over" -> {
                btn(g, 40, 210, 100, 18, L("ЕЩЁ РАЗ", "YANA")) { g.play(g.LVI) }
                btn(g, 40, 234, 100, 18, L("МЕНЮ", "MENYU"), style = 1) { g.goTitle() }
            }
            "win" -> {
                val last = g.LVI == g.levels.size - 1
                when {
                    last -> btn(g, 20, 196, 140, 18, L("ФИНАЛ", "YAKUN")) { g.show("credits") }
                    g.LVI + 1 < g.levels.size -> btn(g, 20, 196, 140, 18, L("ДАЛЬШЕ", "KEYINGI") + ": " + L(g.levels[g.LVI + 1].sh[0], g.levels[g.LVI + 1].sh[1])) { g.goIntro(g.LVI + 1) }
                }
                btn(g, 40, 220, 100, 16, L("ЕЩЁ РАЗ", "YANA"), style = 1) { g.play(g.LVI) }
                btn(g, 40, 242, 100, 16, L("МЕНЮ", "MENYU"), style = 1) { g.goTitle() }
            }
            "credits" -> {
                Plat.share?.let { sh -> btn(g, 30, 216, 120, 18, L("ПОДЕЛИТЬСЯ", "ULASHISH"), style = 1) {
                    sh(L("Я прошёл игру Botir и собрал ${g.allStars()} звёзд! Сможешь лучше?", "Men Botir o'yinini tugatdim va ${g.allStars()} ta yulduz yig'dim! Sen yaxshiroq qila olasanmi?")) } }
                btn(g, 40, 240, 100, 18, L("МЕНЮ", "MENYU")) { g.goTitle() }
            }
            "endover" -> {
                btn(g, 40, 200, 100, 20, L("ЕЩЁ ЗАБЕГ", "YANA YUGUR")) { g.startEndless() }
                btn(g, 40, 226, 100, 18, L("МЕНЮ", "MENYU"), style = 1) { g.goTitle() }
                Plat.share?.let { sh -> btn(g, 30, 250, 120, 16, L("ПОДЕЛИТЬСЯ", "ULASHISH"), style = 1) {
                    val m = ((g.pl.x - g.runStart) / T).toInt().coerceAtLeast(0)
                    sh(L("Я пробежал $m м в игре Botir! Мой рекорд — ${g.best} м. Сможешь больше?", "Men Botir o'yinida $m m yugurdim! Rekordim — ${g.best} m. Sen ko'proq yugura olasanmi?")) } }
            }
            "shop" -> {
                btn(g, 10, 30, 78, 16, L("СКИНЫ", "LIBOSLAR"), style = if (g.shopTab == 0) 3 else 1) { g.shopTab = 0; layout(g) }
                btn(g, 92, 30, 78, 16, L("УЛУЧШЕНИЯ", "KUCHAYTIRISH"), style = if (g.shopTab == 1) 3 else 1) { g.shopTab = 1; layout(g) }
                if (g.shopTab == 0) Skins.ALL.forEachIndexed { i, s ->
                    val x = 8 + (i % 2) * 84; val y = 54 + (i / 2) * 58
                    btn(g, x, y, 80, 54, "", "", 5, i) {
                        when {
                            g.skinId == s.id -> say(g, L("Уже надето", "Allaqachon kiyilgan"))
                            g.skinAvailable(s) -> { g.buy(s); say(g, L("Надето!", "Kiyildi!")) }
                            s.season != 0 -> say(g, L("Доступно в сезон: ", "Mavsumda ochiladi: ") + Season.name(s.season, g))
                            g.wallet < s.price -> say(g, L("Не хватает нонов", "Nonlar yetarli emas"))
                            else -> { g.buy(s); say(g, L("Куплено!", "Sotib olindi!")) }
                        }
                        layout(g)
                    }
                } else Upgrades.ALL.forEachIndexed { i, u ->
                    btn(g, 10, 56 + i * 46, 160, 40, "", "", 6, i) {
                        when {
                            g.hasUp(u.id) -> say(g, L("Уже есть", "Allaqachon bor"))
                            g.wallet < u.price -> say(g, L("Не хватает нонов", "Nonlar yetarli emas"))
                            else -> { g.buyUp(u); say(g, L("Куплено!", "Sotib olindi!")) }
                        }
                        layout(g)
                    }
                }
                btn(g, 40, 236, 100, 18, L("НАЗАД", "ORQAGA"), style = 1) { g.show("title") }
            }
            "settings" -> {
                val on = L("вкл", "yoq"); val off = L("выкл", "o'ch")
                val rows = listOf(
                    Triple("music", L("Музыка", "Musiqa"), if (g.musicOn) on else off),
                    Triple("sfx", L("Звуки", "Ovozlar"), if (g.sfxOn) on else off),
                    Triple("vib", L("Вибрация", "Tebranish"), if (g.vibOn) on else off),
                    Triple("size", L("Кнопки", "Tugmalar"), arrayOf("S", "M", "L")[g.btnSize]),
                    Triple("hand", L("Рука", "Qo'l"), if (g.lefty) L("левая", "chap") else L("правая", "o'ng")),
                    Triple("season", L("Сезон", "Mavsum"), when (g.seasonSet) { 0 -> L("авто", "avto"); 1 -> off; else -> Season.name(g.seasonSet - 1, g) }))
                rows.forEachIndexed { i, r -> btn(g, 14, 34 + i * 28, 152, 22, r.second, r.third, 7) { g.setOpt(r.first) } }
                btn(g, 40, 236, 100, 18, L("НАЗАД", "ORQAGA"), style = 1) { g.show("title") }
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

    private fun stars3(g: Game, bits: Int, cx: Float, y: Float, big: Boolean) {
        val sc = if (big) 3f else 1.4f
        for (i in 0 until 3) {
            val on = bits and (1 shl i) != 0
            G.c.save(); G.c.translate(cx + (i - 1) * (if (big) 22 else 9) - 2.5f * sc, y); G.c.scale(sc, sc)
            g.star(0f, 0f, if (on) "#f4c430" else "#4a4f70"); G.c.restore()
        }
    }

    private fun heroAt(g: Game, sk: Skin, x: Float, y: Float, scale: Float, lk: String = "idle") {
        G.c.save(); G.c.translate(x, y); G.c.scale(scale, scale)
        val anim = when (lk) { "run0", "run1" -> "run"; else -> lk }
        Hero.draw(anim, g.frame, sk, false, (g.frame % 48) / 48f); G.c.restore()
    }

    private fun speaker(g: Game, who: Char, x: Float, y: Float, active: Boolean) {
        val bob = if (active && g.frame % 20 < 10) -1f else 0f
        G.c.save(); G.c.translate(x, y + bob); G.c.scale(2f, 2f)
        when (who) {
            'D' -> Spr.person(0f, 0f, "#2f7d4a", "#f2f2f2", scarf = true, beard = true, arm = if (active) 6 else 0)
            'N' -> Spr.person(0f, 0f, "#7b3fa0", "#f2f2f2", scarf = true, beard = true, arm = if (active) 6 else 0)
            'O' -> Spr.person(0f, 0f, "#5b3a8a", "#1c1410", glasses = true, arm = if (active) 6 else 0)
            'Z' -> { Spr.person(0f, 0f, "#2e7d4a", "#1c1410", beard = true, arm = if (active) 6 else 0) }
            'X' -> Spr.person(0f, 0f, "#1f5aa8", "#f5f5f5", arm = if (active) 6 else 0)
        }
        G.c.restore()
    }

    private fun cutscene(g: Game) {
        val cut = Cuts.ALL[g.cutId] ?: return
        val line = cut.lines[g.cutLine.coerceIn(0, cut.lines.size - 1)]
        px("#7a5a3a", 0, 168, W, 40); px("#4f9a3a", 0, 168, W, 4)
        val other = cut.lines.firstOrNull { it.who != 'B' }?.who ?: 'D'
        heroAt(g, g.skin(), 34f, 137f + (if (line.who == 'B' && g.frame % 20 < 10) -1 else 0), 1f, if (line.who == 'B') "jump" else "idle")
        speaker(g, other, 118f, 132f, line.who != 'B')
        val name = when (line.who) { 'B' -> g.L("Ботир", "Botir"); 'D' -> g.L("Бобо", "Bobo"); 'N' -> g.L("Насреддин", "Nasriddin"); 'O' -> g.L("Жадина-ака", "Ochko'z aka"); 'X' -> g.L("Хасис-ака", "Xasis aka"); else -> g.L("Зикна-ака", "Ziqna aka") }
        px(OUT, 5, 205, 170, 60); px("#1d2b53", 6, 206, 168, 58); px("#f4c430", 6, 206, 168, 2)
        txt(name, 12f, 211f, "#f4c430", SM)
        var y = 223f
        for (l in G.wrap(g.L(line.ru, line.uz), 156f, SM)) { txt(l, 12f, y, "#fff1e8", SM); y += 9 }
        if (g.frame % 40 < 25) txt(">", 164f, 254f, "#f4c430", SM)
        val bx = if (line.who == 'B') 46f else 128f
        G.tri("#1d2b53", floatArrayOf(bx - 4, bx + 4, bx), floatArrayOf(206f, 206f, 198f))
    }

    fun draw(g: Game) {
        val L = g::L
        if (g.state == "cut") { cutscene(g); drawButtons(g); return }
        px("#d80f1631", 0, 0, W, H)
        when (g.state) {
            "title" -> {
                Season.titleDecor(g.season, g.frame)
                txt("BOTIR", W / 2f, 18f, "#f4c430", 16f, 1)
                txt(L("Приключения в Узбекистане", "O'zbekiston sarguzashtlari"), W / 2f, 40f, "#fff1e8", SM, 1)
                Spr.item("non", 39f, 55f); txt("${g.wallet}", 54f, 58f, "#fff1e8", SM)
                g.star(98f, 58f); txt("${g.allStars()}/${g.levels.size * 3}", 106f, 58f, "#f4c430", SM)
                txt(L("Пасхалки", "Sirlar") + " ${g.allEggs()}/${g.eggTotals.sum()}", W / 2f, 72f, "#a8b3d9", SM, 1)
                heroAt(g, g.skin(), 140f, 228f, 1f, if ((g.frame / 8) % 2 == 0) "run0" else "run1")
                Season.banner(g.season, g, 84f)
            }
            "levels" -> {
                txt(L("Глава 1: Ташкент", "1-bob: Toshkent"), W / 2f, 12f, "#f4c430", SM, 1)
                txt(L("Глава 2: Шёлковый путь", "2-bob: Ipak yo'li"), W / 2f, 108f, "#f4c430", SM, 1)
                txt(L("Глава 3: Долина и Арал", "3-bob: Vodiy va Orol"), W / 2f, 164f, "#f4c430", SM, 1)
            }
            "intro" -> {
                val l = g.levels[g.introI]
                txt(L("Уровень", "Bosqich") + " ${g.introI + 1}", W / 2f, 16f, "#f4c430", SM, 1)
                txt(L(l.n[0], l.n[1]), W / 2f, 28f, "#f4c430", BIG, 1)
                stars3(g, g.stars(g.introI), W / 2f, 42f, false)
                var y = para(L(l.intro[0], l.intro[1]), 56f)
                y = para(L(l.mech[0], l.mech[1]), y + 6, "#86c5ef")
                para("* ${g.savedEggs(g.introI).size}/${g.eggTotals[g.introI]}. " + L("Звёзды: пройти, все ноны, все пасхалки", "Yulduzlar: o'tish, barcha non, barcha sirlar"), y + 6)
            }
            "pause" -> {
                txt(L("Пауза", "Pauza"), W / 2f, 14f, "#f4c430", BIG, 1)
                if (g.endless) txt(L("Рекорд", "Rekord") + ": ${g.best}m", W / 2f, 40f, "#fff1e8", SM, 1)
                else { txt(L("Пасхалки уровня", "Bosqich sirlari"), W / 2f, 30f, "#a8b3d9", SM, 1); eggList(g, 44f) }
            }
            "over" -> {
                txt(L("Ой!", "Voy!"), W / 2f, 14f, "#f4c430", 16f, 1)
                txt(L("Ноны", "Nonlar") + ": ${g.coins}/${g.total}  * ${g.found.size}/${g.zones.size}", W / 2f, 36f, "#fff1e8", SM, 1)
                eggList(g, 52f)
            }
            "win" -> {
                txt(L("Уровень пройден!", "Bosqich tugadi!"), W / 2f, 10f, "#f4c430", BIG, 1)
                stars3(g, g.lastStars, W / 2f, 26f, true)
                txt(L("Ноны", "Nonlar") + ": ${g.coins}/${g.total}  +${g.lastEarned}", W / 2f, 48f, "#fff1e8", SM, 1)
                eggList(g, 62f)
            }
            "credits" -> {
                txt(L("ПОБЕДА!", "G'ALABA!"), W / 2f, 16f, "#f4c430", 16f, 1)
                val y = para(L("Казан, золотой кафгир и риштанский ляган вернулись домой. Весь Узбекистан ест плов в честь Ботира!", "Qozon, oltin kapgir va Rishton lagani uyga qaytdi. Butun O'zbekiston Botir sharafiga osh yemoqda!"), 44f, "#fff1e8")
                txt(L("Звёзды", "Yulduzlar") + " ${g.allStars()}/${g.levels.size * 3}", W / 2f, y + 8, "#f4c430", SM, 1)
                txt(L("Пасхалки", "Sirlar") + " ${g.allEggs()}/${g.eggTotals.sum()}", W / 2f, y + 18, "#f4c430", SM, 1)
                para(L("Спасибо, что играл! Найди все пасхалки и собери все звёзды.", "O'ynaganingiz uchun rahmat! Barcha sirlarni toping va barcha yulduzlarni yig'ing."), y + 34)
                heroAt(g, g.skin(), 60f, 190f, 1f, if (g.frame % 30 < 15) "jump" else "idle")
                Spr.item("qozon", 88f, 204f); Spr.item("kafgir", 110f, 208f); Spr.item("lagan", 132f, 205f)
            }
            "endover" -> {
                val m = ((g.pl.x - g.runStart) / T).toInt().coerceAtLeast(0)
                txt(L("Забег окончен", "Yugurish tugadi"), W / 2f, 30f, "#f4c430", BIG, 1)
                txt("${m}m", W / 2f, 60f, "#fff1e8", 16f, 1)
                if (g.newBest && g.frame % 30 < 20) txt(L("НОВЫЙ РЕКОРД!", "YANGI REKORD!"), W / 2f, 86f, "#f4c430", BIG, 1)
                txt(L("Рекорд", "Rekord") + ": ${g.best}m", W / 2f, 104f, "#a8b3d9", SM, 1)
                Spr.item("non", 69f, 121f); txt("+${g.lastEarned}", 84f, 124f, "#fff1e8", SM)
                txt(L("Всего нонов", "Jami nonlar") + ": ${g.wallet}", W / 2f, 142f, "#a8b3d9", SM, 1)
            }
            "shop" -> {
                txt(L("Дом деда", "Bobo uyi"), W / 2f, 6f, "#f4c430", BIG, 1)
                Spr.item("non", 65f, 17f); txt("${g.wallet}", 80f, 20f, "#fff1e8", SM)
            }
            "settings" -> txt(L("Настройки", "Sozlamalar"), W / 2f, 12f, "#f4c430", BIG, 1)
        }
        drawButtons(g)
        if (g.menuMsgF > 0) {
            g.menuMsgF--
            px(OUT, 9, 214, 162, 16); px("#1d2b53", 10, 215, 160, 14); txt(g.menuMsg, W / 2f, 219f, "#f4c430", SM, 1)
        }
    }

    private fun drawButtons(g: Game) {
        for (b in g.btns) {
            val fill = when (b.style) { 0 -> "#2e6fd8"; 2 -> "#3a4060"; 3 -> "#f4c430"; else -> "#29366f" }
            px(OUT, b.x - 1, b.y - 1, b.w + 2, b.h + 4); px(fill, b.x, b.y, b.w, b.h)
            when (b.style) {
                4 -> { // level card
                    txt(b.label, b.x + b.w / 2, b.y + 5, "#f4c430", SM, 1)
                    stars3(g, g.stars(b.tag), b.x + b.w / 2, b.y + 17f, false)
                    txt("* ${g.savedEggs(b.tag).size}/${g.eggTotals[b.tag]}", b.x + b.w / 2, b.y + 27, "#a8b3d9", SM, 1)
                }
                5 -> { // skin card
                    val s = Skins.ALL[b.tag]; val sel = g.skinId == s.id
                    if (sel) { px("#f4c430", b.x, b.y, b.w, 2); px("#f4c430", b.x, b.y + b.h - 2, b.w, 2) }
                    heroAt(g, s, b.x + 2, b.y + 8, 1f)
                    val nm = G.wrap(g.L(s.n[0], s.n[1]), 50f, SM)
                    nm.take(2).forEachIndexed { i, l -> txt(l, b.x + 28, b.y + 8 + i * 9, "#fff1e8", SM) }
                    val st = when { sel -> g.L("надето", "kiyilgan"); g.skinAvailable(s) -> g.L("надеть", "kiyish"); s.season != 0 -> Season.name(s.season, g); else -> "${s.price}" }
                    if (!g.skinAvailable(s) && s.season == 0) Spr.item("non", b.x + 27, b.y + 37)
                    txt(st, b.x + (if (!g.skinAvailable(s) && s.season == 0) 41 else 28), b.y + 40, if (sel) "#f4c430" else "#a8b3d9", SM)
                }
                6 -> { // upgrade row
                    val u = Upgrades.ALL[b.tag]; val has = g.hasUp(u.id)
                    txt(g.L(u.n[0], u.n[1]), b.x + 6, b.y + 6, "#fff1e8", SM)
                    txt(g.L(u.d[0], u.d[1]), b.x + 6, b.y + 17, "#a8b3d9", SM)
                    if (has) txt(g.L("куплено", "olingan"), b.x + 6, b.y + 28, "#f4c430", SM)
                    else { Spr.item("non", b.x + 5, b.y + 26); txt("${u.price}", b.x + 20, b.y + 29, "#f4c430", SM) }
                }
                7 -> { txt(b.label, b.x + 6, b.y + (b.h - SM) / 2, "#fff1e8", SM); txt(b.sub, b.x + b.w - 6, b.y + (b.h - SM) / 2, "#f4c430", SM, 2) }
                else -> {
                    val tc = if (b.style == 3) "#140c08" else if (b.style == 2) "#8a90b0" else "#fff1e8"
                    if (b.sub.isEmpty()) txt(b.label, b.x + b.w / 2, b.y + (b.h - SM) / 2, tc, SM, 1, shadow = b.style != 3)
                    else { txt(b.label, b.x + b.w / 2, b.y + 8, tc, SM, 1); txt(b.sub, b.x + b.w / 2, b.y + 20, tc, SM, 1) }
                }
            }
        }
    }

    @Suppress("unused") private fun wobble(f: Int) = sin(f / 10f)
}
