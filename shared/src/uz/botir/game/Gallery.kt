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
                val es = listOf('g', 'k', 's', 'c', 'b', 'z', 'h', 'u', 'o', 't', 'a')
                es.forEachIndexed { i, e ->
                    for (k in 0 until 4) { G.c.save(); G.c.translate(10f + k * 40f + (i / 6) * 0f, 6f + i * 24f); G.c.scale(1f, 1f); G.drawParts(Spr.enemy(e, k), 0f); G.c.restore() }
                }
            }
            3 -> {   // townsfolk and items, 2x
                G.c.save(); G.c.scale(2f, 2f)
                val ppl = listOf(Npc(0, 0, "#2f7d4a", "#f2f2f2", scarf = true, beard = true), Npc(0, 0, "#7b3fa0", "#f2f2f2", scarf = true), Npc(0, 0, "#d8373b", "#f4c430", atlas = true, scarf = true),
                    Npc(0, 0, "#2457c5", "#15151c", wave = true), Npc(0, 0, "#5b3a8a", "#1c1410", glasses = true), Npc(0, 0, "#f5f0e6", "#15151c", arm = 12), Npc(0, 0, "#2b2f3a", "#2457c5"), Npc(0, 0, "#1f5aa8", "#f5f5f5", arm = 6))
                ppl.forEachIndexed { i, n -> Spr.person(4f + (i % 4) * 20f, 10f + (i / 4) * 32f, n.body, n.hat, n.atlas, n.scarf, n.beard, n.glasses, if (n.wave) (if (frame % 30 < 15) 2 else 6) else n.arm) }
                listOf("non", "somsa", "doppi", "qozon", "kafgir", "lagan", "pilla", "kosa", "chiganoq").forEachIndexed { i, k -> Spr.item(k, 4f + (i % 4) * 21f, 80f + (i / 4) * 16f) }
                G.c.restore()
            }
            4 -> {   // bosses
                for (k in 0 until 3) { G.c.save(); G.c.translate(14f + k * 56f, 30f); G.c.scale(1.2f, 1.2f); G.drawParts(Foes.boss(k, frame, false), 0f); G.c.restore() }
                listOf('G', 'A', 'K', 'V', 'P', 'S', 'T', 'W', 'L').forEachIndexed { i, m -> G.c.save(); G.c.translate(14f + (i % 3) * 56f, 110f + (i / 3) * 50f); G.c.scale(1.2f, 1.2f); G.drawParts(Foes.mini(m, frame, 0), 0f); G.c.restore() }
            }
        }
    }
}
