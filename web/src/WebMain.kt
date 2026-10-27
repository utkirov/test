package uz.botir.game

import kotlin.math.PI
import kotlin.math.min

/*
 * Web / Telegram Mini App front-end for the shared game code.
 * Uses `dynamic` for browser and Telegram APIs so it compiles with the plain Kotlin/JS stdlib.
 */

private val window: dynamic get() = js("window")
private val document: dynamic get() = js("document")
private val tg: dynamic get() = js("(window.Telegram && window.Telegram.WebApp) ? window.Telegram.WebApp : null")

private fun tgAtLeast(v: String): Boolean = tg != null && (tg.isVersionAtLeast(v) as Boolean)

class WebGfx(var ctx: dynamic) : Gfx {
    private val css = HashMap<Int, String>()
    private val fonts = HashMap<Float, String>()
    private fun c(v: Int): String = css.getOrPut(v) {
        val a = (v ushr 24) and 255; val r = (v shr 16) and 255; val g = (v shr 8) and 255; val b = v and 255
        if (a == 255) "rgb($r,$g,$b)" else "rgba($r,$g,$b,${a / 255.0})"
    }
    private fun font(size: Float) = fonts.getOrPut(size) { "${size}px 'Press Start 2P', monospace" }
    override fun rect(c: Int, x: Float, y: Float, w: Float, h: Float) { ctx.fillStyle = c(c); ctx.fillRect(x, y, w, h) }
    override fun circle(c: Int, x: Float, y: Float, r: Float) { ctx.fillStyle = c(c); ctx.beginPath(); ctx.arc(x, y, r, 0, 2 * PI); ctx.fill() }
    override fun arcTop(c: Int, x: Float, y: Float, r: Float) { ctx.fillStyle = c(c); ctx.beginPath(); ctx.moveTo(x, y); ctx.arc(x, y, r, PI, 2 * PI); ctx.closePath(); ctx.fill() }
    override fun ring(c: Int, x: Float, y: Float, r: Float, w: Float) { ctx.strokeStyle = c(c); ctx.lineWidth = w; ctx.beginPath(); ctx.arc(x, y, r, 0, 2 * PI); ctx.stroke() }
    override fun line(c: Int, x0: Float, y0: Float, x1: Float, y1: Float, w: Float) { ctx.strokeStyle = c(c); ctx.lineWidth = w; ctx.beginPath(); ctx.moveTo(x0, y0); ctx.lineTo(x1, y1); ctx.stroke() }
    override fun poly(c: Int, xs: FloatArray, ys: FloatArray) {
        ctx.fillStyle = c(c); ctx.beginPath(); ctx.moveTo(xs[0], ys[0]); for (i in 1 until xs.size) ctx.lineTo(xs[i], ys[i]); ctx.closePath(); ctx.fill()
    }
    override fun text(s: String, x: Float, y: Float, c: Int, size: Float, align: Int) {
        ctx.font = font(size); ctx.textAlign = when (align) { 1 -> "center"; 2 -> "right"; else -> "left" }; ctx.textBaseline = "alphabetic"
        ctx.fillStyle = c(c); ctx.fillText(s, x, y)
    }
    override fun measure(s: String, size: Float): Float { ctx.font = font(size); return (ctx.measureText(s).width as Number).toFloat() }
    override fun save() { ctx.save() }
    override fun restore() { ctx.restore() }
    override fun translate(x: Float, y: Float) { ctx.translate(x, y) }
    override fun scale(x: Float, y: Float) { ctx.scale(x, y) }
    override fun clip(l: Float, t: Float, r: Float, b: Float) { ctx.beginPath(); ctx.rect(l, t, r - l, b - t); ctx.clip() }
    override fun saveAlpha(a: Float) { ctx.save(); ctx.globalAlpha = (ctx.globalAlpha as Number).toDouble() * a }
    override fun identity() { ctx.setTransform(1, 0, 0, 1, 0, 0) }

    private val pics = HashMap<String, dynamic>()
    override fun cached(key: String, w: Int, h: Int, x: Float, y: Float, draw: () -> Unit) {
        var pic = pics[key]
        if (pic == null) {
            val cnv = js("document.createElement('canvas')"); cnv.width = w * 3; cnv.height = h * 3
            val prev = ctx; ctx = cnv.getContext("2d"); ctx.scale(3, 3)
            try { draw() } finally { ctx = prev }
            pics[key] = cnv; pic = cnv
        }
        ctx.imageSmoothingEnabled = false
        ctx.drawImage(pic, x, y, w, h)
    }
    override fun dropCache(prefix: String) { pics.keys.filter { it.startsWith(prefix) }.forEach { pics.remove(it) } }
}

/** localStorage, mirrored to Telegram CloudStorage so progress follows the player between devices. */
object WebStore : Store {
    private const val PFX = "botir_"
    private val cloud get() = if (tgAtLeast("6.9")) tg.CloudStorage else null
    private fun raw(k: String): String? = try { window.localStorage.getItem(PFX + k) as String? } catch (e: dynamic) { mem[k] }
    private val mem = HashMap<String, String>()
    private fun put(k: String, v: String) {
        mem[k] = v
        try { window.localStorage.setItem(PFX + k, v) } catch (e: dynamic) { }
        try { cloud?.setItem(k, v) } catch (e: dynamic) { }
    }
    override fun getInt(k: String, d: Int) = raw(k)?.toIntOrNull() ?: d
    override fun putInt(k: String, v: Int) = put(k, v.toString())
    override fun getBool(k: String, d: Boolean) = raw(k)?.let { it == "1" } ?: d
    override fun putBool(k: String, v: Boolean) = put(k, if (v) "1" else "0")
    override fun getString(k: String, d: String) = raw(k) ?: d
    override fun putString(k: String, v: String) = put(k, v)
    override fun getSet(k: String): Set<String> = raw(k)?.split('|')?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    override fun putSet(k: String, v: Set<String>) = put(k, v.joinToString("|"))

    /** On a fresh device restore progress from the cloud; otherwise push local progress to the cloud. */
    fun sync(done: () -> Unit) {
        val cs = cloud
        val fresh = raw("_init") == null
        if (cs == null) { put("_init", "1"); done(); return }
        var finished = false
        val finish = { if (!finished) { finished = true; put("_init", "1"); done() } }
        window.setTimeout({ finish() }, 2500)
        try {
            cs.getKeys { err: dynamic, keys: dynamic ->
                if (err != null || keys == null || (keys.length as Int) == 0) { finish() }
                else cs.getItems(keys) { err2: dynamic, values: dynamic ->
                    if (err2 == null && values != null && fresh) {
                        val n = keys.length as Int
                        for (i in 0 until n) { val key = keys[i] as String; val v = values[key] as String?; if (v != null) try { window.localStorage.setItem(PFX + key, v) } catch (e: dynamic) { } }
                    }
                    finish()
                }
            }
        } catch (e: dynamic) { finish() }
    }
}

object WebAudio : Audio {
    private var ac: dynamic = null
    private var musicSrc: dynamic = null
    private var musicGain: dynamic = null
    private var want = -1; private var playing = -1
    private var musicOn = true
    private val sfxBuf = HashMap<String, dynamic>()
    private val songBuf = HashMap<Int, dynamic>()

    fun unlock() {
        if (ac == null) {
            ac = js("(window.AudioContext || window.webkitAudioContext) ? new (window.AudioContext || window.webkitAudioContext)() : null")
            if (ac == null) return
        }
        if (ac.state == "suspended") ac.resume()
        if (playing != want) startMusic()
    }
    fun suspend() { try { ac?.suspend() } catch (e: dynamic) { } }
    fun resume() { try { ac?.resume() } catch (e: dynamic) { } }

    private fun buffer(pcm: FloatArray): dynamic {
        val b = ac.createBuffer(1, pcm.size, Synth.RATE)
        b.getChannelData(0).set(pcm)
        return b
    }

    override fun play(key: String, pcm: () -> FloatArray) {
        if (ac == null || ac.state != "running") return
        val b = sfxBuf.getOrPut(key) { buffer(pcm()) }
        val src = ac.createBufferSource(); src.buffer = b; src.connect(ac.destination); src.start()
    }

    override fun music(i: Int) { want = i; if (ac != null) startMusic() }
    override fun setMusicEnabled(on: Boolean) { musicOn = on; if (ac != null) startMusic() }

    private fun startMusic() {
        val old = musicSrc; val oldGain = musicGain
        if (old != null) {
            try { val t = ac.currentTime as Double; oldGain.gain.setTargetAtTime(0, t, 0.08); old.stop(t + 0.4) } catch (e: dynamic) { }
            musicSrc = null
        }
        playing = want
        if (!musicOn || want !in 0 until Synth.songCount) return
        val b = songBuf.getOrPut(want) { buffer(Synth.song(want)) }
        val g = ac.createGain(); g.gain.value = 0.0
        val src = ac.createBufferSource(); src.buffer = b; src.loop = true
        src.connect(g); g.connect(ac.destination); src.start()
        try { g.gain.setTargetAtTime(1, ac.currentTime, 0.15) } catch (e: dynamic) { g.gain.value = 1 }
        musicSrc = src; musicGain = g
    }
}

/** Page host: full-window canvas, pixel game buffer, touch controls, Telegram integration. */
object WebHost {
    lateinit var game: Game
    private var canvas: dynamic = null
    private var ctx: dynamic = null
    private var off: dynamic = null
    private lateinit var gfx: WebGfx
    private var dpr = 1.0
    private var cw = 0.0; private var ch = 0.0
    private var dx = 0.0; private var dy = 0.0; private var dw = 0.0; private var dh = 0.0
    private var ctrlTop = 0.0; private var r = 60.0; private var cxL = 0.0; private var cxR = 0.0; private var cxJ = 0.0; private var cy = 0.0
    private var layoutVer = -1
    private val zoneOf = HashMap<String, Char>()
    private var last = 0.0; private var acc = 0.0
    private var lastBack = false
    private var galleryPage = -1

    fun start() {
        canvas = document.getElementById("c")
        ctx = canvas.getContext("2d")
        off = document.createElement("canvas"); off.width = W * 3; off.height = H * 3
        gfx = WebGfx(off.getContext("2d"))
        Plat.gfx = gfx; Plat.store = WebStore; Plat.audio = WebAudio
        Plat.later = { ms, f -> window.setTimeout({ f() }, ms) }
        Plat.today = { val d = js("new Date()"); intArrayOf(d.getFullYear() as Int, (d.getMonth() as Int) + 1, d.getDate() as Int) }
        Plat.platformName = "web"
        val t = tg
        if (t != null) {
            try { t.ready(); t.expand() } catch (e: dynamic) { }
            try { if (tgAtLeast("6.1")) { t.setHeaderColor("#0f1631"); t.setBackgroundColor("#0f1631") } } catch (e: dynamic) { }
            try { if (tgAtLeast("7.7")) t.disableVerticalSwipes() } catch (e: dynamic) { }
            try { if (tgAtLeast("8.0")) t.lockOrientation() } catch (e: dynamic) { }
            try { if (tgAtLeast("6.2")) t.enableClosingConfirmation() } catch (e: dynamic) { }
            val lc = try { t.initDataUnsafe?.user?.language_code as String? } catch (e: dynamic) { null }
            Plat.defaultLang = if (lc == "uz") "uz" else "ru"
            if (tgAtLeast("6.1")) Plat.vibrate = { ms -> try { t.HapticFeedback.impactOccurred(if (ms >= 50) "heavy" else if (ms >= 25) "medium" else "light") } catch (e: dynamic) { } }
            Plat.share = { text ->
                val link = (window.BOTIR_LINK as String?) ?: (window.location.href as String)
                val u = "https://t.me/share/url?url=" + js("encodeURIComponent")(link) + "&text=" + js("encodeURIComponent")(text)
                try { t.openTelegramLink(u) } catch (e: dynamic) { window.open(u, "_blank") }
            }
            try { t.BackButton.onClick { if (!game.back()) t.close() } } catch (e: dynamic) { }
            try { t.onEvent("viewportChanged") { layoutVer = -1 } } catch (e: dynamic) { }
        } else {
            val nav = (window.navigator.language as String?) ?: "ru"
            Plat.defaultLang = if (nav.startsWith("uz")) "uz" else "ru"
            if (window.navigator.vibrate != null) Plat.vibrate = { ms -> try { window.navigator.vibrate(ms) } catch (e: dynamic) { } }
        }
        WebStore.sync { boot() }
    }

    private fun boot() {
        game = Game()
        Music.play(Music.TITLE)
        window.addEventListener("resize", { layoutVer = -1 })
        document.addEventListener("visibilitychange", {
            if (document.hidden as Boolean) { if (game.state == "play") game.show("pause"); WebAudio.suspend() } else WebAudio.resume()
        })
        val opts: dynamic = js("({passive:false})")
        canvas.addEventListener("touchstart", { e: dynamic -> e.preventDefault(); WebAudio.unlock(); touches(e, 0) }, opts)
        canvas.addEventListener("touchmove", { e: dynamic -> e.preventDefault(); touches(e, 1) }, opts)
        canvas.addEventListener("touchend", { e: dynamic -> e.preventDefault(); touches(e, 2) }, opts)
        canvas.addEventListener("touchcancel", { e: dynamic -> e.preventDefault(); touches(e, 2) }, opts)
        canvas.addEventListener("mousedown", { e: dynamic -> WebAudio.unlock(); pointer("m", e.clientX as Double, e.clientY as Double, 0) })
        window.addEventListener("mousemove", { e: dynamic -> if ((e.buttons as Int) != 0) pointer("m", e.clientX as Double, e.clientY as Double, 1) })
        window.addEventListener("mouseup", { e: dynamic -> pointer("m", e.clientX as Double, e.clientY as Double, 2) })
        window.addEventListener("keydown", { e: dynamic -> key(e.code as String, true, e) })
        window.addEventListener("keyup", { e: dynamic -> key(e.code as String, false, e) })
        document.getElementById("load")?.remove()
        if ((window.location.search as String).contains("selftest")) { selfTest(); return }
        val gal = Regex("gallery=(\\d+)").find(window.location.search as String)
        if (gal != null) { galleryPage = gal.groupValues[1].toInt() }
        val wq = Regex("world=(\\d+)(?:&x=(\\d+))?").find(window.location.search as String)
        if (wq != null) { game.loadLevel(wq.groupValues[1].toInt()); game.state = "play"; game.toasts.clear()
            val xs = wq.groupValues[2]; if (xs.isNotEmpty()) { game.pl.x = xs.toFloat(); game.pl.y = 0f } }
        window.requestAnimationFrame { t: Double -> frame(t) }
    }

    private fun selfTest() {
        val res = SelfTest.run(game)
        window.selftestResult = res
        console("BOTIR_SELFTEST\n$res")
        document.title = "SELFTEST DONE"
        window.requestAnimationFrame { t: Double -> frame(t) }
    }

    private fun console(s: String) { js("console").log(s) }

    private fun key(code: String, down: Boolean, e: dynamic) {
        when (code) {
            "ArrowLeft", "KeyA" -> game.kL = down
            "ArrowRight", "KeyD" -> game.kR = down
            "ArrowUp", "KeyW", "Space" -> { if (down && !game.kJ) game.jumpPressed(); game.kJ = down }
            "Escape" -> if (down) game.back()
            else -> return
        }
        e.preventDefault(); if (down) WebAudio.unlock()
    }

    private fun layout() {
        layoutVer = game.settingsVer
        dpr = (window.devicePixelRatio as Number?)?.toDouble() ?: 1.0
        val vw = (window.innerWidth as Number).toDouble(); val vh = (window.innerHeight as Number).toDouble()
        cw = vw * dpr; ch = vh * dpr
        canvas.width = cw.toInt(); canvas.height = ch.toInt()
        val scale = arrayOf(0.82, 1.0, 1.2)[game.btnSize]
        val ctrlH = (ch * 0.24 * scale).coerceIn(150.0 * dpr, 420.0 * dpr)
        val avail = ch - ctrlH
        val s = min(cw / W, avail / H)
        dw = W * s; dh = H * s; dx = (cw - dw) / 2; dy = ((avail - dh) / 2).coerceAtLeast(0.0)
        ctrlTop = dy + dh
        val rest = ch - ctrlTop
        r = min(cw * 0.11 * scale, rest * 0.32)
        cy = ctrlTop + rest * 0.5
        if (!game.lefty) { cxL = r * 1.35; cxR = cxL + r * 2.45; cxJ = cw - r * 1.6 } else { cxJ = r * 1.6; cxR = cw - r * 1.35; cxL = cxR - r * 2.45 }
    }

    private fun zone(x: Double, y: Double): Char {
        if (y < ctrlTop) return ' '
        val mid = (cxL + cxR) / 2
        return if (!game.lefty) when { x < mid -> 'L'; x < cw * 0.55 -> 'R'; else -> 'J' } else when { x < cw * 0.45 -> 'J'; x < mid -> 'L'; else -> 'R' }
    }

    private fun refresh() { game.kL = zoneOf.containsValue('L'); game.kR = zoneOf.containsValue('R'); game.kJ = zoneOf.containsValue('J') }

    private fun pointer(id: String, cx: Double, cy0: Double, phase: Int) {
        val x = cx * dpr; val y = cy0 * dpr
        when (phase) {
            0 -> {
                val z = zone(x, y)
                if (z == ' ' || game.state != "play") game.tap(((x - dx) / dw * W).toFloat(), ((y - dy) / dh * H).toFloat())
                else { zoneOf[id] = z; if (z == 'J') game.jumpPressed(); refresh() }
            }
            1 -> { val old = zoneOf[id] ?: return; val z = zone(x, y); if (z != ' ' && z != old && old != 'J' && z != 'J') zoneOf[id] = z; refresh() }
            else -> { zoneOf.remove(id); refresh() }
        }
    }

    private fun touches(e: dynamic, phase: Int) {
        val list = e.changedTouches; val n = list.length as Int
        for (i in 0 until n) { val t = list[i]; pointer("t" + t.identifier, t.clientX as Double, t.clientY as Double, phase) }
    }

    private fun frame(t: Double) {
        if (last == 0.0) last = t
        acc += (t - last) / 1000.0; last = t
        if (acc > 0.1) acc = 0.1
        while (acc >= 1.0 / 60) { game.update(); acc -= 1.0 / 60 }
        if (layoutVer != game.settingsVer) layout()
        // Telegram back button mirrors the in-game back action
        val wantBack = game.state != "title"
        if (wantBack != lastBack) { lastBack = wantBack; try { if (wantBack) tg?.BackButton?.show() else tg?.BackButton?.hide() } catch (e: dynamic) { } }
        if (galleryPage >= 0) { Spr.tick = game.frame; Gallery.draw(game, game.frame, galleryPage) } else game.render()
        ctx.setTransform(1, 0, 0, 1, 0, 0)
        ctx.fillStyle = "#0f1631"; ctx.fillRect(0, 0, cw, ch)
        ctx.imageSmoothingEnabled = false
        ctx.drawImage(off, dx, dy, dw, dh)
        ctx.strokeStyle = "#000"; ctx.lineWidth = 2 * dpr; ctx.strokeRect(dx, dy, dw, dh)
        controls()
        window.requestAnimationFrame { tt: Double -> frame(tt) }
    }

    private fun controls() {
        val play = game.state == "play"
        ctx.globalAlpha = if (play) 1.0 else 0.35
        fun btn(x: Double, rr: Double, fill: String, pressed: Boolean) {
            ctx.fillStyle = "#000"; ctx.beginPath(); ctx.arc(x, cy + (if (pressed) 4 else 10) * dpr, rr, 0, 2 * PI); ctx.fill()
            ctx.fillStyle = if (pressed) "#2e6fd8" else fill; ctx.beginPath(); ctx.arc(x, cy + (if (pressed) 4 else 0) * dpr, rr - 4 * dpr, 0, 2 * PI); ctx.fill()
        }
        btn(cxL, r, "#1d2b53", game.kL); btn(cxR, r, "#1d2b53", game.kR); btn(cxJ, r * 1.2, "#d8373b", game.kJ)
        ctx.fillStyle = "#fff1e8"
        val tt = r * 0.42
        fun tri(x: Double, y: Double, d: Int) {
            ctx.beginPath()
            when (d) {
                -1 -> { ctx.moveTo(x - tt, y); ctx.lineTo(x + tt * 0.8, y - tt); ctx.lineTo(x + tt * 0.8, y + tt) }
                1 -> { ctx.moveTo(x + tt, y); ctx.lineTo(x - tt * 0.8, y - tt); ctx.lineTo(x - tt * 0.8, y + tt) }
                else -> { ctx.moveTo(x, y - tt); ctx.lineTo(x - tt, y + tt * 0.7); ctx.lineTo(x + tt, y + tt * 0.7) }
            }
            ctx.closePath(); ctx.fill()
        }
        val p = 4 * dpr
        tri(cxL, cy + (if (game.kL) p else 0.0), -1); tri(cxR, cy + (if (game.kR) p else 0.0), 1); tri(cxJ, cy - r * 0.2 + (if (game.kJ) p else 0.0), 0)
        ctx.font = "${(r * 0.2).toInt()}px 'Press Start 2P', monospace"; ctx.textAlign = "center"
        ctx.fillText(game.L("ПРЫЖОК", "SAKRASH"), cxJ, cy + r * 0.55 + (if (game.kJ) p else 0.0))
        ctx.globalAlpha = 1.0
    }
}

fun main() {
    val fonts = document.fonts
    if (fonts != null) {
        fonts.load("8px 'Press Start 2P'").then({ WebHost.start() }, { WebHost.start() })
    } else WebHost.start()
}
