package uz.botir.game

import uz.botir.game.G.px

/**
 * Detailed 16×16 tile painters per world style (= LevelDef.bg). Ground top tiles are 16×20 so grass can overhang.
 * Painted once into the layer cache by Game.drawTiles.
 */
object Tiles {
    private fun sh(c: String, f: Float) = G.shade(c, f)

    /** Ground tile. When [top] the picture is 16×20 with the surface starting at y=4. */
    fun ground(st: Int, th: Theme, v: Int, top: Boolean, l: Boolean, r: Boolean) {
        val oy = if (top) 4 else 0
        val body = th.body
        // fill texture
        px(body, 0, oy, 16, 16)
        when (st) {
            1, 7 -> { // packed earth / sandstone blocks
                val m = sh(body, 0.82f)
                for (row in 0 until 4) { val y = oy + row * 4; px(m, 0, y + 3, 16, 1); val o = if (row % 2 == 0) 0 else 6; px(m, o + 5, y, 1, 3); px(m, (o + 13) % 16, y, 1, 3) }
                px(sh(body, 1.12f), 1, oy + 1, 3, 1)
            }
            2 -> { // marble blocks
                px(sh(body, 1.15f), 0, oy, 16, 1); px(sh(body, 0.8f), 0, oy + 15, 16, 1); px(sh(body, 0.85f), 15, oy, 1, 16)
                px(sh(body, 1.25f), 2 + v * 2, oy + 4, 5, 1); px(sh(body, 1.25f), 6 + v, oy + 5, 1, 4)
            }
            4, 5 -> { // concrete
                px(sh(body, 0.88f), 0, oy + 15, 16, 1); px(sh(body, 0.88f), 15, oy, 1, 16)
                for (k in 0 until 4) { val h = hash(v * 7 + k, st); px(sh(body, if (k % 2 == 0) 0.8f else 1.1f), h % 14, oy + (h shr 4) % 14, 2, 1) }
            }
            6 -> { // sand brick with mosaic band
                val m = sh(body, 0.82f)
                for (row in 0 until 4) { val y = oy + row * 4; px(m, 0, y + 3, 16, 1); val o = if (row % 2 == 0) 0 else 8; px(m, o + 3, y, 1, 3) }
                if (v == 0) { px("#2e6fd8", 0, oy + 7, 16, 2); for (k in 0 until 4) px("#2fb3c9", k * 4 + 1, oy + 7, 2, 2) }
            }
            10 -> { // red Rishtan clay with pottery shards
                if (v % 2 == 0) { val h = hash(v * 13, 10); val sx = h % 12; val sy = oy + 5 + (h shr 5) % 9
                    px(if (v == 0) "#2fb3c9" else "#f5f0e6", sx, sy, 3, 2); px("#1f5aa8", sx, sy + 1, 3, 1) }
                for (k in 0 until 3) { val h = hash(v * 7 + k, 12); px(sh(body, 0.8f), h % 14, oy + 4 + (h shr 4) % 11, 2, 1) }
                px(sh(body, 0.8f), 0, oy + 11, 16, 1); px(sh(body, 1.15f), (v * 5) % 12, oy + 3, 3, 1)
            }
            11 -> { // dry Aral seabed: layered silt with shells
                px(sh(body, 0.88f), 0, oy + 6, 16, 2); px(sh(body, 0.8f), 0, oy + 12, 16, 1); px(sh(body, 1.12f), 0, oy + 9, 16, 1)
                val h = hash(v, 11); px("#f2d6c4", h % 12, oy + 3 + (h shr 4) % 8, 2, 1); px("#c99a8a", h % 12, oy + 4 + (h shr 4) % 8, 2, 1)
            }
            8 -> { // adobe with straw
                for (k in 0 until 6) { val h = hash(v * 11 + k, 8); px(sh(body, 1.15f), h % 14, oy + (h shr 4) % 15, 3, 1) }
                px(sh(body, 0.8f), 0, oy + 8, 16, 1)
            }
            else -> { // soil with stones and roots
                for (k in 0 until 3) { val h = hash(v * 13 + k, st + 1); val sx = h % 13; val sy = oy + 5 + (h shr 5) % 9
                    px(sh(body, 0.75f), sx, sy + 1, 3, 2); px(sh(body, 1.25f), sx, sy, 3, 2); px(sh(body, 1.45f), sx, sy, 1, 1) }
                px(th.dot, (v * 5) % 14, oy + 12, 2, 2)
            }
        }
        if (l) px(sh(body, 0.75f), 0, oy, 1, 16)
        if (r) px(sh(body, 0.75f), 15, oy, 1, 16)
        if (!top) return
        // surface
        when (st) {
            11 -> { // white salt crust with cracks
                px("#efe9da", 0, 4, 16, 4); px("#ffffff", 0, 4, 16, 1); px("#c9c2b0", 0, 8, 16, 1)
                px("#c9c2b0", 3 + v, 5, 1, 2); px("#c9c2b0", 4 + v, 6, 3, 1); px("#c9c2b0", 11 - v, 5, 1, 3)
                if (v == 2) { px("#efe9da", 6, 2, 3, 2); px("#ffffff", 7, 2, 1, 1) }
            }
            0, 3, 9, 10 -> { // grass with blades and flowers
                px(th.top, 0, 4, 16, 5); px(th.top2, 0, 4, 16, 2); px(sh(th.top, 0.7f), 0, 9, 16, 1)
                for (k in 0 until 8) { val h = hash(v * 17 + k, 3); val bx = h % 16; val bh = 1 + (h shr 4) % 3; px(if (k % 2 == 0) th.top2 else th.top, bx, 4 - bh, 1, bh) }
                for (k in 0 until 3) { val h = hash(v * 5 + k, 9); px(sh(th.top, 0.75f), h % 15, 9, 2, 1 + (h shr 6) % 2) }
                if (st == 3 || v == 1) { val c = if (v % 2 == 0) "#f7c6d9" else "#fff1a0"; val fx = 3 + v * 3; px("#3d7a2c", fx, 1, 1, 3); px(c, fx - 1, 0, 3, 1); px(c, fx, -1 + 1, 1, 1) }
            }
            1 -> { // cobblestones
                px(sh(th.top, 0.7f), 0, 4, 16, 5)
                for (k in 0 until 4) { val cx = k * 4 + (v % 2) * 2; px(th.top, cx, 4, 3, 2); px(th.top2, cx, 4, 2, 1); px(sh(th.top, 0.85f), cx + 1, 6, 3, 2) }
                px(sh(th.top, 0.6f), 0, 9, 16, 1)
            }
            2 -> { // polished granite with tactile yellow strip
                px("#7a3a3a", 0, 4, 16, 4); px("#9a5a5a", 0, 4, 16, 1); px("#5a2a2a", 0, 8, 16, 1)
                if (v < 2) { px("#f4c430", 0, 5, 16, 2); for (k in 0 until 4) px("#c9a227", k * 4 + 1, 5, 2, 1) }
            }
            4 -> { px("#9aa8b8", 0, 4, 16, 3); px("#c9d3de", 0, 4, 16, 1); px("#5f7287", 0, 7, 16, 1); for (k in 0 until 2) px("#6f849b", 3 + k * 8, 5, 1, 1) }
            5 -> { px("#b9c2d2", 0, 4, 16, 4); px("#e0e6ee", 0, 4, 16, 1); px("#6b7280", 0, 8, 16, 1); if (v == 0) px("#f4c430", 2, 5, 12, 1) }
            6 -> { // turquoise tile cap with white diamonds
                px("#1f8aa0", 0, 4, 16, 5); px("#2fb3c9", 0, 4, 16, 3); px("#86e0ee", 0, 4, 16, 1)
                for (k in 0 until 2) { val dx = k * 8 + 3; px("#f5f0e6", dx + 1, 5, 1, 1); px("#f5f0e6", dx, 6, 3, 1); px("#f5f0e6", dx + 1, 7, 1, 1) }
                px("#14606e", 0, 9, 16, 1)
            }
            7 -> { px(th.top, 0, 4, 16, 4); px(th.top2, 0, 4, 16, 1); px(sh(th.top, 0.75f), (v * 4) % 12, 4, 1, 4); px(sh(th.top, 0.7f), 0, 8, 16, 1) }
            8 -> { px(th.top, 0, 4, 16, 4); px(th.top2, 0, 4, 16, 1); px(sh(th.top, 0.7f), 0, 8, 16, 1); if (v == 2) { px(th.top, 5, 1, 6, 3); px(th.top2, 5, 1, 6, 1) } }
        }
    }

    fun wall(st: Int, th: Theme, ty: Int, top: Boolean) {
        val w = th.wall; val m = th.mortar
        px(w, 0, 0, 16, 16)
        when (st) {
            2 -> { px(sh(w, 1.2f), 0, 0, 16, 1); px(sh(w, 0.8f), 0, 15, 16, 1); px(sh(w, 1.3f), 3, 5, 6, 1); px(sh(w, 1.3f), 8, 6, 1, 5); px("#c9a227", 0, 7, 16, 1) }
            6 -> { // glazed tiles
                for (q in 0 until 2) for (r in 0 until 2) { val x = q * 8; val y = r * 8; px(if ((q + r) % 2 == 0) "#2e6fd8" else "#2fb3c9", x, y, 8, 8); px("#f5f0e6", x + 3, y + 1, 2, 6); px("#f5f0e6", x + 1, y + 3, 6, 2); px("#f4c430", x + 3, y + 3, 2, 2) }
                px("#14606e", 0, 15, 16, 1)
            }
            10 -> { // Rishtan glazed tiles: white field, cobalt & turquoise flower
                px("#f5f0e6", 0, 0, 16, 16); px("#1f5aa8", 0, 0, 16, 1); px("#1f5aa8", 0, 15, 16, 1); px("#1f5aa8", 0, 0, 1, 16); px("#1f5aa8", 15, 0, 1, 16)
                px("#2fb3c9", 7, 2, 2, 12); px("#2fb3c9", 2, 7, 12, 2); px("#1f5aa8", 5, 5, 6, 6); px("#f5f0e6", 6, 6, 4, 4); px("#d8373b", 7, 7, 2, 2)
                for (c in arrayOf(2 to 2, 12 to 2, 2 to 12, 12 to 12)) px("#2e9b6b", c.first, c.second, 2, 2)
            }
            11 -> { // rusted ship hull plates with rivets
                px("#8a4a2a", 0, 0, 16, 16); px("#a0582f", 1, 1, 14, 6); px("#7a3a1e", 0, 7, 16, 1); px("#a0582f", 1, 8, 14, 7)
                for (k in 0 until 4) { px("#c9a68a", 2 + k * 4, 2, 1, 1); px("#c9a68a", 2 + k * 4, 10, 1, 1) }
                val h = hash(ty, 11); px("#c0703a", h % 12, 3 + (h shr 4) % 3, 3, 1); px("#5a2a14", (h shr 3) % 13, 12, 2, 2)
            }
            8 -> { for (k in 0 until 6) { val h = hash(ty * 3 + k, 8); px(sh(w, 1.12f), h % 14, (h shr 4) % 15, 3, 1) }; px(sh(w, 0.82f), 0, 8, 16, 1) }
            else -> {
                for (row in 0 until 3) {
                    val y = row * 5 + 1; val o = if ((row + ty) % 2 == 0) 0 else 4
                    px(m, 0, y + 4, 16, 1)
                    var x = -o; while (x < 16) { px(m, x + 7, y, 1, 4); px(sh(w, 1.12f), x + 1, y, 5, 1); px(sh(w, 0.9f), x + 1, y + 3, 6, 1); x += 8 }
                }
            }
        }
        if (top) { px(sh(w, 0.7f), 0, 0, 16, 2); px(sh(w, 1.2f), 0, 0, 16, 1) }
    }

    /** One-way platform; picture is 16×8 drawn from y-1. */
    fun plank(st: Int, th: Theme, v: Int) {
        px(OUT, 0, 0, 16, 7)
        when {
            th.awning -> { for (k in 0 until 4) px(if (k % 2 == 1) th.plank2 else th.plank, k * 4, 1, 4, 5); px("#33000000", 0, 5, 16, 1); for (k in 0 until 4) px(if (k % 2 == 1) th.plank2 else th.plank, k * 4 + 1, 6, 2, 1) }
            st == 2 -> { px("#c9a227", 0, 1, 16, 5); px("#f4dc7a", 0, 1, 16, 1); px("#8a6a10", 0, 5, 16, 1); px("#8a6a10", 7, 2, 1, 3) }
            st == 4 || st == 5 -> { px("#9aa1ab", 0, 1, 16, 5); px("#d6dbe2", 0, 1, 16, 1); for (k in 0 until 4) px("#5f6b80", k * 4 + 1, 3, 2, 1); px("#5f6b80", 0, 5, 16, 1) }
            st == 11 -> { px("#8a4a2a", 0, 1, 16, 5); px("#c0703a", 0, 1, 16, 1); px("#5a2a14", 0, 5, 16, 1); for (k in 0 until 3) px("#c9a68a", 2 + k * 6, 3, 1, 1); px("#d98a4a", 5 + v * 3, 2, 2, 1) }
            st == 6 -> { px("#8b5a2b", 0, 1, 16, 5); px("#b07a45", 0, 1, 16, 1); px("#2fb3c9", 0, 3, 16, 1); px("#f5f0e6", 3 + v * 3, 3, 1, 1); px("#5a3a1a", 0, 5, 16, 1) }
            else -> { px(th.plank, 0, 1, 16, 5); px(th.plank2, 0, 1, 16, 1); px(sh(th.plank, 0.75f), 0, 5, 16, 1); px(sh(th.plank, 0.8f), 7, 1, 1, 5)
                px("#c9ced6", 2, 3, 1, 1); px("#c9ced6", 12, 3, 1, 1); px(sh(th.plank, 1.15f), 3 + v, 2, 3, 1) }
        }
    }
}
