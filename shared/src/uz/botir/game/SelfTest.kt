package uz.botir.game

/** A simple bot that plays every level, the endless run and all menus. Used by CI on Android and in the browser. */
object SelfTest {
    fun bot(g: Game, f: Int) {
        val p = g.pl
        p.hp = 5
        if (g.LV.vertical) {
            val fx = ((p.x + p.w / 2) / T).toInt(); val fy = ((p.y + p.h) / T).toInt()
            var target = -1
            loop@ for (dy in 1..4) for (dx in 0..10) for (s in intArrayOf(1, -1)) {
                val tx = fx + dx * s; val ty = fy - dy
                val c = g.tileAt(tx, ty)
                if ((c == '=' || c == '#') && g.tileAt(tx, ty - 1) == '.') { target = tx; break@loop }
            }
            g.kR = target > fx; g.kL = target in 0 until fx
            if (p.on && f % 12 == 0) p.buf = 7
            g.kJ = p.vy < 0
        } else {
            g.kR = true; g.kL = false
            val ahead = p.x + p.w + 8
            val wall = g.solidAt(ahead, p.y + p.h - 4) || g.solidAt(ahead, p.y + 6)
            val gap = !g.solidAt(ahead, p.y + p.h + 4) && g.tAt(ahead, p.y + p.h + 4) != '=' && !g.solidAt(ahead, p.y + p.h + 20)
            if (p.on && (wall || gap || (g.mini != null && f % 30 == 0) || f % 90 == 0)) p.buf = 7
            g.kJ = p.vy < 0 || p.buf > 0
        }
    }

    private fun loop(g: Game, max: Int, renderEvery: Int, until: () -> Boolean = { false }): Int {
        var k = 0
        while (k < max && g.state == "play" && !until()) { bot(g, k); g.update(); if (k % renderEvery == 0) g.render(); k++ }
        return k
    }

    fun run(g: Game, renderEvery: Int = 5): String {
        val out = StringBuilder(); var ok = true
        for (i in g.levels.indices) {
            try {
                g.play(i)
                var maxX = 0f; var frames = 0
                while (frames < 7000 && g.state == "play") { bot(g, frames); g.update(); if (frames % renderEvery == 0) g.render(); if (g.pl.x > maxX) maxX = g.pl.x; frames++ }
                val md = g.miniDef
                if (md != null && !g.miniDone && g.state == "play") {
                    g.pl.x = md.trigX + T; g.pl.y = if (md.trigY >= 0) md.trigY - 40 else md.gy - 26; g.pl.vy = 0f
                    loop(g, 2500, renderEvery) { g.miniDone }
                }
                if (g.arenaX0 > 0 && g.state == "play") { g.pl.x = g.arenaX0 + 2 * T; g.pl.y = g.bossGround - 30; loop(g, 3000, renderEvery) }
                out.append("L${i + 1} ${g.LV.sh[0]}: frames=$frames maxTile=${(maxX / T).toInt()} state=${g.state} mini=${g.miniDef?.k ?: '-'}:${g.miniDone} eggs=${g.found.size}/${g.zones.size}\n")
            } catch (t: Throwable) { ok = false; out.append("L${i + 1} FAIL $t\n${t.stackTraceToString()}\n") }
        }
        try {
            g.startEndless(); val f = loop(g, 6000, renderEvery)
            out.append("ENDLESS: frames=$f dist=${((g.pl.x - g.runStart) / T).toInt()}m cols=${g.cols}\n")
        } catch (t: Throwable) { ok = false; out.append("ENDLESS FAIL $t\n${t.stackTraceToString()}\n") }
        try {
            for (s in listOf("title", "levels", "shop", "settings", "credits")) { g.goTitle(); g.show(s); repeat(30) { g.update() }; g.render() }
            g.shopTab = 1; g.show("shop"); g.render()
            for (i in g.levels.indices) { g.introI = i; g.show("intro"); g.render() }
            for (id in Cuts.ALL.keys) { g.startCut(id, "credits"); repeat(10) { g.update(); g.render(); g.nextLine() } }
            for (opt in listOf("music", "sfx", "vib", "size", "hand", "season", "season", "season", "season", "season", "size", "size", "hand", "music", "sfx", "vib")) { g.setOpt(opt); g.render() }
            for (ss in 0..4) { g.seasonSet = ss; g.loadLevel(0); g.state = "play"; repeat(20) { g.update() }; g.render() }
            g.seasonSet = 0; g.goTitle()
            out.append("MENUS ok\n")
        } catch (t: Throwable) { ok = false; out.append("MENUS FAIL $t\n${t.stackTraceToString()}\n") }
        out.append(if (ok) "RESULT OK\n" else "RESULT FAIL\n")
        return out.toString()
    }
}
