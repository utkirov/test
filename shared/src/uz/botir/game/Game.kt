package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
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
    var zip: Zip? = null; var zipT = 0f; var zipCd = 0; var inWater = false; var sink = 0f
    var wasOn = true; var landT = 0; var anim = "idle"; var animT = 0
}

class Enemy(val t: Char, x: Float, y: Float, w: Float, h: Float, var min: Float, var max: Float) : Body(x, y, w, h) {
    val y0 = y; var dead = 0; var f = (G.rnd() * 60).toInt(); val fly = t == 'b' || t == 'h'
}

class Item(val t: Char, var x: Float, var y: Float, val egg: String? = null) {
    var w = 10f; var h = 10f; val ph = (G.rnd() * 6).toFloat(); var fall = false; var vy = 0f; var drift = 0.6f
}

class MP(var x: Float, var y: Float, val w: Float, val ax: Char, val a0: Float, val a1: Float, val sp: Float, val tag: String, val kind: String) {
    var dir = 1; var dx = 0f; var dy = 0f; val h = 6f
}

class Zone(val id: String, val n: Array<String>, val m: Array<String>, val x: Float, val y: Float, val w: Float, val h: Float,
           val cond: String, val eff: String? = null, val et: Char = ' ', val cnt: Int = 0)

class Npc(val tx: Int, val ty: Int, val body: String, val hat: String, val atlas: Boolean = false, val scarf: Boolean = false, val beard: Boolean = false,
          val glasses: Boolean = false, val wave: Boolean = false, val note: Int = 0, val dancer: Boolean = false, val arm: Int = 0)

class Zip(val x0: Float, val y0: Float, val x1: Float, val y1: Float)
class Wind(val y0: Float, val y1: Float, val dir: Int, val phase: Int, val storm: Boolean = false)
class Thrower(val x: Float, val y: Float, val trig: Float, val egg: String) { var done = false }
class Goal(val x: Float, val y: Float, val w: Float, val h: Float, val kind: String)
class Boss(var x: Float, var y: Float, val kind: Int) { val w = 34f; val h = 40f; var vx = -0.8f; var vy = 0f; var hp = 3; var inv = 0; var t = 0; var dead = 0 }
class Shot(var x: Float, var y: Float, var vx: Float, var vy: Float, val k: Char = 'p') { var dead = false; var life = 0 }
class Fx(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Int, val c: String, val kind: Int = 0, val text: String = "") { val life0 = life }
class Toast(val s: String, var f: Int)
class Btn(val x: Float, val y: Float, val w: Float, val h: Float, val label: String, val sub: String = "", val style: Int = 0, val tag: Int = -1, val act: () -> Unit)
class Theme(val top: String, val top2: String, val body: String, val dot: String, val wall: String, val mortar: String, val plank: String, val plank2: String, val awning: Boolean = false)
class MiniDef(val k: Char, val x0: Float, val x1: Float, val gy: Float, val trigX: Float, val trigY: Float)

class LevelDef(val n: Array<String>, val intro: Array<String>, val mech: Array<String>, val bg: Int, val th: Theme,
               val vertical: Boolean = false, val boss: Boolean = false,
               val build: Game.(B) -> Unit, val back: (Game.() -> Unit)? = null, val front: (Game.() -> Unit)? = null, val over: (Game.() -> Unit)? = null,
               val sh: Array<String> = n, val song: Int = Music.MAHALLA, val cutIn: String? = null, val cutOut: String? = null, val bossKind: Int = 0)

/** Level builder helpers (tile coordinates). */
class B(val g: Game) {
    fun G(a: Int, b: Int, top: Int, bot: Int = g.rows - 1) { for (x in a..b) for (y in top..bot) g.map[y][x] = '#' }
    fun P(a: Int, b: Int, y: Int, c: Char = '=') { for (x in a..b) g.map[y][x] = c }
    fun WB(a: Int, b: Int, y0: Int, y1: Int) { for (x in a..b) for (y in y0..y1) g.map[y][x] = 'B' }
    fun WA(a: Int, b: Int, y0: Int, y1: Int) { for (x in a..b) for (y in y0..y1) g.map[y][x] = 'w' }
    fun I(t: Char, x: Int, y: Int, egg: String? = null) { g.items.add(Item(t, x * T + 3f, y * T + 3f, egg)) }
    fun N(a: Int, b: Int, y: Int) { for (x in a..b) I('n', x, y) }
    fun E(t: Char, x: Int, y: Int, range: Int = 4) {
        val h = when (t) { 'g' -> 12f; 'b', 'h', 'z' -> 8f; else -> 20f }
        val sp = when (t) { 'g' -> 0.5f; 'k' -> 1.1f; 's' -> 0.7f; 'c' -> 0.9f; 'b' -> 0.8f; 'z' -> 1.4f; else -> 0.7f }
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
    fun STORM(phase: Int) { g.winds.add(Wind(0f, g.rows * T.toFloat(), -1, phase, storm = true)) }
    /** Mini-boss arena between tiles x0..x1 standing on ground row gy. Vertical arenas trigger by height (trigRow). */
    fun MINI(k: Char, x0: Int, x1: Int, gy: Int, trigRow: Int = -1) {
        g.miniDef = MiniDef(k, x0 * T.toFloat(), (x1 + 1) * T.toFloat(), gy * T.toFloat(), (x0 + 1) * T.toFloat(), if (trigRow >= 0) trigRow * T.toFloat() else -1f)
    }
}

class Game {
    // ================= persistence =================
    private val st get() = Plat.store
    private fun gI(k: String, d: Int) = st.getInt(k, d)
    private fun pI(k: String, v: Int) = st.putInt(k, v)
    private fun gB(k: String, d: Boolean) = st.getBool(k, d)
    private fun pB(k: String, v: Boolean) = st.putBool(k, v)
    private fun gS(k: String): Set<String> = st.getSet(k)
    private fun addS(k: String, v: String) { val s = HashSet(gS(k)); if (s.add(v)) st.putSet(k, s) }

    var lang = st.getString("lang", Plat.defaultLang)
    var unlock = gI("unlock", 0)
    var wallet = gI("wallet", 0)
    var skinId = st.getString("skin", "street")
    var best = gI("best", 0)
    var musicOn = gB("music", true); var sfxOn = gB("sfx", true); var vibOn = gB("vib", true)
    var btnSize = gI("btnSize", 1); var lefty = gB("lefty", false); var seasonSet = gI("season", 0)
    var settingsVer = 0
    val season: Int get() = Season.resolve(seasonSet)

    fun savedEggs(i: Int): Set<String> = gS("eggs$i")
    fun stars(i: Int) = gI("stars$i", 0)
    fun owned(id: String) = gS("owned").contains(id)
    fun hasUp(id: String) = gS("ups").contains(id)
    fun skin(): Skin = Skins.get(skinId)
    fun skinAvailable(s: Skin) = s.price == 0 && (s.season == 0 || s.season == season) || owned(s.id)
    fun L(ru: String, uz: String) = if (lang == "uz") uz else ru

    fun vib(ms: Long) { if (vibOn) try { Plat.vibrate(ms) } catch (_: Throwable) { } }

    // ================= level data =================
    var LVI = 0
    lateinit var LV: LevelDef
    var map: Array<CharArray> = arrayOf(CharArray(1))
    var rows = 17; var cols = 1
    val items = ArrayList<Item>(); val enemies = ArrayList<Enemy>(); val zones = ArrayList<Zone>(); val mps = ArrayList<MP>()
    val npcs = ArrayList<Npc>(); val throwers = ArrayList<Thrower>(); val zips = ArrayList<Zip>(); val winds = ArrayList<Wind>()
    val crumble = HashMap<Int, Int>()
    var goal: Goal? = null
    var arenaX0 = -1f; var arenaX1 = -1f; var bossGround = 14f * T
    var boss: Boss? = null; val shots = ArrayList<Shot>()
    var miniDef: MiniDef? = null; var mini: Mini? = null; var miniDone = false
    val fx = ArrayList<Fx>(); val toasts = ArrayList<Toast>(); val found = HashSet<String>()
    var veh: String? = null; var vehX = 0f
    var coins = 0; var total = 0; var frame = 0
    var msg: String? = null; var msgF = 0
    var mosaicA = true
    // per-run counters
    var idle = 0; var still = 0; var dance = 0; var lowg = 0; var bossHits = 0; var miniHits = 0; var bounced = 0
    val jc = HashMap<String, Int>(); val stomps = HashMap<Char, Int>(); val tips = HashSet<String>()
    var camX = 0f; var camY = 0f
    lateinit var pl: Player
    // endless
    var endless = false; var genX = 0; var genTop = 14; var runStart = 0f
    private var greeted = false

    // ================= input =================
    var kL = false; var kR = false; var kJ = false
    fun jumpPressed() { if (state == "play") pl.buf = 7 }

    // ================= UI state =================
    var state = "title"; var introI = 0
    var cutId = ""; var cutLine = 0; var cutAfter = ""
    var lastStars = 0; var lastEarned = 0; var newBest = false
    var shopTab = 0; var menuMsg = ""; var menuMsgF = 0
    val btns = ArrayList<Btn>()
    val levels: List<LevelDef> = buildLevels()
    val endlessDef: LevelDef = buildEndlessDef()
    val eggTotals: List<Int> by lazy { levels.indices.map { i -> countZones(i) } }

    private fun countZones(i: Int): Int { val keep = LVI; loadLevel(i); val n = zones.size; loadLevel(keep); return n }

    fun X(t: Int) = (t * T - camX).roundToInt().toFloat()
    fun Y(t: Int) = (t * T - camY).roundToInt().toFloat()

    // ================= tiles & physics =================
    fun tileAt(tx: Int, ty: Int): Char = if (tx < 0 || tx >= cols) '#' else if (ty < 0 || ty >= rows) '.' else map[ty][tx]
    fun solidC(c: Char) = c == '#' || c == 'B' || c == 'J' || c == 'C' || c == 'E' || c == 'e' || c == 'Q' || (c == 'M' && mosaicA) || (c == 'm' && !mosaicA)
    fun solidAt(x: Float, y: Float) = solidC(tAt(x, y))
    fun tAt(x: Float, y: Float) = tileAt(floor((x / T).toDouble()).toInt(), floor((y / T).toDouble()).toInt())

    fun moveX(o: Body): Boolean {
        o.x += o.vx; var hit = false
        val ys = floatArrayOf(o.y + 1, o.y + o.h / 2, o.y + o.h - 1)
        if (o.vx > 0) { if (ys.any { solidAt(o.x + o.w, it) }) { o.x = floor(((o.x + o.w) / T).toDouble()).toFloat() * T - o.w - 0.01f; hit = true } }
        else if (o.vx < 0) { if (ys.any { solidAt(o.x, it) }) { o.x = (floor((o.x / T).toDouble()).toFloat() + 1) * T; hit = true } }
        if (hit) o.vx = 0f
        return hit
    }

    fun moveY(o: Body) {
        val pb = o.y + o.h; o.y += o.vy; o.on = false; o.onPipe = false; o.land = '.'
        val xs = floatArrayOf(o.x + 1, o.x + o.w - 1)
        if (o.vy > 0) {
            val ty = floor(((o.y + o.h) / T).toDouble()).toInt(); val top = ty * T.toFloat()
            for (x in xs) {
                val tx = floor((x / T).toDouble()).toInt(); val c = tileAt(tx, ty)
                if (solidC(c) || ((c == '=' || c == 'p') && pb <= top + 0.5f)) {
                    o.y = top - o.h; o.vy = 0f; o.on = true; if (c == 'p') o.onPipe = true
                    if (o.land == '.' || c == 'J' || c == 'Q') { o.land = c; o.landTx = tx; o.landTy = ty }
                }
            }
        } else if (o.vy < 0) {
            if (xs.any { solidAt(it, o.y) }) { o.y = (floor((o.y / T).toDouble()).toFloat() + 1) * T; o.vy = 0f }
        }
    }

    fun ov(ax: Float, ay: Float, aw: Float, ah: Float, bx: Float, by: Float, bw: Float, bh: Float) = ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by
    fun ovP(z: Zone) = ov(pl.x, pl.y, pl.w, pl.h, z.x, z.y, z.w, z.h)

    // ================= flow =================
    private fun clearWorld() {
        items.clear(); enemies.clear(); zones.clear(); mps.clear(); npcs.clear(); throwers.clear(); zips.clear(); winds.clear(); crumble.clear()
        goal = null; arenaX0 = -1f; arenaX1 = -1f; bossGround = 14f * T; boss = null; shots.clear(); fx.clear(); toasts.clear(); found.clear(); veh = null
        miniDef = null; mini = null; miniDone = false; mosaicA = true; timers.clear()
    }

    private fun spawnPlayer() {
        var sy = 0f
        for (y in 0 until rows) if (solidC(tileAt(2, y))) { sy = y * T - 26f; break }
        pl = Player(2f * T, sy); pl.hp = 3 + (if (hasUp("heart")) 1 else 0)
        idle = 0; still = 0; dance = 0; lowg = 0; bossHits = 0; miniHits = 0; bounced = 0; jc.clear(); stomps.clear(); tips.clear()
        camX = 0f; camY = max(0f, min(rows * T - H.toFloat(), pl.y - H * 0.55f))
    }

    fun loadLevel(i: Int) {
        endless = false; LVI = i; LV = levels[i]; clearWorld()
        LV.build(this, B(this))
        total = items.count { it.t == 'n' }; coins = 0; msg = null
        spawnPlayer()
    }

    fun newMap(c: Int, r: Int) { cols = c; rows = r; map = Array(r) { CharArray(c) { '.' } } }

    private fun startLevel(i: Int) {
        Sfx.fx("click"); loadLevel(i); state = "play"; btns.clear(); frame = 0
        Music.play(LV.song)
        tip(L(levels[i].mech[0], levels[i].mech[1]))
        if (!greeted && season != Season.NONE) { greeted = true; tip(Season.greeting(season, this)) }
    }

    fun startEndless() {
        Sfx.fx("click"); endless = true; LV = endlessDef; LVI = -1; clearWorld()
        newMap(400, 17); genX = 0; genTop = 14
        val b = B(this); b.G(0, 12, 14); genX = 13; genEndless(b, 380)
        total = 0; coins = 0; msg = null; spawnPlayer(); runStart = pl.x
        state = "play"; btns.clear(); frame = 0; Music.play(Music.ENDLESS)
        tip(L("Бесконечный забег! Ботир бежит сам — только прыгай. Чем дальше, тем быстрее.", "Cheksiz yugurish! Botir o'zi yuguradi — faqat sakra. Qancha uzoq bo'lsa, shuncha tez."))
    }

    fun tip(s: String) { toasts.add(Toast(s, 260)) }
    fun toast(s: String) { toasts.add(Toast(s, 230)) }

    fun egg(z: Zone?) {
        if (z == null || found.contains(z.id) || endless) return
        found.add(z.id); addS("eggs$LVI", z.id)
        toast("* " + L(z.m[0], z.m[1])); Sfx.fx("egg"); sparkle(pl.x + 5, pl.y, 8); burst(pl.x + 5, pl.y, "#f4c430", 14); vib(25)
        when (z.eff) {
            "hp" -> pl.hp = min(5, pl.hp + 1)
            "lowg" -> lowg = 600
            "dance" -> { dance = 120; Sfx.tune(intArrayOf(587, 659, 698, 659, 587, 523, 587), 140, 1) }
            "pow" -> pl.pow = powTime()
            "carry" -> pl.carry = 60
            "ring" -> { Sfx.tune(intArrayOf(880, 880, 0, 880, 880), 120); vib(300) }
            "chime" -> Sfx.tune(intArrayOf(659, 523, 587, 392), 220, 1)
            else -> if (z.eff != null && z.eff.startsWith("veh:")) { veh = z.eff.substring(4); vehX = -70f }
        }
    }

    fun powTime() = if (hasUp("doppi")) 900 else 600

    fun hurt() {
        if (pl.inv > 0 || pl.pow > 0 || pl.carry > 0) return
        pl.hp--; pl.inv = 100; pl.vy = -3.5f; pl.vx = -pl.face * 2f; pl.zip = null; pl.sink = 0f; Sfx.fx("hurt"); vib(70)
        if (mini != null) miniHits++
        if (boss != null) bossHits++
        if (pl.hp <= 0) { state = "dead"; delayed(30) { if (endless) endRun() else show("over") } }
    }
    private fun respawn() { pl.inv = 0; pl.pow = 0; hurt(); if (pl.hp > 0) { pl.x = pl.safeX; pl.y = pl.safeY; pl.vx = 0f; pl.vy = 0f; pl.mp = null; pl.zip = null } }
    fun burst(x: Float, y: Float, c: String, n: Int = 8) { repeat(n) { fx.add(Fx(x, y, ((G.rnd() - .5) * 2.5).toFloat(), (-G.rnd() * 2.5).toFloat(), 25, c)) } }

    private val timers = ArrayList<Pair<Int, () -> Unit>>()
    fun delayed(frames: Int, f: () -> Unit) { timers.add(Pair(frame + frames, f)) }

    // ================= update =================
    fun update() {
        frame++
        if (timers.isNotEmpty()) { val due = timers.filter { it.first <= frame }; timers.removeAll(due.toSet()); due.forEach { it.second() } }
        if (state != "play") {
            stepFx()
            if (state == "won" || state == "dead") animatePlayer(0f)
            if (state in MENU_BG) { camX += 0.3f; if (camX > cols * T - W) camX = 0f }
            return
        }
        val p = pl
        val vyPrev = p.vy
        var dir = (if (kR) 1 else 0) - (if (kL) 1 else 0)
        if (endless) dir = 1
        // mosaic rhythm
        if (frame % 150 == 0) { mosaicA = !mosaicA; if (map.any { r -> r.any { it == 'M' || it == 'm' } }) Sfx.play(330f, 0.06f, 1, 0.12f, 1.2f) }
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
        val wasWater = p.inWater
        p.inWater = tAt(cx, p.y + p.h * 0.6f) == 'w'
        if (p.inWater && !wasWater) { Sfx.fx("splash"); repeat(10) { fx.add(Fx(cx, p.y + p.h * 0.5f, ((G.rnd() - .5) * 3).toFloat(), (-1.5 - G.rnd() * 2).toFloat(), 26, "#cfeeff")) } }
        if (p.inWater && frame % 40 == 0) fx.add(Fx(cx + 3, p.y + 4, 0f, -0.5f, 30, "#e8f6ff", 2))
        val maxV = if (endless) 1.75f + min(1.1f, (p.x - runStart) / T / 1800f) else if (p.sink > 0) 0.9f else 1.75f

        if (p.zip != null) {
            pb = p.y + p.h; updateZip()
        } else if (p.inWater) {
            if (!found.contains("anhor")) egg(zones.find { it.cond == "water" })
            if (tips.add("swim")) tip(L("Плыви! Жми прыжок, чтобы грести вверх.", "Suz! Yuqoriga suzish uchun sakrashni bos."))
            if (dir != 0) { p.vx += dir * 0.18f; p.vx = p.vx.coerceIn(-1.1f, 1.1f); p.face = dir } else p.vx *= 0.9f
            if (p.buf > 0) {
                val nearTop = tAt(cx, p.y + 2) != 'w'
                p.vy = if (nearTop) -5.8f else -2.4f; p.buf = 0; Sfx.fx(if (nearTop) "jump" else "bubble")
                burst(cx, p.y + 4, "#cfeeff", 3)
            }
            p.buf--
            p.vy = min(1.3f, p.vy + 0.09f)
            pb = p.y + p.h
            val sv = p.vx; p.vx = sv + 0.25f; val hit = moveX(p); p.vx = if (hit) 0f else sv; moveY(p); p.dist += abs(p.vx)
        } else {
            if (dir != 0 && dance <= 0) { p.vx += dir * 0.28f; p.vx = p.vx.coerceIn(-maxV, maxV); p.face = dir }
            else { p.vx *= if (p.on) 0.72f else 0.92f; if (abs(p.vx) < 0.05f) p.vx = 0f }
            if (p.on) { p.coy = 6; p.jumps = 0 } else p.coy--
            if (p.buf > 0) {
                if (p.coy > 0) {
                    p.vy = (if (lowg > 0) -4.6f else -6.1f) + p.sink * 0.1f; p.coy = 0; p.buf = 0; p.jumps = 1; p.mp = null; Sfx.fx("jump"); if (p.sink == 0f) dust(p.x + 5, p.y + p.h, 2)
                    if (p.sink > 0) { burst(p.x + 5, p.y + p.h, "#e2c68e", 6); p.sink = max(0f, p.sink - 5f) }
                    for (z in zones) if (z.cond == "jump" && ovP(z)) { val n = (jc[z.id] ?: 0) + 1; jc[z.id] = n; if (n >= 3) egg(z) }
                } else if (p.pow > 0 && p.jumps < 2) { p.vy = -5.6f; p.jumps = 2; p.buf = 0; Sfx.fx("djump"); burst(p.x + 5, p.y + 26, "#f4c430", 6) }
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
        if (p.land != 'Q' || !p.on) p.sink = max(0f, p.sink - 1.5f)
        // boss & mini arenas
        if (arenaX0 > 0 && p.x > arenaX0 + T && boss == null) spawnBoss()
        if (boss != null && p.x < arenaX0) p.x = arenaX0
        miniDef?.let { d ->
            if (mini == null && !miniDone && ((d.trigY < 0 && p.x > d.trigX) || (d.trigY >= 0 && p.y < d.trigY))) spawnMini(d)
            if (mini != null && !miniDone) { if (p.x < d.x0) p.x = d.x0; if (p.x + p.w > d.x1) { p.x = d.x1 - p.w; if (p.vx > 0) p.vx = 0f } }
        }
        // moving platforms landing
        if (p.vy >= 0 && !p.on && p.zip == null) {
            p.mp = null
            for (m in mps) if (p.x + p.w > m.x && p.x < m.x + m.w && pb <= m.y + max(0f, m.dy) + 1.5f && p.y + p.h >= m.y) { p.y = m.y - p.h; p.vy = 0f; p.on = true; p.mp = m }
        } else if (p.vy < 0) p.mp = null
        if (p.on && p.mp == null && p.land != 'C' && p.land != 'Q' && p.land != 'M' && p.land != 'm' && solidAt(p.x, p.y + p.h + 2) && solidAt(p.x + p.w, p.y + p.h + 2)) { p.safeX = p.x; p.safeY = p.y }
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
                "ride" -> if (p.mp != null && p.mp!!.tag.startsWith(z.id) && ride) egg(z)
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
        if (mini != null) updMini()
        updShots()
        stepFx()
        animatePlayer(vyPrev)
        if (endless) { if (p.x > (cols - 40) * T) growEndless() }
        else if (!LV.boss && (miniDef == null || miniDone)) goal?.let { g -> if (ov(p.x, p.y, p.w, p.h, g.x + 12, g.y, g.w - 12, g.h)) levelWon() }
        var tx = p.x + p.w / 2 - W / 2 + p.face * 24
        if (endless) tx = p.x - 50
        if (boss != null) tx = tx.coerceIn(arenaX0, arenaX1 - W)
        val md = miniDef
        if (mini != null && md != null && md.trigY < 0 && md.x1 - md.x0 >= W) tx = tx.coerceIn(md.x0, md.x1 - W)
        camX += (tx - camX) * (if (endless) 0.25f else 0.1f); camX = camX.coerceIn(0f, max(0f, cols * T - W.toFloat()))
        val ty = p.y - H * 0.55f; camY += (ty - camY) * 0.12f; camY = camY.coerceIn(0f, max(0f, rows * T - H.toFloat()))
    }

    fun levelWon() {
        if (state != "play") return
        state = "won"; Sfx.fx("win"); vib(40); sparkle(pl.x + 5, pl.y + 5, 12)
        lastEarned = coins; wallet += coins; pI("wallet", wallet)
        var st = 1
        if (coins >= total) st = st or 2
        val eggsAll = HashSet(savedEggs(LVI)).apply { addAll(found) }
        if (eggsAll.size >= zones.size) st = st or 4
        lastStars = st
        pI("stars$LVI", stars(LVI) or st)
        if (unlock < LVI + 1) { unlock = min(levels.size - 1, LVI + 1); pI("unlock", unlock) }
        if (LVI == levels.size - 1) pB("allDone", true)
        delayed(48) { val co = LV.cutOut; if (co != null) startCut(co, "win") else show("win") }
    }

    private fun endRun() {
        val m = ((pl.x - runStart) / T).roundToInt().coerceAtLeast(0)
        newBest = m > best; if (newBest) { best = m; pI("best", best) }
        lastEarned = coins; wallet += coins; pI("wallet", wallet)
        show("endover")
    }

    private fun afterLanding() {
        val p = pl
        if (!p.on) return
        when (p.land) {
            'J' -> {
                p.vy = -9.4f; p.on = false; p.jumps = 0; bounced = 30; Sfx.fx("spring"); burst(p.x + 5, p.y + p.h, "#f4c430", 8); vib(15)
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
            'Q' -> {
                p.sink = min(16f, p.sink + 0.3f); p.vx *= 0.8f
                if (frame % 6 == 0) burst(p.x + 5, p.y + p.h, "#e2c68e", 1)
                if (tips.add("sand")) tip(L("Зыбучий песок! Прыгай, чтобы не утонуть.", "Botqoq qum! Botib ketmaslik uchun sakra."))
                if (p.sink >= 16f) { p.sink = 0f; respawn() }
            }
            'M', 'm' -> if (tips.add("mosaic")) tip(L("Мозаика мигает и исчезает в ритме музыки!", "Mozaika musiqa ritmida yonib-o'chadi!"))
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
                if (e.value >= 36) { map[ty][tx] = '.'; burst(tx * T + 8f, ty * T + 8f, "#8b5a2b", 10); Sfx.fx("crack"); e.setValue(-300) }
            } else {
                e.setValue(e.value + 1)
                if (e.value >= 0) {
                    if (!ov(pl.x, pl.y, pl.w, pl.h, tx * T.toFloat(), ty * T.toFloat(), T.toFloat(), T.toFloat())) { map[ty][tx] = 'C'; restore.add(k) } else e.setValue(-1)
                }
            }
        }
        restore.forEach { crumble.remove(it) }
    }

    fun windActive(w: Wind) = if (w.storm) ((frame + w.phase) % 420) < 170 else ((frame + w.phase) % 240) < 110
    fun stormSoon(w: Wind) = w.storm && ((frame + w.phase) % 420) > 360
    private fun updWind() {
        val p = pl
        for (w in winds) {
            if (!windActive(w)) continue
            val cy = p.y + p.h / 2
            if (cy > w.y0 && cy < w.y1 && p.zip == null && !p.inWater) {
                if (w.storm) {
                    val sx = if (w.dir < 0) p.x + p.w + 6 else p.x - 6
                    if (solidAt(sx, p.y + p.h / 2) || solidAt(sx, p.y + 4)) continue   // sheltered behind a wall
                    if (frame % 420 == 0) Sfx.fx("storm")
                    if (tips.add("storm")) tip(L("Песчаная буря! Прячься за стенами, пока она не утихнет.", "Qum bo'roni! Bo'ron tingguncha devor ortiga yashirin."))
                } else if (tips.add("wind")) tip(L("Ветер на высоте! Держись против порывов.", "Balandda shamol! Shamolga qarshi tur."))
                val sv = p.vx; p.vx = w.dir * (if (w.storm) 0.95f else 0.85f); moveX(p); p.vx = sv
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
                p.zip = z; p.zipT = t; p.vx = 0f; p.vy = 0f; Sfx.fx("zip")
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
            if (endless && e.x < camX - 60) { dead.add(e); continue }
            e.f++
            if (e.fly) { e.x += e.vx; e.y = e.y0 + sin(e.f / 18f) * 10f; if (e.x < e.min || e.x > e.max) e.vx = -e.vx }
            else {
                e.vy = min(6f, e.vy + 0.32f); val sv = e.vx; if (moveX(e)) e.vx = -sv; moveY(e)
                if (e.on) { val ax = if (e.vx > 0) e.x + e.w + 1 else e.x - 1; val c = tAt(ax, e.y + e.h + 2); if (!solidC(c) && c != '=' && c != 'p') e.vx = -e.vx }
                if ((e.x < e.min && e.vx < 0) || (e.x > e.max && e.vx > 0)) e.vx = -e.vx
                if (e.y > rows * T) { dead.add(e); continue }
            }
            if (ov(p.x, p.y, p.w, p.h, e.x, e.y, e.w, e.h)) {
                if (e.t == 'h') { e.dead = 1; val z = zones.find { it.cond == "humo" }; if (z != null) egg(z) else p.carry = 60; continue }
                if (p.pow > 0 || p.carry > 0 || (p.vy > 0 && p.y + p.h - p.vy <= e.y + 6)) {
                    e.dead = 1; p.vy = if (kJ) -6f else -4f; Sfx.fx("stomp"); if (e.t == 'g') Sfx.fx("honk"); popText(e.x + 6, e.y - 4, "+", "#ffffff"); burst(e.x + 6, e.y + 4, "#ffffff", 10); vib(20)
                    if (endless) coins++
                    stomps[e.t] = (stomps[e.t] ?: 0) + 1
                    for (z in zones) if (z.cond == "stomp" && z.et == e.t && (stomps[e.t] ?: 0) >= z.cnt) egg(z)
                } else hurt()
            }
        }
        enemies.removeAll(dead.toSet())
    }

    private fun updItems() {
        val p = pl
        val magnet = hasUp("magnet")
        for (i in items) {
            if (i.fall) {
                i.vy = min(5f, i.vy + 0.25f); i.y += i.vy; if (i.t != 'q') i.x += i.drift
                if (i.vy > 0 && solidAt(i.x + 5, i.y + i.h)) { i.y = floor(((i.y + i.h) / T).toDouble()).toFloat() * T - i.h; i.fall = false }
            } else if (magnet && i.t == 'n') {
                val dx = p.x + 5 - (i.x + 5); val dy = p.y + 13 - (i.y + 5)
                if (dx * dx + dy * dy < 46f * 46f) { val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f); i.x += dx / d * 2.4f; i.y += dy / d * 2.4f }
            }
        }
        val rm = ArrayList<Item>()
        for (i in items) {
            if (ov(p.x, p.y, p.w, p.h, i.x, i.y, i.w, i.h)) {
                when (i.t) {
                    'n' -> { coins++; Sfx.fx("coin"); sparkle(i.x + 5, i.y + 5, 3, "#ffe9a8"); popText(i.x + 5, i.y - 2, "+1") }
                    's' -> { p.hp = min(5, p.hp + 1); Sfx.fx("heal"); popText(i.x + 5, i.y - 2, "+1", "#ff8a8a"); burst(i.x + 5, i.y + 5, "#e8b25a", 8) }
                    'd' -> { p.pow = powTime(); Sfx.fx("power"); sparkle(i.x + 5, i.y + 5, 10); msg = L("ДУППИ-СИЛА!", "DO'PPI KUCHI!"); msgF = 90; burst(i.x + 5, i.y + 5, "#f4c430", 14) }
                    'q' -> levelWon()
                    else -> burst(i.x + 5, i.y + 5, "#f4c430", 10)
                }
                i.egg?.let { id -> egg(zones.find { it.id == id }) }
                rm.add(i)
            } else if (i.y > rows * T + 40 || (endless && i.x < camX - 40)) rm.add(i)
        }
        items.removeAll(rm.toSet())
    }

    fun stepFx() {
        for (q in fx) {
            q.x += q.vx; q.y += q.vy; q.life--
            when (q.kind) { 1 -> { q.vx *= 0.9f; q.vy *= 0.9f }; 2 -> q.vy += 0.05f; 3 -> q.vy *= 0.94f; else -> q.vy += 0.12f }
        }
        fx.removeAll { it.life <= 0 }
    }
    /** Soft dust puffs at a point. */
    fun dust(x: Float, y: Float, n: Int = 3, dir: Float = 0f) { repeat(n) { fx.add(Fx(x + ((G.rnd() - .5) * 6).toFloat(), y - 2, (dir + (G.rnd() - .5) * 1.2).toFloat(), (-G.rnd() * 0.6).toFloat(), 22, "#e9dcc4", 1)) } }
    fun sparkle(x: Float, y: Float, n: Int = 4, c: String = "#fff6c8") { repeat(n) { fx.add(Fx(x + ((G.rnd() - .5) * 10).toFloat(), y + ((G.rnd() - .5) * 10).toFloat(), 0f, -0.3f, 18 + (G.rnd() * 10).toInt(), c, 2)) } }
    fun popText(x: Float, y: Float, s: String, c: String = "#f4c430") { fx.add(Fx(x, y, 0f, -1.2f, 40, c, 3, s)) }
    private fun drawFx() {
        for (q in fx) {
            val x = (q.x - camX).roundToInt(); val y = (q.y - camY).roundToInt(); val k = q.life.toFloat() / q.life0
            when (q.kind) {
                1 -> { val r = 1f + (1f - k) * 3f; G.alpha(k * 0.8f) { G.circle(q.c, x, y, r) } }
                2 -> { val on = (q.life / 3) % 2 == 0; if (on) { px(q.c, x - 1, y, 3, 1); px(q.c, x, y - 1, 1, 3) } else px(q.c, x, y, 1, 1) }
                3 -> G.alpha(k.coerceAtMost(1f) * 1f) { txt(q.text, x, y, q.c, SM, 1) }
                else -> px(q.c, x, y, 2, 2)
            }
        }
    }

    // ================= boss (chapter finales) =================
    private fun spawnBoss() {
        boss = Boss(arenaX1 - 4f * T, bossGround - 40, LV.bossKind)
        Music.play(Music.BOSS)
        if (LV.bossKind == 0) toast(L("Жадина-ака: «Казан мой! Ха-ха!»", "Ochko'z aka: «Qozon meniki! Ha-ha!»"))
        else toast(L("Зикна-ака: «Кафгир мой! Ни монетки не отдам!»", "Ziqna aka: «Kapgir meniki! Bir tiyin ham bermayman!»"))
        Sfx.fx("roar")
    }

    private fun updBoss() {
        val b = boss!!; val p = pl
        if (b.dead > 0) { b.dead++; if (b.dead == 60) { val it = Item('q', b.x + 10, b.y); it.w = 14f; it.fall = true; it.vy = -4f; items.add(it) }; return }
        b.t++; if (b.inv > 0) b.inv--
        val spd = 0.8f + (3 - b.hp) * 0.35f + b.kind * 0.2f
        b.vx = (if (b.vx >= 0) 1 else -1) * spd; b.x += b.vx
        if (b.x < arenaX0 + 8) { b.x = arenaX0 + 8; b.vx = spd }
        if (b.x > arenaX1 - b.w - 8) { b.x = arenaX1 - b.w - 8; b.vx = -spd }
        b.vy += 0.32f; b.y += b.vy
        if (b.y > bossGround - b.h) { b.y = bossGround - b.h; if (b.vy > 3) { burst(b.x + 17, b.y + 40, "#c9ced6", 10); Sfx.fx("land"); vib(30) }; b.vy = 0f }
        val per = max(50, 100 - (3 - b.hp) * 20 - b.kind * 10)
        if (b.t % per == 0) {
            val dx = p.x - b.x - 12
            shots.add(Shot(b.x + 17, b.y + 6, (dx / 50f).coerceIn(-3f, 3f), -4.5f, if (b.kind == 1) 'c' else 'p'))
            if (b.kind == 1 && b.hp < 3) shots.add(Shot(b.x + 17, b.y + 6, (dx / 70f).coerceIn(-3f, 3f), -6f, 'c'))
            Sfx.fx("throw")
        }
        if (b.t % (per * 3) == per * 3 - 1 && b.y >= bossGround - b.h - 1) b.vy = -6.5f
        if (ov(p.x, p.y, p.w, p.h, b.x, b.y, b.w, b.h) && b.inv == 0) {
            if (p.vy > 0 && p.y + p.h - p.vy <= b.y + 8) {
                b.hp--; b.inv = 70; p.vy = -6.8f; Sfx.fx("bosshit"); burst(b.x + 17, b.y, "#f4c430", 18); vib(50)
                if (b.hp <= 0) {
                    b.dead = 1; Music.play(LV.song)
                    if (b.kind == 0) toast(L("Жадина-ака: «Ой-ой! Забирай свой казан!»", "Ochko'z aka: «Voy-voy! Qozoningni ol!»"))
                    else toast(L("Зикна-ака: «Ладно! Забирай свой кафгир!»", "Ziqna aka: «Mayli! Kapgiringni ol!»"))
                    Sfx.fx("lose")
                    if (bossHits == 0) egg(zones.find { it.cond == "perfect" })
                }
            } else hurt()
        }
    }

    private fun updShots() {
        val p = pl
        for (s in shots) {
            s.life++
            if (s.k == 'm') {   // rolling melon
                s.x += s.vx; s.vy += 0.3f; s.y += s.vy
                val gy = (mini?.gy ?: bossGround) - 4
                if (s.y > gy) { s.y = gy; s.vy = -abs(s.vy) * 0.45f }
            } else { s.vy += 0.2f; s.x += s.vx; s.y += s.vy }
            if (ov(p.x, p.y, p.w, p.h, s.x - 3, s.y - 3, 6f, 6f)) { s.dead = true; hurt() }
            if (s.y > rows * T || s.life > 400 || s.x < camX - 60 || s.x > camX + W + 60) s.dead = true
            if (s.k != 'm' && solidAt(s.x, s.y + 3) && s.vy > 0) s.dead = true
            if (s.dead) burst(s.x, s.y, if (s.k == 'c') "#f4c430" else if (s.k == 's') "#cfe8d0" else "#f0c27a", 4)
        }
        shots.removeAll { it.dead }
    }

    // ================= endless generation =================
    private fun genEndless(b: B, upTo: Int) {
        while (genX < upTo) {
            val diff = min(1f, genX / 1500f)
            val r = G.rnd()
            val len = 6 + (G.rnd() * 9).toInt()
            val top = (genTop + listOf(-1, 0, 0, 1)[(G.rnd() * 4).toInt()]).coerceIn(12, 14)
            val x0 = genX; val x1 = min(cols - 1, genX + len)
            b.G(x0, x1, top)
            when {
                r < 0.25 -> b.N(x0 + 2, min(x1 - 1, x0 + 5), top - 2)
                r < 0.45 -> { b.E(if (G.rnd() < 0.6) 'g' else 'k', x0 + len / 2, top - 1, 2) }
                r < 0.58 -> { val px = x0 + 2; b.P(px, min(x1, px + 3), top - 4); b.N(px, min(x1, px + 3), top - 5) }
                r < 0.66 -> { b.P(x0 + len / 2, x0 + len / 2, top - 1, 'J'); b.N(x0 + len / 2 - 1, x0 + len / 2 + 1, top - 8) }
                r < 0.74 -> b.E('b', x0 + len / 2, top - 4, 3)
                r < 0.78 -> b.I('s', x0 + len / 2, top - 2)
                r < 0.81 -> b.I('d', x0 + len / 2, top - 3)
            }
            genTop = top
            val gap = if (G.rnd() < 0.55 + diff * 0.2) 2 + (G.rnd() * (1 + 2 * diff)).toInt() else 0
            if (gap > 0 && G.rnd() < 0.3) { for (k in 0 until gap) b.P(x1 + 1 + k, x1 + 1 + k, top, 'C') }
            genX = x1 + 1 + gap
        }
    }

    private fun growEndless() {
        val add = 300
        val nc = cols + add
        map = Array(rows) { r -> CharArray(nc) { c -> if (c < cols) map[r][c] else '.' } }
        cols = nc
        genEndless(B(this), cols - 20)
    }

    // ================= render =================
    fun render(c: Cv = G.c) {
        Plat.gfx.identity(); c.scale(3f, 3f)
        val inCut = state == "cut"
        val bgi = if (inCut) (Cuts.ALL[cutId]?.bg ?: 0) else LV.bg
        if (inCut) { camX = (frame * 0.4f) % 1200f; camY = max(0f, rows * T - H.toFloat()) }
        Bg.draw(this, bgi)
        if (!inCut) {
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
                    'q' -> { if (LV.bossKind == 1) G.spr(Spr.KAF, x, y, Spr.PAL) else G.spr(Spr.QOZ, x, y + 2, Spr.pal('O' to "#f0c27a")); if (frame % 10 < 5) px("#ffffff", x + 4, y - 2) }
                    else -> { G.spr(Spr.SX[i.egg] ?: Spr.SX["jeton"]!!, x + 2, y + 2, Spr.PAL); if (frame % 24 < 12) px("#ffffff", x + 10, y) }
                }
            }
            for (e in enemies) {
                val sh = Spr.enemyH(e.t)
                c.save(); c.translate((e.x + e.w / 2 - camX).roundToInt().toFloat(), (e.y + e.h - sh - camY).roundToInt().toFloat()); c.scale(if (e.vx > 0) 1f else -1f, 1f); c.translate(-8f, 0f)
                if (e.dead > 0) { c.translate(0f, sh * 0.6f); c.scale(1f, 0.4f) }
                G.drawParts(Spr.enemy(e.t, e.f / (if (e.fly) 5 else 7)), 0f); c.restore()
            }
            drawBoss(); drawMini()
            drawPlayer(c)
            LV.over?.invoke(this)
            drawWaterTop()
            drawFx()
            drawWindFx()
            if (lowg > 0) for (i in 0 until 20) { val h = hash(i, 3); px("#fff1e8", h % W, (h shr 8) % 100) }
        }
        Season.overlay(season, frame)
        if (state in setOf("play", "won", "dead")) hud()
        if (state != "play" && state != "won" && state != "dead") Menu.draw(this)
    }

    private fun drawPlayer(c: Cv) {
        val p = pl
        if (p.y <= -500 || (p.inv > 0 && p.inv < 76 && (frame / 4) % 2 == 1)) return
        var face = p.face
        if (dance > 0) face = if (frame % 40 < 20) 1 else -1
        val gold = p.pow > 0 && !(p.pow < 120 && frame % 10 < 5)
        val sink = p.sink.roundToInt()
        val baseY = (p.y + p.h - 31 - camY).roundToInt().toFloat()
        val cx = (p.x + p.w / 2 - camX).roundToInt().toFloat()
        if (gold && abs(p.vx) > 0.5f) G.alpha(0.3f) {   // after-image while powered up
            c.save(); c.translate(cx - p.vx * 4, baseY); c.scale(face.toFloat(), 1f); c.translate(-12f, 0f)
            Hero.draw(p.anim, p.animT, skin(), true, (p.dist / 26f) % 1f, p.vy); c.restore()
        }
        if (p.on && sink == 0 && p.zip == null) G.alpha(0.25f) { G.circle("#000000", cx, baseY + 31.5f, 5f) }
        c.save()
        if (sink > 0) c.clipRect(0f, 0f, W.toFloat(), baseY + 31)
        c.translate(cx, baseY + sink); c.scale(face.toFloat(), 1f); c.translate(-12f, 0f)
        Hero.draw(p.anim, p.animT, skin(), gold, (p.dist / 26f) % 1f, p.vy)
        c.restore()
        if (p.carry > 0) { c.save(); c.translate((p.x + 5 - camX).roundToInt() - 8f, (p.y - camY - 10).roundToInt().toFloat()); G.drawParts(Spr.enemy('h', frame / 5), 0f); c.restore() }
    }

    /** Picks the animation for this frame and spawns footstep / landing dust. */
    private fun animatePlayer(vyPrev: Float) {
        val p = pl
        if (p.on && !p.wasOn && vyPrev > 3f && p.zip == null && !p.inWater) {
            p.landT = 8; dust(p.x + 5, p.y + p.h, 5); Sfx.fx("land")
        }
        if (p.landT > 0) p.landT--
        if (p.on && abs(p.vx) > 1.2f && frame % 9 == 0 && p.sink == 0f) dust(p.x + 5 - p.face * 4, p.y + p.h, 1, -p.face * 0.4f)
        p.wasOn = p.on
        val a = when {
            state == "won" -> "win"
            p.zip != null -> "hang"
            dance > 0 -> "dance"
            p.inWater -> "swim"
            p.inv > 76 -> "hurt"
            !p.on -> if (p.vy < -0.5f) "jump" else "fall"
            p.landT > 0 -> "land"
            abs(p.vx) > 0.2f -> "run"
            else -> "idle"
        }
        if (a != p.anim) { p.anim = a; p.animT = 0 } else p.animT++
    }

    private fun note(x: Float, y: Float, heart: Boolean) {
        if (heart) { G.spr(Spr.HRT, x, y + 2, Spr.PAL) }
        else { px("#f4c430", x + 3, y, 1, 6); px("#f4c430", x + 3, y, 3, 1); px("#f4c430", x + 1, y + 5, 3, 2) }
    }

    private fun hud() {
        val p = pl
        for (i in 0 until max(3, p.hp)) G.spr(Spr.HRT, 5 + i * 9, 6, Spr.pal('R' to if (i < p.hp) "#e24b4a" else "#4a4a5a"))
        if (endless) {
            val m = ((p.x - runStart) / T).roundToInt().coerceAtLeast(0)
            txt("${m}m", W / 2f, 6f, "#fff1e8", BIG, 1)
            txt(L("Рекорд", "Rekord") + " ${max(best, m)}m", W / 2f, 18f, "#f4c430", SM, 1)
            G.spr(Spr.NON, W - 50, 20, Spr.PAL); txt("$coins", W - 38f, 22f, size = SM)
        } else {
            G.spr(Spr.NON, W - 50, 20, Spr.PAL); txt("$coins/$total", W - 38f, 22f, size = SM)
            star(W - 50f, 33f); txt("${found.size}/${zones.size}", W - 38f, 33f, "#f4c430", SM)
        }
        if (p.pow > 0) { px(OUT, 5, 16, 52, 5); px("#f4c430", 6, 17, (50f * p.pow / powTime()).roundToInt(), 3) }
        if (lowg > 0) txt(L("НЕВЕСОМОСТЬ", "VAZNSIZLIK"), 5f, 24f, "#86c5ef", SM)
        if (LV.vertical) txt("${((1 - p.y / (rows * T)) * 375).roundToInt()}m", 5f, 24f, "#fff1e8", SM)
        px("#bb0f1631", W - 22, 4, 18, 12); px("#fff1e8", W - 16, 7, 2, 6); px("#fff1e8", W - 11, 7, 2, 6)
        if (msg != null && msgF > 0) { msgF--; txt(msg!!, W / 2f, 90f, "#f4c430", BIG, 1) }
        if (!endless && frame < 200 && toasts.isEmpty() && boss == null && mini == null) txt("${LVI + 1}. " + L(LV.n[0], LV.n[1]), W / 2f, 60f, "#f4c430", BIG, 1)
        for (w in winds) if (stormSoon(w) && frame % 16 < 10) txt(L("БУРЯ!", "BO'RON!"), W / 2f, 76f, "#ffe28a", BIG, 1)
        drawToast()
    }

    fun star(x: Float, y: Float, col: String = "#f4c430") { px(col, x + 2, y, 1, 5); px(col, x, y + 2, 5, 1); px(col, x + 1, y + 1, 3, 3) }

    private fun drawToast() {
        if (toasts.isEmpty()) return
        val t = toasts[0]; t.f--; if (t.f <= 0) { toasts.removeAt(0); return }
        val lines = G.wrap(t.s, 152f, SM); val h = lines.size * 9 + 9; val y = if (boss != null || mini != null) 42f else 48f
        px(OUT, 9, y - 1, 162, h + 2); px("#1d2b53", 10, y, 160, h); px("#f4c430", 10, y, 160, 2)
        lines.forEachIndexed { i, l -> txt(l, 90f, y + 5 + i * 9, "#fff1e8", SM, 1) }
    }

    // ---------- tiles ----------
    private fun drawTiles() {
        val th = LV.th
        val snow = season == Season.WINTER; val flowers = season == Season.NAVRUZ
        val c0 = max(0, (camX / T).toInt()); val c1 = min(cols - 1, c0 + W / T + 1)
        val r0 = max(0, (camY / T).toInt()); val r1 = min(rows - 1, r0 + H / T + 1)
        val blink = (frame % 150) > 115 && frame % 8 < 4
        val st = if (endless) 0 else LV.bg
        for (ty in r0..r1) for (tx in c0..c1) {
            val ch = map[ty][tx]; if (ch == '.') continue
            val x = X(tx); val y = Y(ty); val h = hash(tx, ty)
            when (ch) {
                '#' -> { val top = tileAt(tx, ty - 1) != '#'; val l = tileAt(tx - 1, ty) == '.'; val r = tileAt(tx + 1, ty) == '.'; val v = h % 4
                    if (top) Plat.gfx.cached("tile:$st:g:$v:1:$l:$r", 16, 20, x, y - 4) { Tiles.ground(st, th, v, true, l, r) }
                    else Plat.gfx.cached("tile:$st:g:$v:0:$l:$r", 16, 16, x, y) { Tiles.ground(st, th, v, false, l, r) }
                    if (top && snow) { px("#f4f8ff", x, y - 1, T, 3); if (h % 3 == 0) px("#f4f8ff", x + h % 10, y + 2, 4, 2) }
                    if (top && flowers && h % 5 == 0) { px("#2e9b6b", x + 6, y - 4, 1, 4); px(if (h % 2 == 0) "#e24b4a" else "#f4c430", x + 5, y - 6, 3, 2) } }
                'B' -> { val top = tileAt(tx, ty - 1) != 'B'
                    Plat.gfx.cached("tile:$st:B:${ty % 2}:$top", 16, 16, x, y) { Tiles.wall(st, th, ty % 2, top) }
                    if (top && snow) px("#f4f8ff", x, y - 1, T, 2) }
                '=' -> { Plat.gfx.cached("tile:$st:P:${h % 3}", 16, 8, x, y - 1) { Tiles.plank(st, th, h % 3) }
                    if (snow) px("#f4f8ff", x, y - 1, T, 1) }
                'p' -> { px(OUT, x, y - 1, T, 7); px("#f2c230", x, y, T, 5); px("#fff1a0", x, y, T, 1); px("#d8a818", x, y + 3, T, 1); px("#b88a10", x, y + 4, T, 1)
                    if (tx % 2 == 0) { px(OUT, x + 6, y + 5, 4, 48); px("#f2c230", x + 7, y + 5, 2, 48); px("#fff1a0", x + 7, y + 5, 1, 48); px(OUT, x + 4, y - 1, 8, 2); px("#9aa1ab", x + 5, y - 1, 6, 1) } }
                'w' -> { val surf = tileAt(tx, ty - 1) != 'w'
                    px(if (surf) "#3f8fcf" else "#2f74b0", x, y, T, T); px(if (surf) "#357fc0" else "#28689e", x, y + 10, T, 6)
                    val wv = ((frame / 6 + tx * 3) % 8)
                    if (surf) { px("#86c5ef", x, y, T, 2); px("#bfe4f7", x + wv * 2 % 14, y, 4, 1); px("#e8f6ff", x + ((frame / 3 + tx * 5) % 14), y + 1, 2, 1); px("#5fa8dc", x + (wv * 3 + 5) % 14, y + 5, 3, 1) }
                    else px("#3f8fcf", x + (wv * 2 + tx) % 14, y + 6, 2, 1) }
                'J' -> { val sq = if (abs(pl.x + 5 - (tx * T + 8)) < 12 && pl.vy < -6) 3 else 0
                    px(OUT, x, y + 3 + sq, T, 13 - sq)
                    for (k in 0 until 3) { val ly = y + 4 + sq + k * ((12 - sq) / 3); val c = arrayOf("#d8373b", "#2457c5", "#2e9b6b")[k]
                        px(c, x + 1, ly, 14, (12 - sq) / 3); px(G.shade(c, 1.3f), x + 1, ly, 14, 1)
                        for (q in 0 until 3) { px("#f4c430", x + 3 + q * 5, ly + 1, 2, 1); px("#f5f0e6", x + 4 + q * 5, ly + 2, 1, 1) } }
                    px("#59000000", x + 1, y + 14, 14, 2) }
                'C' -> { val cr = crumble[ty * cols + tx] ?: 0; val sh = if (cr > 0 && frame % 4 < 2) 1 else 0
                    px(OUT, x + sh, y, T, T); px("#b07a45", x + 1 + sh, y + 1, 14, 14); px("#c99a5a", x + 1 + sh, y + 1, 14, 1); px("#8b5a2b", x + 1 + sh, y + 5, 14, 1); px("#8b5a2b", x + 1 + sh, y + 10, 14, 1)
                    px("#8b5a2b", x + 2 + sh, y + 1, 1, 14); px("#8b5a2b", x + 13 + sh, y + 1, 1, 14); px("#d9b779", x + 4 + sh, y + 7, 8, 1)
                    px(OUT, x + 2 + sh, y - 4, 12, 6); px("#e9c94a", x + 3 + sh, y - 3, 10, 5); px("#f4dc7a", x + 4 + sh, y - 3, 6, 1); px("#b89a2a", x + 5 + sh, y - 3, 1, 5); px("#b89a2a", x + 9 + sh, y - 3, 1, 5)
                    if (cr > 18) px(OUT, x + 7 + sh, y + 2, 1, 12) }
                'E', 'e' -> { px("#3a4250", x, y, T, T); px("#9aa1ab", x, y, T, 5); val d = if (ch == 'E') 1 else -1
                    for (k in 0 until 4) { val ox = ((frame * d / 2 + k * 4) % 16 + 16) % 16; px("#6b7280", x + ox, y + 1, 1, 3) }
                    px("#f4c430", x, y, T, 1); px("#2b2b33", x, y + 5, T, 1); px("#5a6270", x + 2, y + 8, 12, 1)
                    val ax = x + 6 + (if (d > 0) ((frame / 6) % 3) else 2 - ((frame / 6) % 3)); px("#86c5ef", ax, y + 10, 4, 1); px("#86c5ef", ax + (if (d > 0) 3 else 0), y + 9, 1, 3) }
                'Q' -> { px("#d9b779", x, y, T, T); px("#e8cd95", x, y, T, 3); px("#c9a46a", x, y + 13, T, 3)
                    if ((frame / 10 + tx) % 5 == 0) { px("#b8935a", x + 6, y + 6, 4, 2); px("#a8834a", x + 7, y + 7, 2, 1) }
                    val o = (frame / 4 + tx * 3) % 16; px("#c9a46a", x + o % 12, y + 6, 4, 1); px("#c9a46a", x + (o + 7) % 12, y + 10, 3, 1)
                    if (frame % 30 < 15) px("#b8935a", x + h % 12, y + 2, 2, 1) }
                'M', 'm' -> { val act = (ch == 'M') == mosaicA
                    if (act && !blink) Plat.gfx.cached("tile:mosaic", 16, 16, x, y) {
                        px(OUT, 0, 0, T, T); px("#1f4ea8", 1, 1, 14, 14); px("#2fb3c9", 2, 2, 12, 12); px("#1f4ea8", 3, 3, 10, 10)
                        px("#f5f0e6", 7, 3, 2, 10); px("#f5f0e6", 3, 7, 10, 2); px("#f5f0e6", 5, 5, 6, 6); px("#2fb3c9", 6, 6, 4, 4); px("#f4c430", 7, 7, 2, 2)
                        px("#86c5ef", 1, 1, 14, 1); px("#14306e", 1, 14, 14, 1) }
                    else { val a = if (act) "#cc2fb3c9" else "#552fb3c9"; px(a, x + 1, y + 1, 14, 1); px(a, x + 1, y + 14, 14, 1); px(a, x + 1, y + 1, 1, 14); px(a, x + 14, y + 1, 1, 14) } }
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
            val act = windActive(w)
            if (w.storm) {
                if (act) {
                    px("#55c99a5a", 0, 0, W, H)
                    for (i in 0 until 26) { val h = hash(i, 77); val yy = h % H; val xx = W - ((h shr 5) + frame * 6) % (W + 40); px("#ccf0d8a8", xx, yy, 18, 1) }
                }
                continue
            }
            val y0 = w.y0 - camY; val y1 = w.y1 - camY
            if (y1 < 0 || y0 > H) continue
            val ph = (frame + w.phase) % 240
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
        pl.zip?.let { _ -> val hx = (pl.x + 5 - camX).roundToInt(); val hy = (pl.y - 2 - camY).roundToInt(); px(OUT, hx - 4, hy - 2, 9, 4); px("#d8373b", hx - 3, hy - 1, 7, 2) }
    }

    private fun drawMP(m: MP) {
        val x = (m.x - camX).roundToInt().toFloat(); val y = (m.y - camY).roundToInt().toFloat(); val w = m.w
        when (m.kind) {
            "train" -> { px(OUT, x - 1, y - 1, w + 2, 22); px("#2e6fd8", x, y, w, 20); px("#f5f5f5", x, y + 8, w, 3); for (i in 0 until 4) px("#cfe8ff", x + 4 + i * 15, y + 2, 10, 5); px("#f4c430", x + w - 4, y + 12, 3, 3) }
            "boat" -> { px(OUT, x - 1, y - 1, w + 2, 10); px("#d8373b", x, y, w, 4); px("#f5f0e6", x + 2, y + 4, w - 4, 4); px("#8b5a2b", x + w / 2 - 1, y - 14, 2, 14) }
            "camel" -> {
                val f = ((m.x / 6).toInt()) % 2; val d = m.dir
                // legs
                for (k in 0 until 4) { val lx = x + 6 + k * 11; val sw = if ((k + f) % 2 == 0) 2 else -2; px(OUT, lx - 1 + sw, y + 13, 4, 20); px("#c08a4a", lx + sw, y + 14, 2, 18) }
                px(OUT, x - 1, y + 3, w + 2, 13); px("#d9a35a", x, y + 4, w, 11); px("#b8833a", x, y + 12, w, 3)
                px(OUT, x + 8, y - 1, 32, 6); px("#d9a35a", x + 9, y, 30, 5)            // hump (saddle)
                px("#d8373b", x + 12, y, 24, 3); px("#f4c430", x + 12, y + 3, 24, 1)    // carpet
                val hx = if (d > 0) x + w - 2 else x - 10
                px(OUT, hx - 1, y - 9, 13, 16); px("#d9a35a", hx + (if (d > 0) 0 else 6), y - 2, 6, 8); px("#d9a35a", hx, y - 8, 12, 6); px("#000000", hx + (if (d > 0) 8 else 2), y - 7, 1, 1)
            }
            else -> { px(OUT, x - 1, y - 1, w + 2, 8); px("#c9ced6", x, y, w, 6); px("#6b7280", x, y + 5, w, 1); px("#6b7280", x + w / 2, y - 200, 1, 200) }
        }
    }

    private fun drawGoal() {
        val g = goal ?: return
        val x = (g.x - camX).roundToInt().toFloat(); val y = (g.y - camY).roundToInt().toFloat()
        val locked = miniDef != null && !miniDone
        if (g.kind == "deck") { px(OUT, x - 2, y + g.h - 8, g.w + 4, 10); px("#86c5ef", x, y + g.h - 6, g.w, 6); px("#f4c430", x + g.w / 2 - 2, y, 4, g.h - 8); if (frame % 20 < 10) px("#e24b4a", x + g.w / 2 - 2, y - 3, 4, 3); return }
        val door = if (g.kind == "mgate") "#c9a227" else "#2f7fc1"
        px(OUT, x - 3, y - 7, 38, 57); px("#6b3f1f", x - 2, y - 6, 36, 6); px("#f4c430", x + 4, y - 4, 24, 2)
        px("#6b3f1f", x - 2, y, 4, 48); px("#6b3f1f", x + 30, y, 4, 48); px(door, x + 2, y, 28, 48); px(OUT, x + 15, y, 2, 48)
        for (d in arrayOf(4 to 6, 4 to 22, 4 to 36)) { px("#86c5ef", x + d.first, y + d.second, 10, 10); px(door, x + d.first + 2, y + d.second + 2, 6, 6); px("#86c5ef", x + d.first + 18, y + d.second, 10, 10); px(door, x + d.first + 20, y + d.second + 2, 6, 6) }
        if (locked) { px(OUT, x + 10, y + 20, 12, 10); px("#f4c430", x + 11, y + 21, 10, 8); px(OUT, x + 15, y + 23, 2, 3); px(OUT, x + 12, y + 15, 2, 6); px(OUT, x + 18, y + 15, 2, 6); px(OUT, x + 12, y + 14, 8, 2) }
        if (g.kind == "mgate") txt("CHIQISH", x - 4, y - 18, "#f4c430", SM)
    }

    private fun drawVeh() {
        val v = veh ?: return
        val x = vehX.roundToInt().toFloat(); val b = 196f
        when (v) {
            "damas" -> {
                px(OUT, x - 1, b - 5, 44, 20); px("#f2f2f2", x, b - 4, 42, 16); px("#86c5ef", x + 24, b - 2, 14, 6); px("#86c5ef", x + 4, b - 2, 16, 6)
                px("#222222", x + 4, b + 11, 7, 5); px("#222222", x + 30, b + 11, 7, 5); px("#d8373b", x + 2, b - 11, 30, 7); px("#2b2b33", x + 12, b - 16, 12, 5); px("#f4c430", x + 4, b - 9, 26, 2)
            }
            "afrosiyob" -> {
                val tx = x * 2.2f - 80; px(OUT, tx - 1, 159, 132, 16); px("#f5f5f5", tx, 160, 130, 14); px("#2e6fd8", tx, 168, 130, 3)
                for (i in 0 until 9) px("#86c5ef", tx + 6 + i * 13, 162, 8, 4); px("#f5f5f5", tx + 128, 163, 8, 11); px("#d8373b", tx + 133, 168, 3, 3)
            }
        }
    }

    private fun drawBoss() {
        val b = boss ?: return
        if (b.dead > 40) return
        if (b.inv > 0 && frame % 6 < 3) return
        val c = G.c; c.save(); c.translate((b.x - camX).roundToInt().toFloat(), (b.y - camY).roundToInt().toFloat())
        val f = (b.t / 8) % 2
        if (b.kind == 0) G.drawParts(listOf(
            P(7, 31, 5, 9 - f, "#9aa1ab"), P(22, 31, 5, 8 + f, "#9aa1ab"), P(5, 38 - f, 9, 2, "#55556a"), P(20, 37 + f, 9, 3, "#55556a"),
            P(4, 10, 6, 4, "#3a2f5a"), P(24, 10, 6, 4, "#3a2f5a"),
            P(11, 0, 12, 12, "#e2a676") { px("#000000", 12, 4, 10, 2); px("#ffffff", 13, 4, 2, 1); px("#1c1410", 13, 8, 8, 2); px("#1c1410", 11, 0, 12, 2) },
            P(-2, 13, 38, 4, "#55556a") { px("#f0c27a", 0, 13, 34, 2) },
            P(0, 17, 34, 15, "#2b2b33") { px("#3a3a48", 2, 19, 30, 2); px("#f4c430", 13, 24, 8, 4); px("#b8860b", 16, 23, 2, 6) },
            P(12, 12, 10, 2, "#f4c430")), 0f)
        else G.drawParts(listOf(   // Ziqna-aka on a mechanical camel
            P(4, 30, 4, 10 - f, "#9aa1ab"), P(12, 30, 4, 9 + f, "#9aa1ab"), P(20, 30, 4, 10 - f, "#9aa1ab"), P(28, 30, 4, 9 + f, "#9aa1ab"),
            P(0, 20, 34, 11, "#c9a227") { px("#8b6b1a", 2, 26, 30, 2); for (k in 0 until 5) px("#55556a", 4 + k * 6, 22, 2, 2) },
            P(30, 10, 5, 12, "#c9a227"), P(30, 6, 9, 6, "#c9a227") { px("#e24b4a", 36, 7, 2, 2) },
            P(10, 8, 14, 13, "#c9a227") { px("#f4c430", 16, 8, 2, 13); px("#8b6b1a", 10, 16, 14, 2) },
            P(12, -3, 10, 11, "#e2a676") { px("#000000", 18, 1, 2, 1); px("#1c1410", 13, 6, 8, 2); px("#f2f2f2", 14, 7, 6, 1) },
            P(11, -6, 12, 4, "#1c1410") { px("#f4c430", 13, -5, 8, 1) }), 0f)
        c.restore()
        for (i in 0 until 3) G.spr(Spr.HRT, W / 2 - 14 + i * 10, 22, Spr.pal('R' to if (i < b.hp) "#7b3fa0" else "#4a4a5a"))
        txt(if (b.kind == 0) L("ЖАДИНА-АКА", "OCHKO'Z AKA") else L("ЗИКНА-АКА", "ZIQNA AKA"), W / 2f, 13f, "#f4c430", SM, 1)
        for (s in shots) drawShot(s)
    }

    fun drawShot(s: Shot) {
        val x = (s.x - camX).roundToInt(); val y = (s.y - camY).roundToInt()
        when (s.k) {
            'c' -> { px(OUT, x - 3, y - 3, 7, 7); px("#f4c430", x - 2, y - 2, 5, 5); px("#b8860b", x, y - 1, 1, 3) }
            'm' -> { px(OUT, x - 5, y - 4, 11, 9); px("#e9c94a", x - 4, y - 3, 9, 7); px("#b89a2a", x - 1 + (frame / 4) % 3, y - 3, 1, 7) }
            'j' -> { px(OUT, x - 3, y - 3, 7, 7); px("#c98a3a", x - 2, y - 2, 5, 5); px("#8a5422", x - 1, y - 1, 3, 3) }
            's' -> { px("#cfe8d0", x - 2, y - 2, 4, 4); px("#ffffff", x - 1, y - 1, 2, 2) }
            else -> { px(OUT, x - 3, y - 3, 7, 7); px("#f0c27a", x - 2, y - 2, 5, 5); px("#f08a24", x, y - 1) }
        }
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

    // ================= menus, cutscenes, shop, settings =================
    fun show(s: String) {
        state = s; Menu.layout(this)
        if (s in MENU_BG) Music.play(Music.TITLE)
    }

    fun startCut(id: String, after: String) { cutId = id; cutLine = 0; cutAfter = after; addS("cuts", id); show("cut") }
    fun cutSeen(id: String) = gS("cuts").contains(id)
    fun nextLine() {
        val cut = Cuts.ALL[cutId]
        cutLine++
        if (cut == null || cutLine >= cut.lines.size) endCut()
        else { Sfx.fx("click"); Menu.layout(this) }
    }
    fun endCut() { if (cutAfter == "win") show("win") else if (cutAfter == "credits") show("credits") else { introI = cutAfter.toIntOrNull() ?: introI; show("intro") } }

    fun goIntro(i: Int) {
        introI = i
        val ci = levels[i].cutIn
        if (ci != null && !cutSeen(ci)) startCut(ci, "$i") else show("intro")
    }
    fun replayCut(i: Int) { levels[i].cutIn?.let { startCut(it, "$i") } }

    fun tap(gx: Float, gy: Float) {
        if (state == "play") { if (gx > W - 26 && gy < 20) show("pause"); return }
        for (b in btns.toList()) if (gx >= b.x && gx <= b.x + b.w && gy >= b.y && gy <= b.y + b.h) { Sfx.fx("click"); b.act(); return }
        if (state == "cut") nextLine()
    }

    fun back(): Boolean = when (state) {
        "play" -> { show("pause"); true }
        "title" -> false
        "cut" -> { endCut(); true }
        else -> { goTitle(); true }
    }

    fun goTitle() { if (endless || LVI < 0) { endless = false; loadLevel(0) }; show("title") }

    fun changeLang(l: String) { lang = l; st.putString("lang", l); Menu.layout(this) }
    fun play(i: Int) = startLevel(i)
    fun allEggs() = levels.indices.sumOf { savedEggs(it).size }
    fun allStars() = levels.indices.sumOf { stars(it).countOneBits() }

    fun buy(s: Skin): Boolean {
        if (skinAvailable(s)) { skinId = s.id; st.putString("skin", s.id); return true }
        if (wallet < s.price) return false
        wallet -= s.price; pI("wallet", wallet); addS("owned", s.id); skinId = s.id; st.putString("skin", s.id)
        Sfx.fx("door"); vib(30); return true
    }
    fun buyUp(u: Upgrade): Boolean {
        if (hasUp(u.id) || wallet < u.price) return false
        wallet -= u.price; pI("wallet", wallet); addS("ups", u.id); Sfx.fx("door"); vib(30); return true
    }

    fun setOpt(k: String) {
        when (k) {
            "music" -> { musicOn = !musicOn; pB("music", musicOn); Music.enabled = musicOn }
            "sfx" -> { sfxOn = !sfxOn; pB("sfx", sfxOn); Sfx.enabled = sfxOn }
            "vib" -> { vibOn = !vibOn; pB("vib", vibOn); vib(40) }
            "size" -> { btnSize = (btnSize + 1) % 3; pI("btnSize", btnSize) }
            "hand" -> { lefty = !lefty; pB("lefty", lefty) }
            "season" -> { seasonSet = (seasonSet + 1) % 5; pI("season", seasonSet) }
        }
        settingsVer++; Menu.layout(this)
    }

    init {
        Music.enabled = musicOn; Sfx.enabled = sfxOn
        eggTotals.size
        loadLevel(0); Menu.layout(this)
    }

    companion object { val MENU_BG = setOf("title", "levels", "intro", "shop", "settings", "credits") }
}
