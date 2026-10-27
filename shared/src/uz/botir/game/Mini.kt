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
    'P' to arrayOf("КОРОЛЬ ГОЛУБЕЙ", "KAPTAR-QIROL"), 'S' to arrayOf("ШЕР С МЕДРЕСЕ", "SHERDOR SHERI"), 'T' to arrayOf("ЗЛОЙ ВЕРБЛЮД", "JAHLDOR TUYA"),
    'W' to arrayOf("ШЕЛКОПРЯД-ВЕЛИКАН", "ALP IPAK QURTI"), 'L' to arrayOf("ГЛИНЯНЫЙ ГОЛЕМ", "LOY DEV"))

private val MINI_SIZE = mapOf('G' to (26f to 20f), 'A' to (34f to 22f), 'K' to (14f to 24f), 'V' to (26f to 20f), 'P' to (26f to 14f), 'S' to (30f to 18f), 'T' to (30f to 28f), 'W' to (30f to 14f), 'L' to (18f to 26f))

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
        'W' -> L("Шелкопряд-великан: «Я съел все листья туты! Теперь твоя очередь!»", "Alp ipak qurti: «Barcha tut barglarini yedim! Endi navbat senga!»")
        'L' -> L("Глиняный голем: «Ты разбил мой кувшин? Теперь держись!»", "Loy dev: «Ko'zamni sindirdingmi? Endi ushla!»")
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
        'G', 'A', 'V', 'W' -> {
            val walk = if (m.k == 'V') 0f else 0.6f
            when (m.st) {
                0 -> { // patrol
                    val sp = if (m.k == 'V') 2.2f + (3 - m.hp) * 0.4f else walk
                    m.x += m.dir * sp
                    if (m.x < m.x0 + 4) { m.x = m.x0 + 4; if (m.k == 'V') { m.st = 3; m.stT = 40; Sfx.fx("bell") }; m.dir = 1 }
                    if (m.x + m.w > m.x1 - 4) { m.x = m.x1 - 4 - m.w; if (m.k == 'V') { m.st = 3; m.stT = 40; Sfx.fx("bell") }; m.dir = -1 }
                    if (m.k == 'V' && m.t % 170 == 0 && m.vy == 0f) m.vy = -5.5f
                    if (m.k != 'V' && m.t % (120 - (3 - m.hp) * 20) == 0) { m.st = 1; m.stT = 30; m.dir = if (pcx > mcx) 1 else -1; Sfx.fx(if (m.k == 'G') "honk" else "roar") }
                    if (m.k == 'W' && m.t % 130 == 65) for (s in -1..1 step 2) shots.add(Shot(mcx, m.y + 2, s * 1.4f, -4.5f, 'k'))
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
        'K', 'L' -> {
            m.x += m.dir * (if (m.k == 'L') 0.6f else 0.8f)
            if (m.x < m.x0 + 4) { m.x = m.x0 + 4; m.dir = 1 }; if (m.x + m.w > m.x1 - 4) { m.x = m.x1 - 4 - m.w; m.dir = -1 }
            if (m.t % 140 == 0 && m.vy == 0f) m.vy = -6f
            val per = 90 - (3 - m.hp) * 18
            if (m.t % per == 0) { val dx = pcx - mcx; shots.add(Shot(mcx, m.y + 4, (dx / 45f).coerceIn(-3f, 3f), -4.2f, if (m.k == 'L') 'r' else 'j')); Sfx.fx("throw")
                if (m.k == 'L' && m.hp < 3) shots.add(Shot(mcx, m.y + 4, (dx / 70f).coerceIn(-3f, 3f), -5.4f, 'r')) }
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
    val parts = Foes.mini(m.k, m.t, m.st)
    G.drawParts(parts, 0f)
    if (m.st == 3 && m.k != 'P' && m.k != 'K' && m.k != 'L') for (i in 0 until 3) { val a = frame / 6f + i * 2.1f; px("#f4c430", m.w / 2 + sin(a) * 8, -6 + kotlin.math.cos(a) * 2, 2, 2) }
    c.restore()
    val nm = MINI_NAMES[m.k] ?: arrayOf("БОСС", "BOSS")
    for (i in 0 until 3) G.spr(Spr.HRT, W / 2 - 14 + i * 10, 22, Spr.pal('R' to if (i < m.hp) "#7b3fa0" else "#4a4a5a"))
    txt(L(nm[0], nm[1]), W / 2f, 13f, "#f4c430", SM, 1)
}
