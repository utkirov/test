package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt

/** Developer art sheet (web: ?gallery=N). Shows animations frame by frame for review. */
object Gallery {
    fun draw(g: Game, frame: Int, page: Int) {
        Plat.gfx.identity(); G.c.scale(3f, 3f)
        px("#5a6a8a", 0, 0, W, H)
        when (page) {
            0 -> {
                val anims = listOf("idle", "run", "jump", "fall", "land", "hurt", "swim", "hang", "dance", "win")
                anims.forEachIndexed { i, a ->
                    val y = 4 + i * 26
                    txt(a, 2f, y + 8f, "#ffffff", SM, 0)
                    for (k in 0 until 8) {
                        G.c.save(); G.c.translate(34f + k * 18f, y.toFloat() - 6f); G.c.scale(0.75f, 0.75f)
                        val t = if (a == "run") 0 else k * 8
                        Hero.draw(a, t, g.skin(), false, k / 8f); G.c.restore()
                    }
                }
            }
            1 -> {
                G.c.save(); G.c.scale(2f, 2f)
                listOf("idle", "run", "jump").forEachIndexed { i, a -> G.c.save(); G.c.translate(4f + i * 28f, 4f); Hero.draw(a, frame, g.skin(), false, (frame % 48) / 48f); G.c.restore() }
                Skins.ALL.forEachIndexed { i, s -> G.c.save(); G.c.translate(4f + (i % 3) * 28f, 44f + (i / 3) * 36f); Hero.draw("idle", frame, s, false); G.c.restore() }
                G.c.restore()
            }
            2 -> {
                val es = listOf('g', 'k', 's', 'c', 'b', 'z', 'h')
                es.forEachIndexed { i, e ->
                    for (k in 0 until 4) { G.c.save(); G.c.translate(10f + k * 40f, 6f + i * 36f); G.c.scale(1.5f, 1.5f); G.drawParts(Spr.enemy(e, k), 0f); G.c.restore() }
                }
            }
        }
    }
}
