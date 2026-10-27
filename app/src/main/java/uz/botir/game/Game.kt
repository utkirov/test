package uz.botir.game

import android.content.Context
import android.graphics.Canvas
import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

const val W = 180
const val H = 270
const val T = 16
const val SM = 16f / 3f   // small pixel font size (16 real px at 3x)
const val BIG = 8f        // big pixel font size (24 real px at 3x)

open class Body(var x: Float, var y: Float, var w: Float, var h: Float) {
    var vx = 0f; var vy = 0f; var on = false; var onPipe = false
    var land = '.'; var landTx = 0; var landTy = 0
}

class Player(x: Float, y: Float) : Body(x, y, 10f, 26f) {
    var face = 1; var hp = 3; var inv = 0; var pow = 0; var coy = 0; var buf = 0; var jumps = 0; var dist = 0f
    var safeX = x; var safeY = y; var carry = 0; var mp: MP? = null
    var zip: Zip? = null; var zipT = 0f; var zipCd = 0; var inWater = false
}

class Enemy(val t: Char, x: Float, y: Float, w: Float, h: Float, var min: Float, var max: Float) : Body(x, y, w, h) {
    val y0 = y; var dead = 0; var f = (Math.random() * 60).toInt(); val fly = t == 'b' || t == 'h'
}

class Item(val t: Char, var x: Float, var y: Float, val egg: String? = null) {
    var w = 10f; var h = 10f; val ph = (Math.random() * 6).toFloat(); var fall = false; var vy = 0f; var drift = 0.6f
}

class MP(var x: Float, var y: Float, val w: Float, val ax: Char, val a0: Float, val a1: Float, val sp: Float, val tag: String, val kind: String) {
    var dir = 1; var dx = 0f; var dy = 0f; val h = 6f
}

class Zone(val id: String, val n: Array<String>, val m: Array<String>, val x: Float, val y: Float, val w: Float, val h: Float,
           val cond: String, val eff: String? = null, val et: Char = ' ', val cnt: Int = 0)

class Npc(val tx: Int, val ty: Int, val body: String, val hat: String, val atlas: Boolean = false, val scarf: Boolean = false, val beard: Boolean = false,
          val glasses: Boolean = false, val wave: Boolean = false, val note: Int = 0, val dancer: Boolean = false, val arm: Int = 0)

class Zip(val x0: Float, val y0: Float, val x1: Float, val y1: Float)
class Wind(val y0: Float, val y1: Float, val dir: Int, val phase: Int)
class Thrower(val x: Float, val y: Float, val trig: Float, val egg: String) { var done = false }
class Goal(val x: Float, val y: Float, val w: Float, val h: Float, val kind: String)
class Boss(var x: Float, var y: Float) { val w = 34f; val h = 40f; var vx = -0.8f; var vy = 0f; var hp = 3; var inv = 0; var t = 0; var dead = 0 }
class Shot(var x: Float, var y: Float, var vx: Float, var vy: Float) { var dead = false }
class Fx(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Int, val c: String)
class Toast(val s: String, var f: Int)
class Btn(val x: Float, val y: Float, val w: Float, val h: Float, val label: String, val sub: String = "", val style: Int = 0, val act: () -> Unit)
class Theme(val top: String, val top2: String, val body: String, val dot: String, val wall: String, val mortar: String, val plank: String, val plank2: String, val awning: Boolean = false)

class LevelDef(val n: Array<String>, val intro: Array<String>, val mech: Array<String>, val bg: Int, val th: Theme,
               val vertical: Boolean = false, val boss: Boolean = false,
               val build: Game.(B) -> Unit, val back: (Game.() -> Unit)? = null, val front: (Game.() -> Unit)? = null, val over: (Game.() -> Unit)? = null)

/** Level builder helpers (tile coordinates). */
class B(val g: Game) {
    fun G(a: Int, b: Int, top: Int, bot: Int = g.rows - 1) { for (x in a..b) for (y in top..bot) g.map[y][x] = '#' }
    fun P(a: Int, b: Int, y: Int, c: Char = '=') { for (x in a..b) g.map[y][x] = c }
    fun WB(a: Int, b: Int, y0: Int, y1: Int) { for (x in a..b) for (y in y0..y1) g.map[y][x] = 'B' }
    fun WA(a: Int, b: Int, y0: Int, y1: Int) { for (x in a..b) for (y in y0..y1) g.map[y][x] = 'w' }
    fun I(t: Char, x: Int, y: Int, egg: String? = null) { g.items.add(Item(t, x * T + 3f, y * T + 3f, egg)) }
    fun N(a: Int, b: Int, y: Int) { for (x in a..b) I('n', x, y) }
    fun E(t: Char, x: Int, y: Int, range: Int = 4) {
        val h = when (t) { 'g' -> 12f; 'b', 'h' -> 8f; else -> 20f }
        val sp = when (t) { 'g' -> 0.5f; 'k' -> 1.1f; 's' -> 0.7f; 'c' -> 0.9f; 'b' -> 0.8f; else -> 0.7f }
        val e = Enemy(t, x * T + 2f, (y + 1) * T - h, 12f, h, (x - range) * T.toFloat(), (x + range) * T.toFloat()); e.vx = -sp; g.enemies.add(e)
    }
    fun MP(x: Int, y: Int, w: Int, ax: Char, a0: Int, a1: Int, sp: Float, tag: String, kind: String) {
        g.mps.add(uz.botir.game.MP(x * T.toFloat(), y * T.toFloat(), w * T.toFloat(), ax, a0 * T.toFloat(), a1 * T.toFloat(), sp, tag, kind))
    }
    fun Z(id: String, n: Array<String>, m: Array<String>, x: Int, y: Int, w: Int, h: Int, cond: String, eff: String? = null, et: Char = ' ', cnt: Int = 0) {
        g.zones.add(Zone(id, n, m, x * T.toFloat(), y * T.toFloat(), w * T.toFloat(), h * T.toFloat(), cond, eff, et, cnt))
    }
    fun NPC(n: Npc) { g.npcs.add(n) }
    fun goal(tx: Int, ty: Int, kind: String = "gate") { g.goal = Goal(tx * T.toFloat(), ty * T.toFloat(), 32f, 48f, kind) }
    fun ZIP(x0: Float, y0: Float, x1: Float, y1: Float) { g.zips.add(Zip(x0 * T, y0 * T, x1 * T, y1 * T)) }
    fun WIND(r0: Int, r1: Int, dir: Int, phase: Int) { g.winds.add(Wind(r0 * T.toFloat(), (r1 + 1) * T.toFloat(), dir, phase)) }
}

class Game(private val ctx: Context) {
    // ---- persistent ----
    private val prefs = ctx.getSharedPreferences("botir", Context.MODE_PRIVATE)
    var lang = prefs.getString("lang", "ru") ?: "ru"
    var unlock = prefs.getInt("unlock", 0)
    fun savedEggs(i: Int): Set<String> = prefs.getStringSet("eggs$i", emptySet()) ?: emptySet()
    fun L(ru: String, uz: String) = if (lang == "uz") uz else ru

    // ---- level data ----
    var LVI = 0
    lateinit var LV: LevelDef
    var map: Array<CharArray> = arrayOf(CharArray(1))
    var rows = 17; var cols = 1
    val items = ArrayList<Item>(); val enemies = ArrayList<Enemy>(); val zones = ArrayList<Zone>(); val mps = ArrayList<MP>()
    val npcs = ArrayList<Npc>(); val throwers = ArrayList<Thrower>(); val zips = ArrayList<Zip>(); val winds = ArrayList<Wind>()
    val crumble = HashMap<Int, Int>()
    var goal: Goal? = null
    var arenaX0 = -1f; var arenaX1 = -1f
    var boss: Boss? = null; val shots = ArrayList<Shot>()
    val fx = ArrayList<Fx>(); val toasts = ArrayList<Toast>(); val found = HashSet<String>()
    var veh: String? = null; var vehX = 0f
    var coins = 0; var total = 0; var frame = 0
    var msg: String? = null; var msgF = 0
    // per-run counters
    var idle = 0; var still = 0; var dance = 0; var lowg = 0; var bossHits = 0
    val jc = HashMap<String, Int>(); val stomps = HashMap<Char, Int>(); val tips = HashSet<String>()
    var camX = 0f; var camY = 0f
    lateinit var pl: Player

    // ---- input ----
    var kL = false; var kR = false; var kJ = false
    fun jumpPressed() { if (state == "play") pl.buf = 7 }

    // ---- UI ----
    var state = "title"; var introI = 0
    val btns = ArrayList<Btn>()
    val levels: List<LevelDef> = buildLevels()
    val eggTotals: List<Int> by lazy { levels.indices.map { i -> countZones(i) } }

    private fun countZones(i: Int): Int {
        val keep = LVI; loadLevel(i); val n = zones.size; loadLevel(keep); return n
    }

    fun X(t: Int) = (t * T - camX).roundToInt().toFloat()
    fun Y(t: Int) = (t * T - camY).roundToInt().toFloat()

    // ================= tiles & physics =================
    fun tileAt(tx: Int, ty: Int): Char = if (tx < 0 || tx >= cols) '#' else if (ty < 0 || ty >= rows) '.' else map[ty][tx]
    private fun solidC(c: Char) = c == '#' || c == 'B' || c == 'J' || c == 'C' || c == 'E' || c == 'e'
    fun solidAt(x: Float, y: Float) = solidC(tileAt(Math.floor((x / T).toDouble()).toInt(), Math.floor((y / T).toDouble()).toInt()))
    private fun tAt(x: Float, y: Float) = tileAt(Math.floor((x / T).toDouble()).toInt(), Math.floor((y / T).toDouble()).toInt())

    private fun moveX(o: Body): Boolean {
        o.x += o.vx; var hit = false
        val ys = floatArrayOf(o.y + 1, o.y + o.h / 2, o.y + o.h - 1)
        if (o.vx > 0) { if (ys.any { solidAt(o.x + o.w, it) }) { o.x = Math.floor(((o.x + o.w) / T).toDouble()).toFloat() * T - o.w - 0.01f; hit = true } }
        else if (o.vx < 0) { if (ys.any { solidAt(o.x, it) }) { o.x = (Math.floor((o.x / T).toDouble()).toFloat() + 1) * T; hit = true } }
        if (hit) o.vx = 0f
        return hit
    }

    private fun moveY(o: Body) {
        val pb = o.y + o.h; o.y += o.vy; o.on = false; o.onPipe = false; o.land = '.'
        val xs = floatArrayOf(o.x + 1, o.x + o.w - 1)
        if (o.vy > 0) {
            val ty = Math.floor(((o.y + o.h) / T).toDouble()).toInt(); val top = ty * T.toFloat()
            for (x in xs) {
                val tx = Math.floor((x / T).toDouble()).toInt(); val c = tileAt(tx, ty)
                if (solidC(c) || ((c == '=' || c == 'p') && pb <= top + 0.5f)) {
                    o.y = top - o.h; o.vy = 0f; o.on = true; if (c == 'p') o.onPipe = true
                    if (o.land == '.' || c == 'J') { o.land = c; o.landTx = tx; o.landTy = ty }
                }
            }
        } else if (o.vy < 0) {
            if (xs.any { solidAt(it, o.y) }) { o.y = (Math.floor((o.y / T).toDouble()).toFloat() + 1) * T; o.vy = 0f }
        }
    }

    private fun ov(ax: Float, ay: Float, aw: Float, ah: Float, bx: Float, by: Float, bw: Float, bh: Float) = ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by
    private fun ovP(z: Zone) = ov(pl.x, pl.y, pl.w, pl.h, z.x, z.y, z.w, z.h)

    // ================= flow =================
    fun loadLevel(i: Int) {
        LVI = i; LV = levels[i]
        items.clear(); enemies.clear(); zones.clear(); mps.clear(); npcs.clear(); throwers.clear(); zips.clear(); winds.clear(); crumble.clear()
        goal = null; arenaX0 = -1f; arenaX1 = -1f; boss = null; shots.clear(); fx.clear(); toasts.clear(); found.clear(); veh = null
        LV.build(this, B(this))
        total = items.count { it.t == 'n' }; coins = 0; msg = null
        var sy = 0f
        for (y in 0 until rows) if (solidC(tileAt(2, y))) { sy = y * T - 26f; break }
        pl = Player(2f * T, sy)
        idle = 0; still = 0; dance = 0; lowg = 0; bossHits = 0; jc.clear(); stomps.clear(); tips.clear()
        camX = 0f; camY = max(0f, min(rows * T - H.toFloat(), pl.y - H * 0.55f))
    }

    fun newMap(c: Int, r: Int) { cols = c; rows = r; map = Array(r) { CharArray(c) { '.' } } }

    private fun startLevel(i: Int) { Sfx.play(440f, 0.05f); loadLevel(i); state = "play"; frame = 0; tip(L(levels[i].mech[0], levels[i].mech[1])) }

    fun tip(s: String) { toasts.add(Toast(s, 260)) }
    fun toast(s: String) { toasts.add(Toast(s, 230)) }

    fun egg(z: Zone?) {
        if (z == null || found.contains(z.id)) return
        found.add(z.id)
        val s = HashSet(savedEggs(LVI)); if (s.add(z.id)) prefs.edit().putStringSet("eggs$LVI", s).apply()
        toast("* " + L(z.m[0], z.m[1])); Sfx.tune(intArrayOf(523, 659, 784, 1046), 110); burst(pl.x + 5, pl.y, "#f4c430", 14)
        when (z.eff) {
            "hp" -> pl.hp = min(5, pl.hp + 1)
            "lowg" -> lowg = 600
            "dance" -> { dance = 120; Sfx.tune(intArrayOf(587, 659, 698, 659, 587, 523, 587), 140, 1) }
            "pow" -> pl.pow = 600
            "carry" -> pl.carry = 60
            "ring" -> Sfx.tune(intArrayOf(880, 880, 0, 880, 880), 120)
            "chime" -> Sfx.tune(intArrayOf(659, 523, 587, 392), 220, 1)
            else -> if (z.eff != null && z.eff.startsWith("veh:")) { veh = z.eff.substring(4); vehX = -70f }
        }
    }

    private fun hurt() {
        if (pl.inv > 0 || pl.pow > 0 || pl.carry > 0) return
        pl.hp--; pl.inv = 100; pl.vy = -3.5f; pl.vx = -pl.face * 2f; pl.zip = null; Sfx.play(140f, 0.2f, 2, 0.25f, 0.7f)
        if (pl.hp <= 0) { state = "dead"; delayed(30) { show("over") } }
    }
    private fun respawn() { pl.inv = 0; pl.pow = 0; hurt(); if (pl.hp > 0) { pl.x = pl.safeX; pl.y = pl.safeY; pl.vx = 0f; pl.vy = 0f; pl.mp = null; pl.zip = null } }
    fun burst(x: Float, y: Float, c: String, n: Int = 8) { repeat(n) { fx.add(Fx(x, y, ((Math.random() - .5) * 2.5).toFloat(), (-Math.random() * 2.5).toFloat(), 25, c)) } }

    private val timers = ArrayList<Pair<Int, () -> Unit>>()
    private fun delayed(frames: Int, f: () -> Unit) { timers.add(Pair(frame + frames, f)) }

    // ================= update =================
    fun update() {
        frame++
        if (timers.isNotEmpty()) { val due = timers.filter { it.first <= frame }; timers.removeAll(due.toSet()); due.forEach { it.second() } }
        if (state != "play") {
            stepFx()
            if (state == "title" || state == "intro") { camX += 0.3f; if (camX > cols * T - W) camX = 0f }
            return
        }
        val p = pl
        val dir = (if (kR) 1 else 0) - (if (kL) 1 else 0)
        for (m in mps) {
            val o = if (m.ax == 'x') m.x else m.y; var n = o + m.sp * m.dir
            if (n > m.a1) { n = m.a1; m.dir = -1 }; if (n < m.a0) { n = m.a0; m.dir = 1 }
            m.dx = if (m.ax == 'x') n - o else 0f; m.dy = if (m.ax == 'y') n - o else 0f
            if (m.ax == 'x') m.x = n else m.y = n
        }
        p.mp?.let { p.x += it.dx; p.y += it.dy }
        if (p.zipCd > 0) p.zipCd--
        val pb: Float
        val cx = p.x + p.w / 2
        p.inWater = tAt(cx, p.y + p.h * 0.6f) == 'w'

        if (p.zip != null) {
            pb = p.y + p.h; updateZip()
        } else if (p.inWater) {
            if (!found.contains("anhor")) egg(zones.find { it.cond == "water" })
            if (tips.add("swim")) tip(L("Плыви! Жми прыжок, чтобы грести вверх.", "Suz! Yuqoriga suzish uchun sakrashni bos."))
            if (dir != 0) { p.vx += dir * 0.18f; p.vx = p.vx.coerceIn(-1.1f, 1.1f); p.face = dir } else p.vx *= 0.9f
            if (p.buf > 0) {
                val nearTop = tAt(cx, p.y + 2) != 'w'
                p.vy = if (nearTop) -5.8f else -2.4f; p.buf = 0; Sfx.play(if (nearTop) 420f else 300f, 0.06f, 1, 0.2f)
                burst(cx, p.y + 4, "#cfeeff", 3)
            }
            p.buf--
            p.vy = min(1.3f, p.vy + 0.09f)
            pb = p.y + p.h
            val sv = p.vx; p.vx = sv + 0.25f; val hit = moveX(p); p.vx = if (hit) 0f else sv; moveY(p); p.dist += abs(p.vx)
        } else {
            if (dir != 0 && dance <= 0) { p.vx += dir * 0.28f; p.vx = p.vx.coerceIn(-1.75f, 1.75f); p.face = dir }
            else { p.vx *= if (p.on) 0.72f else 0.92f; if (abs(p.vx) < 0.05f) p.vx = 0f }
            if (p.on) { p.coy = 6; p.jumps = 0 } else p.coy--
            if (p.buf > 0) {
                if (p.coy > 0) {
                    p.vy = if (lowg > 0) -4.6f else -6.1f; p.coy = 0; p.buf = 0; p.jumps = 1; p.mp = null; Sfx.play(420f)
                    for (z in zones) if (z.cond == "jump" && ovP(z)) { val n = (jc[z.id] ?: 0) + 1; jc[z.id] = n; if (n >= 3) egg(z) }
                } else if (p.pow > 0 && p.jumps < 2) { p.vy = -5.6f; p.jumps = 2; p.buf = 0; Sfx.play(620f); burst(p.x + 5, p.y + 26, "#f4c430", 6) }
            }
            p.buf--
            if (!kJ && p.vy < -2.4f && p.carry == 0 && bounced == 0) p.vy = -2.4f
            if (p.carry > 0) { p.carry--; p.vy = -6.5f; if (frame % 3 == 0) burst(p.x + 5, p.y, "#f4c430", 2) }
            else p.vy = min(6f, p.vy + if (lowg > 0) 0.14f else 0.32f)
            pb = p.y + p.h
            moveX(p); moveY(p); p.dist += abs(p.vx)
            if (bounced > 0) bounced--
            afterLanding()
        }
        if (lowg > 0) lowg--
        if (p.x < 0) p.x = 0f
        if (arenaX0 > 0 && p.x > arenaX0 + T) { if (boss == null) spawnBoss() }
        if (boss != null && p.x < arenaX0) p.x = arenaX0
        // moving platforms landing
        if (p.vy >= 0 && !p.on && p.zip == null) {
            p.mp = null
            for (m in mps) if (p.x + p.w > m.x && p.x < m.x + m.w && pb <= m.y + max(0f, m.dy) + 1.5f && p.y + p.h >= m.y) { p.y = m.y - p.h; p.vy = 0f; p.on = true; p.mp = m }
        } else if (p.vy < 0) p.mp = null
        if (p.on && p.mp == null && solidAt(p.x, p.y + p.h + 2) && solidAt(p.x + p.w, p.y + p.h + 2) && p.land != 'C') { p.safeX = p.x; p.safeY = p.y }
        if (p.inv > 0) p.inv--
        if (p.pow > 0) { p.pow--; if (frame % 4 == 0) burst(p.x + 5, p.y + 10, "#f4c430", 1) }
        if (p.zip == null && p.zipCd == 0 && !p.inWater) tryZip()
        updWind(); updCrumble()
        if (p.y > rows * T + 20) respawn()
        // zones
        val busy = kL || kR || kJ
        val ride = p.mp?.let { it.dx != 0f || it.dy != 0f } ?: false
        if (!busy && p.on && abs(p.vx) < 0.1f && !ride && p.zip == null) { idle++; still++ } else { idle = 0; still = 0 }
        for (z in zones) {
            if (found.contains(z.id)) continue
            when (z.cond) {
                "touch" -> if (ovP(z)) egg(z)
                "still" -> if (ovP(z) && still > 90) egg(z)
                "pass" -> if (p.x > z.x) egg(z)
                "idle" -> if (idle > 360) egg(z)
                "pipe" -> if (p.onPipe) egg(z)
                "ride" -> if (p.mp != null && p.mp!!.tag == z.id && ride) egg(z)
                "zip" -> if (p.zip != null) egg(z)
                "see" -> if (z.y + z.h > camY && z.y < camY + H && abs((z.y + z.h / 2) - (camY + H / 2)) < H * 0.3f) egg(z)
            }
        }
        for (z in zones) if (z.eff == "dance" && found.contains(z.id) && ovP(z) && still > 90 && dance <= 0) dance = 120
        if (dance > 0) dance--
        if (p.onPipe && frame % 3 == 0) burst(p.x + 5, p.y + p.h, "#ffd65a", 1)
        for (t in throwers) if (!t.done && p.x > t.trig) { t.done = true; val it = Item('s', t.x, t.y, t.egg); it.fall = true; it.vy = -2f; items.add(it) }
        if (veh != null) { vehX += 3.2f; if (vehX > W + 80) veh = null }
        updEnemies(); updItems()
        if (boss != null) updBoss()
        stepFx()
        if (!LV.boss) goal?.let { g -> if (ov(p.x, p.y, p.w, p.h, g.x + 12, g.y, g.w - 12, g.h)) { state = "won"; Sfx.tune(intArrayOf(523, 659, 784, 1046), 150); delayed(48) { show("win") } } }
        var tx = p.x + p.w / 2 - W / 2 + p.face * 24
        if (boss != null) tx = tx.coerceIn(arenaX0, arenaX1 - W)
        camX += (tx - camX) * 0.1f; camX = camX.coerceIn(0f, max(0f, cols * T - W.toFloat()))
        val ty = p.y - H * 0.55f; camY += (ty - camY) * 0.12f; camY = camY.coerceIn(0f, max(0f, rows * T - H.toFloat()))
    }

    private var bounced = 0

    private fun afterLanding() {
        val p = pl
        if (!p.on) return
        when (p.land) {
            'J' -> {
                p.vy = -9.4f; p.on = false; p.jumps = 0; bounced = 30; Sfx.play(260f, 0.25f, 1, 0.25f, 2.6f); burst(p.x + 5, p.y + p.h, "#f4c430", 8)
                if (tips.add("tramp")) tip(L("Курпача-батут! Подбрасывает высоко.", "Ko'rpacha-batut! Baland otadi."))
            }
            'C' -> {
                val k = p.landTy * cols + p.landTx
                if (!crumble.containsKey(k)) { crumble[k] = 1; if (tips.add("crate")) tip(L("Ящики с дынями ломаются — не стой на них!", "Qovun yashiklari sinadi — ustida turma!")) }
            }
            'E', 'e' -> {
                val d = if (p.land == 'E') 1.1f else -1.1f
                val sv = p.vx; p.vx = d; moveX(p); p.vx = sv
                if (tips.add("esc")) tip(L("Эскалатор везёт тебя сам!", "Eskalator o'zi olib ketadi!"))
            }
        }
    }

    private fun updCrumble() {
        if (crumble.isEmpty()) return
        val it = crumble.entries.iterator(); val restore = ArrayList<Int>()
        while (it.hasNext()) {
            val e = it.next(); val k = e.key; val tx = k % cols; val ty = k / cols
            if (e.value > 0) {
                e.setValue(e.value + 1)
                if (e.value % 8 == 0) burst(tx * T + 8f, ty * T + 2f, "#d9963f", 1)
                if (e.value >= 36) { map[ty][tx] = '.'; burst(tx * T + 8f, ty * T + 8f, "#8b5a2b", 10); Sfx.play(160f, 0.15f, 2, 0.2f, 0.6f); e.setValue(-300) }
            } else {
                e.setValue(e.value + 1)
                if (e.value >= 0) {
                    if (!ov(pl.x, pl.y, pl.w, pl.h, tx * T.toFloat(), ty * T.toFloat(), T.toFloat(), T.toFloat())) { map[ty][tx] = 'C'; restore.add(k) } else e.setValue(-1)
                }
            }
        }
        restore.forEach { crumble.remove(it) }
    }

    private fun windActive(w: Wind) = ((frame + w.phase) % 240) < 110
    private fun updWind() {
        val p = pl
        for (w in winds) {
            if (!windActive(w)) continue
            val cy = p.y + p.h / 2
            if (cy > w.y0 && cy < w.y1 && p.zip == null) {
                val sv = p.vx; p.vx = w.dir * 0.85f; moveX(p); p.vx = sv
                if (tips.add("wind")) tip(L("Ветер на высоте! Держись против порывов.", "Balandda shamol! Shamolga qarshi tur."))
                egg(zones.find { it.cond == "wind" })
            }
        }
    }

    private fun tryZip() {
        val p = pl; val cx = p.x + p.w / 2
        for (z in zips) {
            if (cx < z.x0 - 4 || cx > z.x1 - 8) continue
            val t = ((cx - z.x0) / (z.x1 - z.x0)).coerceIn(0f, 1f)
            val ly = z.y0 + (z.y1 - z.y0) * t
            if (abs(p.y - ly) < 9 && !p.on) {
                p.zip = z; p.zipT = t; p.vx = 0f; p.vy = 0f; Sfx.play(700f, 0.08f, 1, 0.2f, 0.8f)
                if (tips.add("zip")) tip(L("Трос! Ботир едет сам. Прыжок — отпустить.", "Tros! Botir o'zi ketadi. Sakrash — qo'yib yuborish."))
                return
            }
        }
    }

    private fun updateZip() {
        val p = pl; val z = p.zip ?: return
        val len = sqrt((z.x1 - z.x0) * (z.x1 - z.x0) + (z.y1 - z.y0) * (z.y1 - z.y0))
        p.zipT += 2.6f / len
        val px0 = z.x0 + (z.x1 - z.x0) * min(1f, p.zipT); val py0 = z.y0 + (z.y1 - z.y0) * min(1f, p.zipT)
        p.x = px0 - p.w / 2; p.y = py0 + 2; p.vx = 0f; p.vy = 0f; p.on = false; p.face = 1
        if (frame % 2 == 0) burst(px0, py0, "#ffd65a", 1)
        if (p.buf > 0 || p.zipT >= 1f) { p.zip = null; p.zipCd = 25; p.vy = if (p.buf > 0) -5f else 0f; p.vx = 1.5f; p.buf = 0 }
        p.buf--
    }

    private fun updEnemies() {
        val p = pl
        val dead = ArrayList<Enemy>()
        for (e in enemies) {
            if (e.dead > 0) { e.dead++; if (e.fly) e.y += 3f; if (e.dead >= 25) dead.add(e); continue }
            e.f++
            if (e.fly) { e.x += e.vx; e.y = e.y0 + sin(e.f / 18f) * 10f; if (e.x < e.min || e.x > e.max) e.vx = -e.vx }
            else {
                e.vy = min(6f, e.vy + 0.32f); val sv = e.vx; if (moveX(e)) e.vx = -sv; moveY(e)
                if (e.on) { val ax = if (e.vx > 0) e.x + e.w + 1 else e.x - 1; val c = tAt(ax, e.y + e.h + 2); if (!solidC(c) && c != '=' && c != 'p') e.vx = -e.vx }
                if ((e.x < e.min && e.vx < 0) || (e.x > e.max && e.vx > 0)) e.vx = -e.vx
            }
            if (ov(p.x, p.y, p.w, p.h, e.x, e.y, e.w, e.h)) {
                if (e.t == 'h') { e.dead = 1; val z = zones.find { it.cond == "humo" }; if (z != null) egg(z) else p.carry = 60; continue }
                if (p.pow > 0 || p.carry > 0 || (p.vy > 0 && p.y + p.h - p.vy <= e.y + 6)) {
                    e.dead = 1; p.vy = if (kJ) -6f else -4f; Sfx.play(220f, 0.12f, 1, 0.3f); burst(e.x + 6, e.y + 4, "#ffffff", 10)
                    stomps[e.t] = (stomps[e.t] ?: 0) + 1
                    for (z in zones) if (z.cond == "stomp" && z.et == e.t && (stomps[e.t] ?: 0) >= z.cnt) egg(z)
                } else hurt()
            }
        }
        enemies.removeAll(dead.toSet())
    }

    private fun updItems() {
        val p = pl
        for (i in items) if (i.fall) {
            i.vy = min(5f, i.vy + 0.25f); i.y += i.vy; if (i.t != 'q') i.x += i.drift
            if (i.vy > 0 && solidAt(i.x + 5, i.y + i.h)) { i.y = Math.floor(((i.y + i.h) / T).toDouble()).toFloat() * T - i.h; i.fall = false }
        }
        val rm = ArrayList<Item>()
        for (i in items) {
            if (ov(p.x, p.y, p.w, p.h, i.x, i.y, i.w, i.h)) {
                when (i.t) {
                    'n' -> { coins++; Sfx.play(880f, 0.06f); burst(i.x + 5, i.y + 5, "#f0c27a", 5) }
                    's' -> { p.hp = min(5, p.hp + 1); Sfx.play(520f, 0.15f, 1); burst(i.x + 5, i.y + 5, "#e8b25a", 8) }
                    'd' -> { p.pow = 600; Sfx.play(660f, 0.3f, 1); msg = L("ДУППИ-СИЛА!", "DO'PPI KUCHI!"); msgF = 90; burst(i.x + 5, i.y + 5, "#f4c430", 14) }
                    'q' -> { state = "won"; Sfx.tune(intArrayOf(523, 659, 784, 1046, 784, 1046), 160); delayed(72) { show("final") } }
                    else -> burst(i.x + 5, i.y + 5, "#f4c430", 10)
                }
                i.egg?.let { id -> egg(zones.find { it.id == id }) }
                rm.add(i)
            } else if (i.y > rows * T + 40) rm.add(i)
        }
        items.removeAll(rm.toSet())
    }

    private fun stepFx() { for (q in fx) { q.x += q.vx; q.y += q.vy; q.vy += 0.12f; q.life-- }; fx.removeAll { it.life <= 0 } }

    // ================= boss =================
    private fun spawnBoss() {
        boss = Boss(104f * T, 14f * T - 40)
        toast(L("Жадина-ака: «Казан мой! Ха-ха!»", "Ochko'z aka: «Qozon meniki! Ha-ha!»")); Sfx.tune(intArrayOf(196, 185, 175, 165), 180, 2)
    }

    private fun updBoss() {
        val b = boss!!; val p = pl
        if (b.dead > 0) { b.dead++; if (b.dead == 60) { val it = Item('q', b.x + 10, b.y); it.w = 14f; it.fall = true; it.vy = -4f; items.add(it) }; return }
        b.t++; if (b.inv > 0) b.inv--
        val spd = 0.8f + (3 - b.hp) * 0.35f
        b.vx = (if (b.vx >= 0) 1 else -1) * spd; b.x += b.vx
        if (b.x < arenaX0 + 8) { b.x = arenaX0 + 8; b.vx = spd }
        if (b.x > arenaX1 - b.w - 8) { b.x = arenaX1 - b.w - 8; b.vx = -spd }
        b.vy += 0.32f; b.y += b.vy
        if (b.y > 14f * T - b.h) { b.y = 14f * T - b.h; if (b.vy > 3) { burst(b.x + 17, b.y + 40, "#c9ced6", 10); Sfx.play(90f, 0.2f, 2, 0.3f, 0.6f) }; b.vy = 0f }
        val per = max(55, 100 - (3 - b.hp) * 20)
        if (b.t % per == 0) { val dx = p.x - b.x - 12; shots.add(Shot(b.x + 17, b.y + 6, (dx / 50f).coerceIn(-3f, 3f), -4.5f)); Sfx.play(300f, 0.1f, 1, 0.2f, 0.6f) }
        if (b.t % (per * 3) == per * 3 - 1 && b.y >= 14f * T - b.h - 1) b.vy = -6.5f
        if (ov(p.x, p.y, p.w, p.h, b.x, b.y, b.w, b.h) && b.inv == 0) {
            if (p.vy > 0 && p.y + p.h - p.vy <= b.y + 8) {
                b.hp--; b.inv = 70; p.vy = -6.8f; Sfx.play(160f, 0.25f, 0, 0.3f, 0.5f); burst(b.x + 17, b.y, "#f4c430", 18)
                if (b.hp <= 0) {
                    b.dead = 1; toast(L("Жадина-ака: «Ой-ой! Забирай свой казан!»", "Ochko'z aka: «Voy-voy! Qozoningni ol!»")); Sfx.tune(intArrayOf(392, 330, 262, 196), 200, 1)
                    if (bossHits == 0) egg(zones.find { it.id == "mukammal" })
                }
            } else { hurt(); if (state == "play") bossHits++ }
        }
        for (s in shots) {
            s.vy += 0.2f; s.x += s.vx; s.y += s.vy
            if (ov(p.x, p.y, p.w, p.h, s.x - 3, s.y - 3, 6f, 6f)) { s.dead = true; hurt(); bossHits++ }
            if (s.y > 14 * T) s.dead = true
            if (s.dead) burst(s.x, s.y, "#f0c27a", 4)
        }
        shots.removeAll { it.dead }
    }

    // ================= render =================
    fun render(c: Canvas) {
        G.c = c; c.setMatrix(null); c.scale(3f, 3f)
        when (LV.bg) { 0 -> Bg.city(this); 1 -> Bg.bazaar(this); 2 -> Bg.metro(this); 3 -> Bg.park(this); 4 -> Bg.tower(this); else -> Bg.sky(this) }
        LV.back?.invoke(this)
        drawVeh(); drawTiles(); mps.forEach { drawMP(it) }; drawZips(); drawGoal()
        for (n in npcs) {
            val d = if (n.dancer && dance > 0 && frame % 20 < 10) -2 else 0
            Spr.person(X(n.tx) + 3, Y(n.ty) - 18 + d, n.body, n.hat, n.atlas, n.scarf, n.beard, n.glasses, if (n.wave) (if (frame % 30 < 15) 2 else 6) else n.arm)
            if (n.note != 0 && frame % 40 < 20) note(X(n.tx) + 14, Y(n.ty) - 28, n.note == 2)
        }
        LV.front?.invoke(this)
        for (i in items) {
            val x = (i.x - camX).roundToInt().toFloat(); val y = (i.y - camY + if (i.fall) 0f else sin(frame / 12f + i.ph) * 1.5f).roundToInt().toFloat()
            when (i.t) {
                'n' -> G.spr(Spr.NON, x, y, Spr.PAL)
                's' -> G.spr(Spr.SOM, x, y + 1, Spr.PAL)
                'd' -> { G.spr(Spr.DOP, x - 1, y + 2, Spr.PAL); if (frame % 20 < 10) px("#ffffff", x + 9, y - 1) }
                'q' -> G.spr(Spr.QOZ, x, y + 2, Spr.pal('O' to "#f0c27a"))
                else -> { G.spr(Spr.SX[i.egg] ?: Spr.SX["jeton"]!!, x + 2, y + 2, Spr.PAL); if (frame % 24 < 12) px("#ffffff", x + 10, y) }
            }
        }
        for (e in enemies) {
            val sh = Spr.enemyH(e.t)
            c.save(); c.translate((e.x + e.w / 2 - camX).roundToInt().toFloat(), (e.y + e.h - sh - camY).roundToInt().toFloat()); c.scale(if (e.vx > 0) 1f else -1f, 1f); c.translate(-8f, 0f)
            if (e.dead > 0) { c.translate(0f, sh * 0.6f); c.scale(1f, 0.4f) }
            G.drawParts(Spr.enemy(e.t, (e.f / (if (e.fly) 6 else 8)) % 2), 0f); c.restore()
        }
        drawBoss()
        val p = pl
        if (p.y > -500 && !(p.inv > 0 && (frame / 4) % 2 == 1)) {
            var lk = "idle"; var ub = 0f; var face = p.face
            when {
                p.zip != null -> lk = "hang"
                dance > 0 -> { lk = if (frame % 20 < 10) "dance" else "idle"; face = if (frame % 40 < 20) 1 else -1 }
                p.inWater -> lk = if ((frame / 10) % 2 == 0) "swim" else "jump"
                !p.on -> lk = "jump"
                abs(p.vx) > 0.2f -> { val f = (p.dist / 9).toInt() % 2; lk = if (f == 1) "run1" else "run0"; ub = if (f == 1) 0f else 1f }
                else -> ub = ((frame / 30) % 2).toFloat()
            }
            c.save(); c.translate((p.x + p.w / 2 - camX).roundToInt().toFloat(), (p.y + p.h - 31 - camY).roundToInt().toFloat()); c.scale(face.toFloat(), 1f); c.translate(-12f, 0f)
            G.drawParts(Spr.hero(lk, p.pow > 0 && !(p.pow < 120 && frame % 10 < 5)), ub); c.restore()
            if (p.carry > 0) { c.save(); c.translate((p.x + 5 - camX).roundToInt() - 8f, (p.y - camY - 10).roundToInt().toFloat()); G.drawParts(Spr.enemy('h', (frame / 5) % 2), 0f); c.restore() }
        }
        LV.over?.invoke(this)
        // water overlay so the swimmer looks submerged
        drawWaterTop()
        for (q in fx) px(q.c, (q.x - camX).roundToInt(), (q.y - camY).roundToInt(), 2, 2)
        drawWindFx()
        if (lowg > 0) for (i in 0 until 20) { val h = hash(i, 3); px("#fff1e8", h % W, (h shr 8) % 100) }
        if (state in setOf("play", "won", "dead", "pause")) hud()
        if (state != "play" && state != "won" && state != "dead") Menu.draw(this)
    }

    private fun note(x: Float, y: Float, heart: Boolean) {
        if (heart) { G.spr(Spr.HRT, x, y + 2, Spr.PAL) }
        else { px("#f4c430", x + 3, y, 1, 6); px("#f4c430", x + 3, y, 3, 1); px("#f4c430", x + 1, y + 5, 3, 2) }
    }

    private fun hud() {
        val p = pl
        for (i in 0 until max(3, p.hp)) G.spr(Spr.HRT, 5 + i * 9, 6, Spr.pal('R' to if (i < p.hp) "#e24b4a" else "#4a4a5a"))
        G.spr(Spr.NON, W - 50, 20, Spr.PAL); txt("$coins/$total", W - 38f, 22f, size = SM)
        star(W - 50f, 33f); txt("${found.size}/${zones.size}", W - 38f, 33f, "#f4c430", SM)
        if (p.pow > 0) { px(OUT, 5, 16, 52, 5); px("#f4c430", 6, 17, (50f * p.pow / 600).roundToInt(), 3) }
        if (lowg > 0) txt(L("НЕВЕСОМОСТЬ", "VAZNSIZLIK"), 5f, 24f, "#86c5ef", SM)
        if (LV.vertical) txt("${((1 - p.y / (rows * T)) * 375).roundToInt()}m", 5f, 24f, "#fff1e8", SM)
        // pause button
        px("#bb0f1631", W - 22, 4, 18, 12); px("#fff1e8", W - 16, 7, 2, 6); px("#fff1e8", W - 11, 7, 2, 6)
        if (msg != null && msgF > 0) { msgF--; txt(msg!!, W / 2f, 90f, "#f4c430", BIG, 1) }
        if (frame < 200 && toasts.isEmpty() && boss == null) txt("${LVI + 1}. " + L(LV.n[0], LV.n[1]), W / 2f, 60f, "#f4c430", BIG, 1)
        drawToast()
    }

    fun star(x: Float, y: Float) { px("#f4c430", x + 2, y, 1, 5); px("#f4c430", x, y + 2, 5, 1); px("#f4c430", x + 1, y + 1, 3, 3) }

    private fun drawToast() {
        if (toasts.isEmpty()) return
        val t = toasts[0]; t.f--; if (t.f <= 0) { toasts.removeAt(0); return }
        val lines = G.wrap(t.s, 152f, SM); val h = lines.size * 9 + 9; val y = if (boss != null) 42f else 48f
        px(OUT, 9, y - 1, 162, h + 2); px("#1d2b53", 10, y, 160, h); px("#f4c430", 10, y, 160, 2)
        lines.forEachIndexed { i, l -> txt(l, 90f, y + 5 + i * 9, "#fff1e8", SM, 1) }
    }

    // ---------- tiles ----------
    private fun drawTiles() {
        val th = LV.th
        val c0 = max(0, (camX / T).toInt()); val c1 = min(cols - 1, c0 + W / T + 1)
        val r0 = max(0, (camY / T).toInt()); val r1 = min(rows - 1, r0 + H / T + 1)
        for (ty in r0..r1) for (tx in c0..c1) {
            val ch = map[ty][tx]; if (ch == '.') continue
            val x = X(tx); val y = Y(ty); val h = hash(tx, ty)
            when (ch) {
                '#' -> { px(th.body, x, y, T, T); px(th.dot, x + (h % 12), y + 4 + (h shr 4) % 10, 2, 2)
                    if (tileAt(tx, ty - 1) != '#') { px(th.top, x, y, T, 5); px(th.top2, x, y, T, 2); px("#40000000", x, y + 5, T, 1) } }
                'B' -> { px(th.wall, x, y, T, T); px(th.mortar, x, y + 5, T, 1); px(th.mortar, x, y + 11, T, 1)
                    px(th.mortar, x + (if (ty % 2 == 1) 4 else 11), y, 1, 5); px(th.mortar, x + (if (ty % 2 == 1) 11 else 4), y + 6, 1, 5)
                    if (tileAt(tx, ty - 1) != 'B') px("#33000000", x, y, T, 2) }
                '=' -> { px(OUT, x, y - 1, T, 7)
                    if (th.awning) { for (k in 0 until 4) px(if (k % 2 == 1) th.plank2 else th.plank, x + k * 4, y, 4, 5); px("#33000000", x, y + 4, T, 1) }
                    else { px(th.plank, x, y, T, 5); px(th.plank2, x, y, T, 1); px("#40000000", x + 7, y, 1, 5) } }
                'p' -> { px(OUT, x, y - 1, T, 7); px("#f2c230", x, y, T, 5); px("#fff1a0", x, y, T, 1); px("#b88a10", x, y + 4, T, 1)
                    if (tx % 2 == 0) { px(OUT, x + 6, y + 5, 4, 48); px("#f2c230", x + 7, y + 5, 2, 48) } }
                'w' -> { px("#3f8fcf", x, y, T, T); if (tileAt(tx, ty - 1) != 'w') { px("#86c5ef", x, y, T, 2); px("#e8f6ff", x + ((frame / 3 + tx * 5) % 14), y, 3, 1) } }
                'J' -> { px(OUT, x, y + 4, T, 12); val cs = arrayOf("#d8373b", "#f4c430", "#2e9b6b", "#2457c5")
                    val sq = if (abs(pl.x + 5 - (tx * T + 8)) < 12 && pl.vy < -6) 2 else 0
                    for (k in 0 until 4) px(cs[k], x + 1 + k * 4 - (if (k == 3) 1 else 0), y + 5 + sq, 4, 10 - sq)
                    px("#fff1e8", x + 1, y + 5 + sq, 14, 1); px("#59000000", x + 1, y + 13, 14, 2) }
                'C' -> { val cr = crumble[ty * cols + tx] ?: 0; val sh = if (cr > 0 && frame % 4 < 2) 1 else 0
                    px(OUT, x + sh, y, T, T); px("#b07a45", x + 1 + sh, y + 1, 14, 14); px("#8b5a2b", x + 1 + sh, y + 5, 14, 1); px("#8b5a2b", x + 1 + sh, y + 10, 14, 1)
                    px("#e9c94a", x + 4 + sh, y - 3, 8, 5); px("#b89a2a", x + 6 + sh, y - 3, 1, 5)
                    if (cr > 18) px(OUT, x + 7 + sh, y + 2, 1, 12) }
                'E', 'e' -> { px("#4b5563", x, y, T, T); px("#9aa1ab", x, y, T, 4); val d = if (ch == 'E') 1 else -1
                    for (k in 0 until 3) { val ox = ((frame * d / 2 + k * 6) % 16 + 16) % 16; px("#f4c430", x + ox, y + 1, 2, 2) }
                    px("#2b2b33", x, y + 4, T, 1) }
            }
        }
    }

    private fun drawWaterTop() {
        if (!pl.inWater) return
        val tx = ((pl.x + 5) / T).toInt(); var ty = ((pl.y + 13) / T).toInt()
        while (ty > 0 && tileAt(tx, ty - 1) == 'w') ty--
        for (dx in -1..1) if (tileAt(tx + dx, ty) == 'w') { val x = X(tx + dx); val y = Y(ty); G.alpha(0.45f) { px("#3f8fcf", x, y + 2, T, (rows - ty) * T) } }
    }

    private fun drawWindFx() {
        for (w in winds) {
            val y0 = w.y0 - camY; val y1 = w.y1 - camY
            if (y1 < 0 || y0 > H) continue
            val act = windActive(w); val ph = (frame + w.phase) % 240
            if (act) for (i in 0 until 8) { val h = hash(i, w.phase); val yy = y0 + (h % (w.y1 - w.y0).toInt()); val xx = ((h shr 5) + frame * 4 * w.dir) % 220; val sx = if (w.dir > 0) xx - 20 else W - xx
                px("#ccffffff", sx, yy, 14, 1) }
            else if (ph > 200 && frame % 10 < 5) { txt(if (w.dir > 0) ">>>" else "<<<", if (w.dir > 0) 8f else W - 8f, y0 + 4, "#ffffff", SM, if (w.dir > 0) 0 else 2) }
        }
    }

    private fun drawZips() {
        for (z in zips) {
            G.line("#2b2b33", z.x0 - camX, z.y0 - camY, z.x1 - camX, z.y1 - camY, 1.2f)
            val px0 = (z.x0 - camX).roundToInt(); val py0 = (z.y0 - camY).roundToInt(); val px1 = (z.x1 - camX).roundToInt(); val py1 = (z.y1 - camY).roundToInt()
            px(OUT, px0 - 2, py0 - 2, 5, 60); px("#9aa1ab", px0 - 1, py0 - 1, 3, 58); px("#f4c430", px0 - 3, py0 - 4, 7, 4)
            px(OUT, px1 - 2, py1 - 2, 5, 60); px("#9aa1ab", px1 - 1, py1 - 1, 3, 58)
        }
        pl.zip?.let { z -> val hx = (pl.x + 5 - camX).roundToInt(); val hy = (pl.y - 2 - camY).roundToInt(); px(OUT, hx - 4, hy - 2, 9, 4); px("#d8373b", hx - 3, hy - 1, 7, 2) }
    }

    private fun drawMP(m: MP) {
        val x = (m.x - camX).roundToInt().toFloat(); val y = (m.y - camY).roundToInt().toFloat(); val w = m.w
        when (m.kind) {
            "train" -> { px(OUT, x - 1, y - 1, w + 2, 22); px("#2e6fd8", x, y, w, 20); px("#f5f5f5", x, y + 8, w, 3); for (i in 0 until 4) px("#cfe8ff", x + 4 + i * 15, y + 2, 10, 5); px("#f4c430", x + w - 4, y + 12, 3, 3) }
            "boat" -> { px(OUT, x - 1, y - 1, w + 2, 10); px("#d8373b", x, y, w, 4); px("#f5f0e6", x + 2, y + 4, w - 4, 4); px("#8b5a2b", x + w / 2 - 1, y - 14, 2, 14) }
            else -> { px(OUT, x - 1, y - 1, w + 2, 8); px("#c9ced6", x, y, w, 6); px("#6b7280", x, y + 5, w, 1); px("#6b7280", x + w / 2, y - 200, 1, 200) }
        }
    }

    private fun drawGoal() {
        val g = goal ?: return
        val x = (g.x - camX).roundToInt().toFloat(); val y = (g.y - camY).roundToInt().toFloat()
        if (g.kind == "deck") { px(OUT, x - 2, y + g.h - 8, g.w + 4, 10); px("#86c5ef", x, y + g.h - 6, g.w, 6); px("#f4c430", x + g.w / 2 - 2, y, 4, g.h - 8); if (frame % 20 < 10) px("#e24b4a", x + g.w / 2 - 2, y - 3, 4, 3); return }
        val door = if (g.kind == "mgate") "#c9a227" else "#2f7fc1"
        px(OUT, x - 3, y - 7, 38, 57); px("#6b3f1f", x - 2, y - 6, 36, 6); px("#f4c430", x + 4, y - 4, 24, 2)
        px("#6b3f1f", x - 2, y, 4, 48); px("#6b3f1f", x + 30, y, 4, 48); px(door, x + 2, y, 28, 48); px(OUT, x + 15, y, 2, 48)
        for (d in arrayOf(4 to 6, 4 to 22, 4 to 36)) { px("#86c5ef", x + d.first, y + d.second, 10, 10); px(door, x + d.first + 2, y + d.second + 2, 6, 6); px("#86c5ef", x + d.first + 18, y + d.second, 10, 10); px(door, x + d.first + 20, y + d.second + 2, 6, 6) }
        if (g.kind == "mgate") txt("CHIQISH", x - 4, y - 18, "#f4c430", SM)
    }

    private fun drawVeh() {
        if (veh != "damas") return
        val x = vehX.roundToInt().toFloat(); val b = 196f
        px(OUT, x - 1, b - 5, 44, 20); px("#f2f2f2", x, b - 4, 42, 16); px("#86c5ef", x + 24, b - 2, 14, 6); px("#86c5ef", x + 4, b - 2, 16, 6)
        px("#222222", x + 4, b + 11, 7, 5); px("#222222", x + 30, b + 11, 7, 5); px("#d8373b", x + 2, b - 11, 30, 7); px("#2b2b33", x + 12, b - 16, 12, 5); px("#f4c430", x + 4, b - 9, 26, 2)
    }

    private fun drawBoss() {
        val b = boss ?: return
        for (s in shots) { val x = (s.x - camX).roundToInt(); val y = (s.y - camY).roundToInt(); px(OUT, x - 3, y - 3, 7, 7); px("#f0c27a", x - 2, y - 2, 5, 5); px("#f08a24", x, y - 1) }
        if (b.dead > 40) return
        if (b.inv > 0 && frame % 6 < 3) return
        val c = G.c; c.save(); c.translate((b.x - camX).roundToInt().toFloat(), (b.y - camY).roundToInt().toFloat())
        val f = (b.t / 8) % 2
        G.drawParts(listOf(
            P(7, 31, 5, 9 - f, "#9aa1ab"), P(22, 31, 5, 8 + f, "#9aa1ab"), P(5, 38 - f, 9, 2, "#55556a"), P(20, 37 + f, 9, 3, "#55556a"),
            P(4, 10, 6, 4, "#3a2f5a"), P(24, 10, 6, 4, "#3a2f5a"),
            P(11, 0, 12, 12, "#e2a676") { px("#000000", 12, 4, 10, 2); px("#ffffff", 13, 4, 2, 1); px("#1c1410", 13, 8, 8, 2); px("#1c1410", 11, 0, 12, 2) },
            P(-2, 13, 38, 4, "#55556a") { px("#f0c27a", 0, 13, 34, 2) },
            P(0, 17, 34, 15, "#2b2b33") { px("#3a3a48", 2, 19, 30, 2); px("#f4c430", 13, 24, 8, 4); px("#b8860b", 16, 23, 2, 6) },
            P(12, 12, 10, 2, "#f4c430")), 0f)
        c.restore()
        for (i in 0 until 3) G.spr(Spr.HRT, W / 2 - 14 + i * 10, 22, Spr.pal('R' to if (i < b.hp) "#7b3fa0" else "#4a4a5a"))
        txt(L("ЖАДИНА-АКА", "OCHKO'Z AKA"), W / 2f, 13f, "#f4c430", SM, 1)
    }

    // shared decor used by levels
    fun choyxona(cx: Float, cy: Float) {
        px("#8b5a2b", cx - 6, cy - 12, 44, 4); px("#6b3f1f", cx - 4, cy - 8, 3, 8); px("#6b3f1f", cx + 32, cy - 8, 3, 8); px("#d8373b", cx - 4, cy - 16, 40, 4)
        Spr.person(cx + 4, cy - 32, "#2f7d4a", "#f2f2f2", scarf = true, beard = true)
        G.drawParts(listOf(P(cx + 20, cy - 24, 14, 8, "#8a8f99"), P(cx + 31, cy - 31, 6, 8, "#8a8f99") { px("#000000", cx + 34, cy - 29); px("#6b7280", cx + 31, cy - 34, 2, 3); px("#6b7280", cx + 35, cy - 34, 2, 3) },
            P(cx + 21, cy - 16, 2, 4, "#6b7280"), P(cx + 31, cy - 16, 2, 4, "#6b7280")), 0f)
        txt("CHOYXONA", cx - 6, cy - 48, "#f4c430", SM)
    }
    fun cat(x: Float, y: Float, sleep: Boolean) {
        G.drawParts(listOf(P(x, y - 5, 10, 5, "#f08a24") { px("#c25e10", x + 2, y - 4, 6, 1) }, P(x + 7, y - 8, 5, 4, "#f08a24") { px("#000000", x + 9, y - 6, 2, 1); px("#f08a24", x + 7, y - 9); px("#f08a24", x + 11, y - 9) }, P(x - 4, y - 3, 4, 2, "#f08a24")), 0f)
        if (sleep && frame % 60 < 30) txt("z", x + 12, y - 16, "#fff1e8", SM)
    }
    fun waterfall(wx: Float, wy: Float, rws: Int) {
        G.alpha(0.7f) { px("#5fb8e8", wx, wy, 30, rws * T); for (i in 0 until 6) px("#e8f6ff", wx + 3 + i * 5, wy + ((frame * 2 + i * 23) % (rws * T)), 1, 10) }
        px("#e8f6ff", wx - 2, wy + rws * T - 3, 34, 3)
    }

    // ================= menus & taps =================
    fun show(s: String) { state = s; Menu.layout(this) }

    fun tap(gx: Float, gy: Float) {
        if (state == "play") { if (gx > W - 26 && gy < 20) show("pause"); return }
        for (b in btns.toList()) if (gx >= b.x && gx <= b.x + b.w && gy >= b.y && gy <= b.y + b.h) { Sfx.play(500f, 0.04f); b.act(); return }
    }

    fun back(): Boolean = when (state) {
        "play" -> { show("pause"); true }
        "title" -> false
        else -> { show("title"); true }
    }

    fun changeLang(l: String) { lang = l; prefs.edit().putString("lang", l).apply(); Menu.layout(this) }
    fun goIntro(i: Int) { introI = i; show("intro") }
    fun play(i: Int) = startLevel(i)
    fun winUnlock() { if (unlock < LVI + 1) { unlock = min(levels.size - 1, LVI + 1); prefs.edit().putInt("unlock", unlock).apply() } }
    fun allEggs() = levels.indices.sumOf { savedEggs(it).size }

    init { loadLevel(0); Menu.layout(this) }
}
