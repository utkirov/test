package uz.botir.game

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Developer tools, only reachable with an intent extra (used by the CI emulator job):
 *  - selfTest: a simple bot plays every level, the endless mode and every menu, and reports crashes.
 *  - promo: renders Google Play screenshots, the 512px icon and the 1024x500 feature graphic.
 */
object Tools {
    private const val TAG = "BOTIR_TOOLS"

    private fun bot(g: Game, f: Int) {
        val p = g.pl
        p.hp = 5
        if (g.LV.vertical) {
            // climb: aim for the closest platform above
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
            val mini = g.mini
            if (p.on && (wall || gap || (mini != null && f % 30 == 0) || f % 90 == 0)) p.buf = 7
            g.kJ = p.vy < 0 || p.buf > 0
        }
    }

    fun selfTest(a: Activity, v: GameView) {
        Thread {
            val out = StringBuilder(); var ok = true
            val g = v.game
            val c = Canvas(Bitmap.createBitmap(W * 3, H * 3, Bitmap.Config.ARGB_8888))
            try { Thread.sleep(1500) } catch (_: Throwable) { }
            synchronized(g) {
                for (i in g.levels.indices) {
                    try {
                        g.play(i)
                        var maxX = 0f; var minY = 1e9f; var frames = 0
                        while (frames < 7000 && g.state == "play") { bot(g, frames); g.update(); if (frames % 5 == 0) g.render(c); maxX = maxOf(maxX, g.pl.x); minY = minOf(minY, g.pl.y); frames++ }
                        // force the arena fight if the bot never got there
                        val md = g.miniDef
                        if (md != null && !g.miniDone && g.state == "play") {
                            g.pl.x = md.trigX + T; g.pl.y = if (md.trigY >= 0) md.trigY - 40 else md.gy - 26; g.pl.vy = 0f
                            var k = 0; while (k < 2500 && g.state == "play" && !g.miniDone) { bot(g, k); g.update(); if (k % 5 == 0) g.render(c); k++ }
                        }
                        if (g.arenaX0 > 0 && g.state == "play") {
                            g.pl.x = g.arenaX0 + 2 * T; g.pl.y = g.bossGround - 30
                            var k = 0; while (k < 3000 && g.state == "play") { bot(g, k); g.update(); if (k % 5 == 0) g.render(c); k++ }
                        }
                        out.append("L${i + 1} ${g.LV.sh[0]}: frames=$frames maxTile=${(maxX / T).toInt()} minRow=${(minY / T).toInt()} state=${g.state} mini=${g.miniDef?.k ?: '-'}:${g.miniDone} eggs=${g.found.size}/${g.zones.size}\n")
                    } catch (t: Throwable) { ok = false; out.append("L${i + 1} FAIL ${Log.getStackTraceString(t)}\n") }
                }
                try {
                    g.startEndless(); var f = 0
                    while (f < 6000 && g.state == "play") { bot(g, f); g.update(); if (f % 5 == 0) g.render(c); f++ }
                    out.append("ENDLESS: frames=$f dist=${((g.pl.x - g.runStart) / T).toInt()}m cols=${g.cols}\n")
                } catch (t: Throwable) { ok = false; out.append("ENDLESS FAIL ${Log.getStackTraceString(t)}\n") }
                try {
                    for (s in listOf("title", "levels", "shop", "settings", "credits")) { g.goTitle(); g.show(s); repeat(30) { g.update() }; g.render(c) }
                    g.shopTab = 1; g.show("shop"); g.render(c)
                    for (i in g.levels.indices) { g.introI = i; g.show("intro"); g.render(c) }
                    for (id in Cuts.ALL.keys) { g.startCut(id, "credits"); repeat(10) { g.update(); g.render(c); g.nextLine() } }
                    for (opt in listOf("music", "sfx", "vib", "size", "hand", "season", "season", "season", "season", "season", "size", "size", "hand", "music", "sfx", "vib")) { g.setOpt(opt); g.render(c) }
                    for (ss in 0..4) { g.seasonSet = ss; g.loadLevel(0); g.state = "play"; repeat(20) { g.update() }; g.render(c) }
                    g.seasonSet = 0
                    out.append("MENUS ok\n")
                } catch (t: Throwable) { ok = false; out.append("MENUS FAIL ${Log.getStackTraceString(t)}\n") }
            }
            out.append(if (ok) "RESULT OK\n" else "RESULT FAIL\n")
            try { File(a.filesDir, "selftest.txt").writeText(out.toString()) } catch (_: Throwable) { }
            out.lines().forEach { Log.i(TAG, it) }
            a.runOnUiThread { a.finish() }
        }.start()
    }

    private fun save(b: Bitmap, f: File) { FileOutputStream(f).use { b.compress(Bitmap.CompressFormat.PNG, 100, it) } }

    fun promo(a: Activity, v: GameView) {
        Thread {
            try { Thread.sleep(1500) } catch (_: Throwable) { }
            val dir = File(a.filesDir, "promo").apply { mkdirs() }
            val g = v.game
            val shot = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            fun scene(name: String, setup: () -> Unit, frames: Int = 40) {
                try {
                    synchronized(g) { setup(); repeat(frames) { g.update() }; g.kL = false; g.kR = false; g.kJ = false }
                    a.runOnUiThread { try { synchronized(g) { v.renderTo(shot) }; save(shot, File(dir, "$name.png")) } catch (t: Throwable) { Log.e(TAG, "render $name", t) } }
                    Thread.sleep(900)
                } catch (t: Throwable) { Log.e(TAG, "scene $name", t) }
            }
            fun place(i: Int, tx: Float, ty: Float) { g.play(i); g.toasts.clear(); g.pl.x = tx * T; g.pl.y = ty * T - 26; g.pl.safeX = g.pl.x; g.pl.safeY = g.pl.y; g.camX = g.pl.x - 80; g.camY = g.pl.y - 150 }
            scene("01_title", { g.goTitle() }, 90)
            scene("02_mahalla", { place(0, 27f, 11f); g.tip(g.L("Курпача-батут! Подбрасывает высоко.", "Ko'rpacha-batut! Baland otadi.")); g.kR = true }, 30)
            scene("03_chorsu", { place(1, 41f, 10f); g.kR = true }, 30)
            scene("04_anhor", { place(3, 22f, 15f); g.kR = true }, 50)
            scene("05_boss", { place(5, 99f, 14f); g.pl.inv = 9999 }, 160)
            scene("06_samarkand", { place(6, 14f, 14f); g.kR = true }, 30)
            scene("07_bukhara", { place(7, 13f, 14f); g.kR = true }, 30)
            scene("08_levels", { g.goTitle(); g.show("levels") }, 30)
            scene("09_shop", { g.goTitle(); g.shopTab = 0; g.show("shop") }, 30)
            // 512x512 icon
            try {
                val icon = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888); val c = Canvas(icon)
                synchronized(g) {
                    G.c = c; c.scale(16f, 16f)
                    G.px("#2e6fd8", 0, 0, 32, 32); G.circle("#f08a24", 16, 17, 14)
                    c.save(); c.translate(6f, 4f); c.scale(0.84f, 0.84f); G.drawParts(Spr.hero("jump", false, Skins.ALL[0]), 0f); c.restore()
                }
                save(icon, File(dir, "icon_512.png"))
            } catch (t: Throwable) { Log.e(TAG, "icon", t) }
            // 1024x500 feature graphic
            try {
                val fg = Bitmap.createBitmap(1024, 500, Bitmap.Config.ARGB_8888); val c = Canvas(fg)
                synchronized(g) {
                    g.goTitle(); G.c = c; c.scale(500f / 270f, 500f / 270f)
                    for (k in 0 until 4) { c.save(); c.translate(k * 180f, 0f); g.camX = k * 180f; Bg.city(g); c.restore() }
                    G.px("#4f9a3a", 0, 214, 560, 6); G.px("#7cc44a", 0, 214, 560, 2); G.px("#b9773d", 0, 220, 560, 50)
                    for (k in 0 until 6) G.spr(Spr.NON, 40 + k * 16, 150 - (k % 2) * 8, Spr.PAL)
                    c.save(); c.translate(60f, 136f); c.scale(2.5f, 2.5f); G.drawParts(Spr.hero("jump", false, Skins.ALL[0]), 0f); c.restore()
                    G.px("#140c08", 248, 38, 294, 124); G.px("#1d2b53", 250, 40, 290, 120); G.px("#f4c430", 250, 40, 290, 3)
                    G.txt("BOTIR", 395f, 56f, "#f4c430", 32f, 1)
                    G.txt("O'zbekiston sarguzashtlari", 395f, 102f, "#fff1e8", 8f, 1)
                    G.txt("Приключения в Узбекистане", 395f, 120f, "#a8b3d9", 8f, 1)
                    c.save(); c.translate(360f, 132f); c.scale(2f, 2f); G.spr(Spr.QOZ, 0f, 0f, Spr.pal('O' to "#f0c27a")); G.spr(Spr.KAF, 18f, 2f, Spr.PAL); c.restore()
                }
                save(fg, File(dir, "feature_1024x500.png"))
            } catch (t: Throwable) { Log.e(TAG, "feature", t) }
            File(dir, "done.txt").writeText("ok")
            Log.i(TAG, "PROMO DONE")
            a.runOnUiThread { a.finish() }
        }.start()
    }
}
