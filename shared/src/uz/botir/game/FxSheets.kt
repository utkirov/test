package uz.botir.game

/**
 * Sprite-sheet effects exported from pixelfx (https://github.com/patch-gg/pixelfx, MIT License,
 * Copyright (c) 2026 Patch, Copyright (c) 2026 Hugo Duprez). Pictures live in fx/<name>.png.
 * w×h = frame size, (px,py) = pivot inside a frame, frames laid out in [cols] columns.
 */
class FxSheet(val name: String, val w: Int, val h: Int, val cols: Int, val n: Int, val fps: Int, val px: Int, val py: Int, val loop: Boolean)

class Vfx(val s: FxSheet, val x: Float, val y: Float, val flip: Boolean, val start: Int, val screen: Boolean = false)

object FxLib {
    val ALL = mapOf(
        "coin" to FxSheet("coin", 74, 82, 13, 42, 30, 34, 78, false),
        "egg" to FxSheet("egg", 73, 77, 14, 42, 30, 32, 74, false),
        "poof" to FxSheet("poof", 82, 72, 12, 49, 30, 37, 62, false),
        "hurt" to FxSheet("hurt", 113, 66, 9, 16, 30, 26, 55, false),
        "bosshit" to FxSheet("bosshit", 117, 64, 8, 16, 30, 26, 53, false),
        "respawn" to FxSheet("respawn", 118, 148, 8, 39, 15, 59, 136, false),
        "confetti" to FxSheet("confetti", 190, 92, 5, 69, 20, 95, 81, false),
        "splash" to FxSheet("splash", 127, 98, 8, 51, 20, 67, 78, false)
    )
}
