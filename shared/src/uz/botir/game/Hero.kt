package uz.botir.game

import uz.botir.game.G.px
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Botir drawn from a small pixel skeleton: two-segment arms and legs, a breathing torso and an expressive head.
 * Local box is 24×32 facing right, feet at y=31. Angles are in degrees from straight down; positive points forward.
 */
object Hero {
    class Pose(
        var thF: Float = 4f, var shF: Float = -2f, var thB: Float = -4f, var shB: Float = 2f,
        var auF: Float = 8f, var alF: Float = 14f, var auB: Float = -8f, var alB: Float = 10f,
        var bob: Int = 0, var headDx: Int = 0, var eyes: Int = 0 /*0 open,1 blink,2 x,3 happy*/, var mouth: Int = 0 /*0 smile,1 open,2 ouch,3 grin*/,
        var sx: Float = 1f, var sy: Float = 1f, var lean: Float = 0f
    )

    private val SKIN = "#e8ad7e"; private val SKIN_S = "#c98a5e"; private val SKIN_H = "#f6c79c"; private val HAIR = "#1c1410"

    /** anim: idle, run, jump, fall, land, hurt, swim, hang, dance, win. t = frames in anim, ph = run phase (0..1). */
    fun pose(anim: String, t: Int, ph: Float = 0f, vy: Float = 0f): Pose {
        val p = Pose()
        val blink = (t % 190) < 6
        p.eyes = if (blink) 1 else 0
        when (anim) {
            "idle" -> { p.bob = if ((t / 36) % 2 == 0) 0 else 1; p.auF = 8f + p.bob * 3; p.auB = -8f - p.bob * 3 }
            "run" -> {
                val q = floor(ph * 8f) / 8f
                val a = (q * 2 * PI).toFloat()
                val s = sin(a); val c = cos(a)
                p.thF = 40f * s; p.shF = p.thF - (12f + 60f * max(0f, -c))
                p.thB = -40f * s; p.shB = p.thB - (12f + 60f * max(0f, c))
                p.auF = -42f * s; p.alF = p.auF + 75f
                p.auB = 42f * s; p.alB = p.auB + 75f
                p.bob = if (abs(s) > 0.7f) 1 else 0
                p.headDx = 1; p.lean = 1f
            }
            "jump" -> { p.thF = 70f; p.shF = 0f; p.thB = -15f; p.shB = -85f; p.auF = 115f; p.alF = 150f; p.auB = -150f; p.alB = -170f; p.mouth = 1; p.sy = 1.06f; p.sx = 0.95f }
            "fall" -> {
                val fl = if ((t / 6) % 2 == 0) 0f else 12f
                p.thF = 25f; p.shF = 8f; p.thB = -18f; p.shB = -30f; p.auF = 105f + fl; p.alF = 120f + fl; p.auB = -100f - fl; p.alB = -115f - fl; p.mouth = 1
            }
            "land" -> { val k = if (t < 4) 1f else 0.5f; p.thF = 45f * k; p.shF = -40f * k; p.thB = 30f * k; p.shB = -45f * k; p.auF = 35f; p.alF = 50f; p.auB = -30f; p.alB = -10f
                p.sy = if (t < 4) 0.84f else 0.93f; p.sx = if (t < 4) 1.12f else 1.05f; p.bob = 0 }
            "hurt" -> { p.thF = 20f; p.shF = 30f; p.thB = -10f; p.shB = 10f; p.auF = 130f; p.alF = 150f; p.auB = -130f; p.alB = -150f; p.eyes = 2; p.mouth = 2; p.lean = -1f; p.headDx = -1 }
            "swim" -> {
                val a = (t / 7f); val s = sin(a); val c = cos(a)
                p.thF = 25f + 25f * s; p.shF = p.thF - 70f; p.thB = -10f - 25f * s; p.shB = p.thB - 60f
                p.auF = 95f + 55f * c; p.alF = p.auF + 20f; p.auB = -60f - 40f * c; p.alB = p.auB - 20f; p.mouth = 1; p.headDx = 1
            }
            "hang" -> { val sw = 12f * sin(t / 9f); p.thF = 10f + sw; p.shF = 5f + sw; p.thB = -6f + sw; p.shB = -12f + sw; p.auF = 172f; p.alF = 178f; p.auB = 188f; p.alB = 182f; p.mouth = 3 }
            "dance" -> {
                val up = (t / 15) % 2 == 0
                p.auF = if (up) 160f else 40f; p.alF = if (up) 190f else 90f; p.auB = if (up) -40f else -160f; p.alB = if (up) -90f else -190f
                p.thF = if (up) 30f else 0f; p.shF = if (up) -30f else 0f; p.thB = if (up) 0f else -30f; p.shB = if (up) 0f else 20f
                p.eyes = 3; p.mouth = 3; p.bob = if (up) 0 else 1
            }
            "win" -> { val w = if ((t / 10) % 2 == 0) 10f else -10f; p.auF = 165f + w; p.alF = 175f + w; p.auB = -165f + w; p.alB = -175f + w; p.eyes = 3; p.mouth = 3 }
        }
        return p
    }

    private fun seg(x0: Float, y0: Float, ang: Float, len: Float): FloatArray {
        val r = ang * PI.toFloat() / 180f
        return floatArrayOf(x0 + sin(r) * len, y0 + cos(r) * len)
    }

    private fun stamp(c: String, x0: Float, y0: Float, x1: Float, y1: Float, w: Int) {
        val n = max(abs(x1 - x0), abs(y1 - y0)).roundToInt().coerceAtLeast(1)
        val h = w / 2
        for (k in 0..n) {
            val x = (x0 + (x1 - x0) * k / n).roundToInt(); val y = (y0 + (y1 - y0) * k / n).roundToInt()
            px(c, x - h, y - h, w, w)
        }
    }

    /** Draws one limb (two segments) with outline; returns the end point. */
    private fun limb(x: Float, y: Float, a1: Float, l1: Float, a2: Float, l2: Float, col: String, end: String, endW: Int, foot: Boolean, sk: Skin?) {
        val j = seg(x, y, a1, l1); val e = seg(j[0], j[1], a2, l2)
        stamp(OUT, x, y, j[0], j[1], 5); stamp(OUT, j[0], j[1], e[0], e[1], 5)
        if (foot) { val fx = e[0].roundToInt(); val fy = e[1].roundToInt(); px(OUT, fx - 3, fy - 1, 7, 4) }
        else { val ex = e[0].roundToInt(); val ey = e[1].roundToInt(); px(OUT, ex - 2, ey - 2, endW + 2, endW + 2) }
        stamp(col, x, y, j[0], j[1], 3); stamp(col, j[0], j[1], e[0], e[1], 3)
        if (foot && sk != null) {
            val fx = e[0].roundToInt(); val fy = e[1].roundToInt()
            px(sk.shoe, fx - 2, fy, 5, 2); px(sk.stripe, fx - 1, fy, 3, 1); px(G.shade(sk.shoe, 0.6f), fx - 2, fy + 1, 5, 1)
        } else { val ex = e[0].roundToInt(); val ey = e[1].roundToInt(); px(end, ex - 1, ey - 1, endW, endW) }
    }

    fun draw(anim: String, t: Int, sk: Skin, gold: Boolean, ph: Float = 0f, vy: Float = 0f) = drawPose(pose(anim, t, ph, vy), sk, gold)

    fun drawPose(p: Pose, sk: Skin, gold: Boolean) {
        val c = G.c
        c.save()
        if (p.sx != 1f || p.sy != 1f) { c.translate(12f, 31f); c.scale(p.sx, p.sy); c.translate(-12f, -31f) }
        val b = p.bob.toFloat()
        val body = sk.body; val bodyD = G.shade(body, 0.72f); val pants = sk.pants; val pantsD = G.shade(pants, 0.72f)
        val hipY = 24f + b * 0.5f; val shY = 16f + b
        // back limbs (darker)
        limb(10.5f, shY, p.auB, 4f, p.alB, 4f, bodyD, SKIN_S, 2, false, null)
        limb(10.5f, hipY, p.thB, 4f, p.shB, 3.6f, pantsD, "", 0, true, sk)
        // backpack
        if (sk.pack) { px(OUT, 2, 14 + b, 6, 11); px("#2e9b6b", 3, 15 + b, 4, 9); px("#1f6e4b", 3, 19 + b, 4, 1); px("#f4c430", 4, 20 + b, 1, 1); px("#55c48c", 3, 15 + b, 1, 8) }
        // torso
        c.save(); c.translate(p.lean, b)
        px(OUT, 6, 14, 12, 12)
        px(body, 7, 15, 10, 10); sk.torso(Part(7f, 15f, 10f, 10f, body)); px(G.shade(body, 0.8f), 7, 15, 1, 10); px("#22ffffff", 16, 15, 1, 9)
        if (sk.hood) { px(OUT, 5, 12, 8, 5); px(body, 6, 13, 6, 3); px(bodyD, 6, 15, 6, 1) }
        c.restore()
        // head
        c.save(); c.translate(p.headDx.toFloat() * 0.5f + p.lean, b)
        head(p, sk, gold)
        c.restore()
        // front limbs
        limb(13.5f, hipY, p.thF, 4f, p.shF, 3.6f, pants, "", 0, true, sk)
        limb(14f + p.lean, shY, p.auF, 4f, p.alF, 4f, body, SKIN, 2, false, null)
        c.restore()
    }

    private fun head(p: Pose, sk: Skin, gold: Boolean) {
        val doppi = if (gold) "#f4c430" else sk.doppi; val dw = if (gold) "#b8860b" else sk.dw
        // outline + face
        px(OUT, 7, 4, 11, 11); px(OUT, 17, 8, 2, 4)
        px(SKIN, 8, 5, 9, 9); px(SKIN_H, 13, 6, 3, 1); px(SKIN, 17, 9, 1, 2)
        // hair: back of head, sideburn, fringe
        px(HAIR, 8, 5, 3, 6); px(HAIR, 8, 11, 2, 1); px(HAIR, 11, 5, 5, 1); px("#3a2a20", 9, 6, 1, 3)
        // ear
        px(SKIN_S, 10, 8, 2, 3); px(SKIN, 10, 9, 1, 1)
        // eyebrow + eye
        px(HAIR, 13, 7, 3, 1)
        when (p.eyes) {
            1 -> px("#2a1810", 14, 9, 2, 1)
            2 -> { px("#000000", 14, 8, 1, 1); px("#000000", 15, 9, 1, 1); px("#000000", 14, 10, 1, 1); px("#000000", 16, 8, 1, 1); px("#000000", 16, 10, 1, 1) }
            3 -> { px("#2a1810", 14, 9, 1, 1); px("#2a1810", 15, 8, 1, 1); px("#2a1810", 16, 9, 1, 1) }
            else -> { px("#ffffff", 14, 8, 2, 2); px("#2a1810", 15, 8, 1, 2) }
        }
        px("#ef8f80", 13, 11, 2, 1)                               // cheek
        when (p.mouth) {
            1 -> { px("#6a1f18", 15, 11, 2, 2); px("#c04a3a", 15, 12, 2, 1) }
            2 -> { px("#6a1f18", 14, 12, 1, 1); px("#6a1f18", 15, 11, 1, 1); px("#6a1f18", 16, 12, 1, 1) }
            3 -> { px("#6a1f18", 14, 11, 3, 1); px("#ffffff", 15, 11, 1, 1); px("#6a1f18", 15, 12, 2, 1) }
            else -> { px("#8a3324", 15, 12, 2, 1); px("#8a3324", 16, 11, 1, 1) }
        }
        px(SKIN_S, 9, 13, 7, 1)                                   // jaw shade
        // doppi (chust style): black square cap, white pepper motifs, white stitched edge
        px(OUT, 6, 1, 13, 6)
        px(doppi, 7, 2, 11, 4); px(G.shade(doppi, 1.5f), 9, 2, 5, 1)
        px(dw, 9, 3, 2, 2); px(doppi, 10, 4, 1, 1); px(dw, 11, 3, 1, 1)
        px(dw, 14, 3, 2, 2); px(doppi, 15, 4, 1, 1); px(dw, 16, 3, 1, 1)
        px(G.mix(doppi, dw, 0.5f), 7, 5, 11, 1)
        if (sk.season == Season.WINTER && !gold) { px(OUT, 11, -2, 5, 4); px("#ffffff", 12, -1, 3, 3) }
    }
}
