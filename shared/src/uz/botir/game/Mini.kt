package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Mini-bosses guarding the exit of each level. All take 3 stomps.
 * G goose king (charges), A porter with cart (charges + rolls melons), K metro controller (jumps + throws tokens),
 * V bike gang leader (fast laps, pauses at walls), P pigeon king (flies and dives), S Sher-Dor tiger (pounces),
 * T angry camel (spits).
 */
class Mini(val k: Char, var x: Float, var y: Float, val w: Float, val h: Float, val x0: Float, val x1: Float, val gy: Float) {
    var vx = 0f; var vy = 0f; var hp = 3; var inv = 0; var t = 0; var dead = 0
    var st = 0; var stT = 0; var dir = -1; var tx = 0f; var ty = 0f
}

private val MINI_NAMES = mapOf(
    'G' to arrayOf("ГУСЬ-КОРОЛЬ", "G'OZ-QIROL"), 'A' to arrayOf("КОРОЛЬ НОСИЛЬЩИКОВ", "ARAVAKASH-QIROL"),
    'K' to arrayOf("КОНТРОЛЁР", "NAZORATCHI"), 'V' to arrayOf("ВЕЛО-ГЛАВАРЬ", "VELO-BOSHLIQ"),
    'P' to arrayOf("КОРОЛЬ ГОЛУБЕЙ", "KAPTAR-QIROL"), 'S' to arrayOf("ШЕР С МЕДРЕСЕ", "SHERDOR SHERI"), 'T' to arrayOf("ЗЛОЙ ВЕРБЛЮД", "JAHLDOR TUYA"))

private val MINI_SIZE = mapOf('G' to (26f to 20f), 'A' to (34f to 22f), 'K' to (14f to 24f), 'V' to (26f to 20f), 'P' to (26f to 14f), 'S' to (30f to 18f), 'T' to (30f to 28f))

fun Game.spawnMini(d: MiniDef) {
    val (w, h) = MINI_SIZE[d.k] ?: (24f to 20f)
    val m = Mini(d.k, d.x1 - w - 3 * T, d.gy - h, w, h, d.x0, d.x1, d.gy)
    if (d.k == 'P') { m.x = (d.x0 + d.x1) / 2 - w / 2; m.y = d.gy - 62 }
    mini = m; miniHits = 0
    Music.play(Music.BOSS); Sfx.fx("roar"); vib(40)
    val line = when (d.k) {
        'G' -> L("Гусь-король: «Га-га-га! Это мой двор!»", "G'oz-qirol: «Ga-ga-ga! Bu mening hovlim!»")
        'A' -> L("Король носильщиков: «Пошт-пошт! С дороги!»", "Aravakash-qirol: «Posht-posht! Yo'ldan qoch!»")
        'K' -> L("Контролёр: «Ваш жетон, молодой человек!»", "Nazoratchi: «Jetoningiz qani, yigitcha!»")
        'V' -> L("Вело-главарь: «Кто быстрее — тот и прав!»", "Velo-boshliq: «Kim tez bo'lsa — o'sha haq!»")
        'P' -> L("Король голубей: «Курлык! Это моя башня!»", "Kaptar-qirol: «Ku-ku! Bu mening minoram!»")
        'S' -> L("Шер сошёл с медресе Шердор! Р-р-р!", "Sher Sherdor madrasasidan tushdi! R-r-r!")
        else -> L("Верблюд: «Пфф! Сейчас как плюну!»", "Tuya: «Pff! Hozir tupuraman!»")
    }
    toast(line)
}

fun Game.updMini() {
    val m = mini ?: return; val p = pl
    if (m.dead > 0) {
        m.dead++
        if (m.dead == 30) {
            miniDone = true; Music.play(LV.song)
            for (i in 0 until 6) { val it = Item('n', m.x + m.w / 2 - 5, m.y); it.fall = true; it.vy = -3f - i * 0.4f; it.drift = (i - 2.5f) * 0.5f; items.add(it); total++ }
            if (miniHits == 0) toast(L("Без единой царапины! Ноны в награду.", "Bir marta ham tegmasdan! Mukofotga nonlar."))
            else toast(L("Путь свободен! Ворота открыты.", "Yo'l ochiq! Darvoza ochildi."))
        }
        if (m.dead > 60) mini = null
        return
    }
    m.t++; if (m.inv > 0) m.inv--
    val pcx = p.x + p.w / 2; val mcx = m.x + m.w / 2
    val ground = m.k != 'P'
    when (m.k) {
        'G', 'A', 'V' -> {
            val walk = if (m.k == 'V') 0f else 0.6f
            when (m.st) {
                0 -> { // patrol
                    val sp = if (m.k == 'V') 2.2f + (3 - m.hp) * 0.4f else walk
                    m.x += m.dir * sp
                    if (m.x < m.x0 + 4) { m.x = m.x0 + 4; if (m.k == 'V') { m.st = 3; m.stT = 40; Sfx.fx("bell") }; m.dir = 1 }
                    if (m.x + m.w > m.x1 - 4) { m.x = m.x1 - 4 - m.w; if (m.k == 'V') { m.st = 3; m.stT = 40; Sfx.fx("bell") }; m.dir = -1 }
                    if (m.k == 'V' && m.t % 170 == 0 && m.vy == 0f) m.vy = -5.5f
                    if (m.k != 'V' && m.t % (120 - (3 - m.hp) * 20) == 0) { m.st = 1; m.stT = 30; m.dir = if (pcx > mcx) 1 else -1; Sfx.fx(if (m.k == 'G') "honk" else "roar") }
                    if (m.k == 'A' && m.t % 150 == 75) shots.add(Shot(mcx, m.gy - 6, (if (pcx > mcx) 1 else -1) * (1.6f + (3 - m.hp) * 0.3f), 0f, 'm'))
                }
                1 -> { m.stT--; if (m.stT <= 0) { m.st = 2 } }       // telegraph (drawn shaking)
                2 -> { // charge
                    m.x += m.dir * (2.8f + (3 - m.hp) * 0.4f)
                    if (m.t % 4 == 0) burst(if (m.dir > 0) m.x else m.x + m.w, m.gy - 2, "#d9c09a", 1)
                    if (m.x < m.x0 + 4 || m.x + m.w > m.x1 - 4) { m.x = m.x.coerceIn(m.x0 + 4, m.x1 - 4 - m.w); m.st = 3; m.stT = 55; vib(30); burst(mcx, m.y, "#ffffff", 8); Sfx.fx("land") }
                }
                3 -> { m.stT--; if (m.stT <= 0) { m.st = 0; m.dir = if (pcx > mcx) 1 else -1 } }   // stunned / resting
            }
        }
        'K' -> {
            m.x += m.dir * 0.8f
            if (m.x < m.x0 + 4) { m.x = m.x0 + 4; m.dir = 1 }; if (m.x + m.w > m.x1 - 4) { m.x = m.x1 - 4 - m.w; m.dir = -1 }
            if (m.t % 140 == 0 && m.vy == 0f) m.vy = -6f
            val per = 90 - (3 - m.hp) * 18
            if (m.t % per == 0) { val dx = pcx - mcx; shots.add(Shot(mcx, m.y + 4, (dx / 45f).coerceIn(-3f, 3f), -4.2f, 'j')); Sfx.fx("throw") }
        }
        'S' -> when (m.st) {
            0 -> { m.x += m.dir * 0.9f; if (m.x < m.x0 + 4) { m.x = m.x0 + 4; m.dir = 1 }; if (m.x + m.w > m.x1 - 4) { m.x = m.x1 - 4 - m.w; m.dir = -1 }
                if (m.t % (100 - (3 - m.hp) * 15) == 0) { m.st = 1; m.stT = 25; m.dir = if (pcx > mcx) 1 else -1; Sfx.fx("roar") } }
            1 -> { m.stT--; if (m.stT <= 0) { m.st = 2; m.vy = -5.8f; m.vx = m.dir * (2.2f + (3 - m.hp) * 0.3f) } }
            2 -> { m.x += m.vx; m.x = m.x.coerceIn(m.x0 + 4, m.x1 - 4 - m.w); if (m.vy == 0f && m.y >= m.gy - m.h - 0.5f) { m.st = 3; m.stT = 35; burst(mcx, m.gy, "#d9c09a", 8); vib(20) } }
            3 -> { m.stT--; if (m.stT <= 0) m.st = 0 }
        }
        'T' -> {
            m.x += m.dir * 0.5f
            if (m.x < m.x0 + 4) { m.x = m.x0 + 4; m.dir = 1 }; if (m.x + m.w > m.x1 - 4) { m.x = m.x1 - 4 - m.w; m.dir = -1 }
            val per = 80 - (3 - m.hp) * 15
            if (m.t % per == 0) { m.dir = if (pcx > mcx) 1 else -1; val dx = pcx - mcx
                shots.add(Shot(if (m.dir > 0) m.x + m.w else m.x, m.y + 2, (dx / 40f).coerceIn(-3.5f, 3.5f), -3.6f, 's')); Sfx.fx("throw") }
            if (m.hp == 1 && m.t % 60 == 30) shots.add(Shot(mcx, m.y + 2, -m.dir * 1.5f, -5f, 's'))
        }
        'P' -> when (m.st) {
            0 -> { m.tx = (m.x0 + m.x1) / 2 + sin(m.t / 50f) * ((m.x1 - m.x0) / 2 - 30); m.x += (m.tx - m.w / 2 - m.x) * 0.05f
                m.y += (m.gy - 66 + sin(m.t / 20f) * 6 - m.y) * 0.08f
                if (m.t % (150 - (3 - m.hp) * 25) == 0) { m.st = 1; m.stT = 24; Sfx.fx("honk") } }
            1 -> { m.stT--; if (m.stT <= 0) { m.st = 2; m.tx = pcx; m.ty = p.y + p.h } }
            2 -> { val dx = m.tx - mcx; val dy = m.ty - (m.y + m.h); val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f); val sp = 3.2f + (3 - m.hp) * 0.4f
                m.x += dx / d * sp; m.y += dy / d * sp; if (d < 6 || m.y + m.h >= m.gy - 2) { m.st = 3; m.stT = 40 } }
            3 -> { m.stT--; m.y -= 1.6f; if (m.stT <= 0) m.st = 0 }
        }
    }
    if (ground) {
        m.vy += 0.32f; m.y += m.vy
        if (m.y > m.gy - m.h) { m.y = m.gy - m.h; m.vy = 0f }
    }
    // contact
    if (ov(p.x, p.y, p.w, p.h, m.x, m.y, m.w, m.h) && m.inv == 0) {
        if (p.vy > 0 && p.y + p.h - p.vy <= m.y + 8) {
            m.hp--; m.inv = 60; p.vy = -6.6f; Sfx.fx("bosshit"); burst(mcx, m.y, "#f4c430", 14); vib(45)
            if (m.k != 'P' && m.k != 'S') { m.st = 3; m.stT = 40 }
            if (m.hp <= 0) { m.dead = 1; Sfx.fx("win") }
        } else if (m.st != 3 || m.k == 'V' || m.k == 'P') hurt()
    }
}

fun Game.drawMini() {
    val m = mini ?: return
    for (s in shots) drawShot(s)
    if (m.dead > 30) return
    if (m.inv > 0 && frame % 6 < 3) return
    val c = G.c
    val shake = if (m.st == 1 && frame % 4 < 2) 1f else 0f
    c.save(); c.translate((m.x - camX).roundToInt() + shake, (m.y - camY).roundToInt().toFloat())
    if (m.dead > 0) { c.translate(0f, m.h * 0.6f); c.scale(1f, 0.4f) }
    val face = if (m.k == 'P') (if (m.st == 2) sign(m.tx - (m.x + m.w / 2)).toInt().let { if (it == 0) 1 else it } else 1) else m.dir
    if (face < 0) { c.translate(m.w, 0f); c.scale(-1f, 1f) }
    val f = (m.t / 7) % 2
    val parts: List<Part> = when (m.k) {
        'G' -> listOf(P(9, 15, 2, 5 - f, "#f08a24"), P(15, 15, 2, 4 + f, "#f08a24"), P(2, 6, 18, 10, "#f2f2f2") { px("#cfcfd6", 5, 8, 9, 3); px("#cfcfd6", 2, 14, 18, 1) },
            P(17, 0, 3, 9, "#f2f2f2"), P(20, 1, 6, 3, "#f08a24") { px("#c25e10", 20, 3, 6, 1) },
            P(16, -3, 5, 4, "#f2f2f2") { px("#000000", 19, -2); px("#000000", 17, -3, 3, 1) }, P(15, -7, 7, 3, "#f4c430") { px("#e24b4a", 17, -6, 1, 1); px("#2457c5", 19, -6, 1, 1) })
        'A' -> listOf(P(0, 10, 22, 9, "#8b5a2b") { px("#6b3f1f", 0, 15, 22, 1); for (k in 0 until 3) px("#e9c94a", 2 + k * 7, 6, 6, 5) },
            P(2, 18, 5, 5, "#222222"), P(15, 18, 5, 5, "#222222"),
            P(24, 15, 3, 7, "#3a2c24"), P(28, 15 - f, 3, 7, "#3a2c24"), P(23, 6, 9, 10, "#2457c5") { px("#f5f0e6", 23, 10, 9, 1) },
            P(24, 0, 7, 7, "#e8ad7e") { px("#000000", 29, 2); px("#1c1410", 26, 5, 4, 1) }, P(23, -2, 9, 3, "#15151c"), P(20, 9, 4, 2, "#e8ad7e"))
        'K' -> listOf(P(3, 16, 3, 8, "#1f2430"), P(8, 16 - f, 3, 8, "#1f2430"), P(2, 7, 10, 10, "#2e6fd8") { px("#f4c430", 2, 11, 10, 1); px("#f4c430", 9, 8, 2, 2) },
            P(3, 1, 8, 7, "#e8ad7e") { px("#000000", 9, 3); px("#8a3324", 6, 6, 3, 1) }, P(2, -2, 10, 4, "#2e6fd8") { px("#f4c430", 6, -2, 2, 1) }, P(11, 9, 3, 2, "#e8ad7e"))
        'V' -> listOf(P(0, 13, 7, 7, "#222222") { px("#9aa1ab", 2, 15, 3, 3) }, P(18, 13, 7, 7, "#222222") { px("#9aa1ab", 20, 15, 3, 3) },
            P(3, 12, 19, 2, "#d8373b"), P(18, 5, 2, 8, "#9aa1ab"), P(16, 4, 6, 2, "#2b2b33"),
            P(7, 8 - f, 3, 6, "#3561a8"), P(11, 8 + f, 3, 6, "#3561a8"), P(6, 0, 10, 9, "#111111") { px("#f4c430", 6, 4, 10, 1) },
            P(9, -7, 8, 7, "#e8ad7e") { px("#000000", 14, -5, 3, 1) }, P(8, -9, 10, 3, "#d8373b"))
        'P' -> listOf(P(0, if (f == 1) 0 else 6, 10, 3, "#a7b0c2"), P(16, if (f == 1) 0 else 6, 10, 3, "#a7b0c2"),
            P(4, 4, 18, 8, "#8a93a6") { px("#5f6b80", 6, 6, 12, 1); px("#5f6b80", 6, 9, 10, 1) },
            P(17, 1, 7, 6, "#6b7690") { px("#ff7a00", 21, 3); px("#2fb39a", 17, 5, 6, 1) }, P(24, 3, 3, 2, "#e8b25a"),
            P(17, -3, 6, 3, "#f4c430") { px("#e24b4a", 19, -2, 1, 1) }, P(6, 12, 2, 2, "#e8b25a"), P(14, 12, 2, 2, "#e8b25a"))
        'S' -> { val cr = if (m.st == 1) 3 else 0
            listOf(P(3, 13 + cr, 3, 5 - cr, "#f08a24"), P(9, 13 + cr - f, 3, 5 - cr, "#f08a24"), P(18, 13 + cr, 3, 5 - cr, "#f08a24"), P(24, 13 + cr + f, 3, 5 - cr, "#f08a24"),
                P(0, 4 + cr, 4, 2, "#f08a24"), P(2, 5 + cr, 24, 9, "#f08a24") { for (k in 0 until 5) px("#1c1410", 6 + k * 4, 6 + cr, 1, 6); px("#f5f0e6", 4, 11 + cr, 20, 2) },
                P(22, 0 + cr, 9, 9, "#f08a24") { px("#1c1410", 23, 1 + cr, 1, 3); px("#000000", 28, 3 + cr, 1, 1); px("#f5f0e6", 26, 6 + cr, 5, 2) },
                P(25, -3 + cr, 6, 4, "#f4c430") { px("#fff6c8", 27, -2 + cr, 2, 2) })   // the sun face from the madrasa facade
        }
        else -> listOf( // 'T' camel
            P(4, 18, 3, 10 - f, "#c08a4a"), P(10, 18, 3, 10 + f - 1, "#c08a4a"), P(18, 18, 3, 10 - f, "#c08a4a"), P(23, 18, 3, 9 + f, "#c08a4a"),
            P(1, 8, 26, 11, "#d9a35a") { px("#b8833a", 1, 16, 26, 3); px("#d8373b", 6, 8, 14, 3); px("#f4c430", 6, 11, 14, 1) },
            P(8, 2, 10, 7, "#d9a35a"), P(25, 0, 4, 12, "#d9a35a"), P(25, -5, 9, 6, "#d9a35a") { px("#000000", 31, -4, 1, 1); px("#8a5422", 32, -1, 2, 1) },
            P(26, -7, 2, 3, "#c08a4a"))
    }
    G.drawParts(parts, 0f)
    if (m.st == 3 && m.k != 'P' && m.k != 'K') for (i in 0 until 3) { val a = frame / 6f + i * 2.1f; px("#f4c430", m.w / 2 + sin(a) * 8, -6 + kotlin.math.cos(a) * 2, 2, 2) }
    c.restore()
    val nm = MINI_NAMES[m.k] ?: arrayOf("БОСС", "BOSS")
    for (i in 0 until 3) G.spr(Spr.HRT, W / 2 - 14 + i * 10, 22, Spr.pal('R' to if (i < m.hp) "#7b3fa0" else "#4a4a5a"))
    txt(L(nm[0], nm[1]), W / 2f, 13f, "#f4c430", SM, 1)
}
