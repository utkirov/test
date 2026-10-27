package uz.botir.game

import uz.botir.game.G.px
import uz.botir.game.G.txt
import kotlin.math.abs
import kotlin.math.sin

private fun a(ru: String, uz: String) = arrayOf(ru, uz)

/** The six Tashkent locations. Each has its own signature mechanic. */
fun Game.buildLevels(): List<LevelDef> = listOf(
    // 1 ─ MAHALLA: trampolines (ko'rpacha) + gas pipes
    LevelDef(a("Махалля", "Mahalla"),
        a("Жадина-ака украл казан дедушки! Без него не будет плова на тое. Найди, куда убежала банда.", "Ochko'z aka bobomning qozonini o'g'irladi! Qozonsiz to'yda osh bo'lmaydi. To'da qayerga qochganini top."),
        a("Механика: курпачи-батуты подбрасывают высоко, по жёлтым газовым трубам можно бегать.", "Mexanika: ko'rpacha-batutlar baland otadi, sariq gaz quvurlarida yugurish mumkin."),
        0, Theme("#4f9a3a", "#7cc44a", "#b9773d", "#a5672f", "#d9b98a", "#b8956a", "#8b5a2b", "#b07a45"),
        build = { b -> newMap(130, 17); with(b) {
            G(0, 20, 14); N(5, 9, 12); E('g', 17, 13); N(22, 22, 11)
            G(24, 40, 14); P(27, 30, 11); N(27, 30, 10); P(33, 33, 13, 'J'); N(32, 34, 5); WB(36, 37, 12, 13); E('g', 39, 13)
            G(41, 55, 12); I('s', 43, 11); N(46, 48, 10); E('k', 47, 11); P(57, 58, 10); N(57, 58, 9)
            G(60, 80, 14); P(61, 61, 13, 'J'); P(63, 66, 11); P(68, 71, 8); I('d', 70, 7); N(68, 69, 7); E('g', 66, 13); N(73, 77, 12); E('g', 79, 13)
            G(81, 95, 13); WB(84, 84, 11, 12); N(82, 83, 11); E('k', 88, 12); WB(90, 93, 5, 9); N(91, 93, 12); N(96, 98, 10)
            G(99, 129, 14); N(100, 103, 11); P(105, 105, 13, 'J'); P(108, 112, 11, 'p'); N(108, 112, 10); E('g', 110, 13); goal(120, 11)
            throwers.add(Thrower(12f * T + 2, 7f * T, 9f * T, "buvi"))
            Z("qozon", a("Пустой казан деда", "Bobomning bo'sh qozoni"), a("Пустой казан деда. Записка: «Скоро вернусь. Жадина-ака»", "Bobomning bo'sh qozoni. Xat: «Tez qaytaman. Ochko'z aka»"), 5, 12, 2, 2, "touch")
            Z("buvi", a("Самса от бабушки", "Buvining somsasi"), a("Бабушка с балкона: «Bolam, ye! Совсем худой стал!»", "Buvi balkondan: «Bolam, ye! Juda ozib ketibsan!»"), 0, 0, 0, 0, "item")
            Z("lazgi", a("Танец лазги", "Lazgi raqsi"), a("Лазги! Ботир не удержался и пустился в пляс.", "Lazgi! Botir chiday olmay raqsga tushdi."), 29, 12, 5, 2, "still", "dance")
            Z("qovun", a("Торг за дыню", "Qovun savdosi"), a("Продавец: «Ладно! Бери дыню даром, только не прыгай!» +1 жизнь", "Sotuvchi: «Mayli! Qovunni tekin ol, faqat sakrama!» +1 jon"), 50, 9, 6, 3, "jump", "hp")
            Z("damas", a("Набитый Дамас", "Liq to'la Damas"), a("Дамас на 12 человек, с ковром и казаном на крыше. Всех обгоняет!", "12 kishilik Damas, tomida gilam va qozon. Hammani quvib o'tadi!"), 56, 0, 1, 17, "pass", "veh:damas")
            Z("mushuk", a("Кот Мушук", "Mushuk"), a("Кот Мушук мурлычет и делится силой: +1 жизнь!", "Mushuk xurillaydi va kuch beradi: +1 jon!"), 64, 10, 2, 1, "touch", "hp")
            Z("metro", a("Станция Космонавтов", "Kosmonavtlar bekati"), a("Станция «Космонавтов»: невесомость! Прыгай как в космосе.", "«Kosmonavtlar» bekati: vaznsizlik! Koinotdagidek sakra."), 74, 12, 2, 2, "touch", "lowg")
            Z("ona", a("Звонок мамы", "Onamning qo'ng'irog'i"), a("Звонит мама: «Qayerdasan?! Плов без тебя стынет!»", "Onang qo'ng'iroq qilyapti: «Qayerdasan?! Osh sovib qolyapti!»"), 0, 0, 0, 0, "idle", "ring")
            Z("sharshara", a("Тайник за водопадом", "Sharshara ortidagi sir"), a("Тайник за водопадом! Тут прятались ноны.", "Sharshara ortidagi yashirin joy! Bu yerda nonlar yashiringan."), 91, 10, 3, 3, "touch")
            Z("tohir", a("Тахир и Зухра", "Tohir va Zuhra"), a("Тахир и Зухра машут тебе. Легенда о любви тоже из этих мест!", "Tohir va Zuhra senga qo'l silkitmoqda. Muhabbat afsonasi ham shu yerlardan!"), 103, 0, 1, 17, "pass")
            Z("quvur", a("Езда по газовой трубе", "Gaz quvurida yurish"), a("Жёлтые газовые трубы махалли — лучшие рельсы!", "Mahallaning sariq gaz quvurlari — eng zo'r relslar!"), 0, 0, 0, 0, "pipe")
            Z("nasriddin", a("Ходжа Насреддин", "Xo'ja Nasriddin"), a("Ходжа Насреддин: «Казан не потерялся — он просто ушёл в гости!»", "Xo'ja Nasriddin: «Qozon yo'qolmagan — u shunchaki mehmonga ketgan!»"), 114, 0, 1, 17, "pass")
            NPC(Npc(31, 14, "#d8373b", "#f4c430", atlas = true, note = 1, dancer = true)); NPC(Npc(54, 12, "#f5f0e6", "#15151c"))
            NPC(Npc(104, 14, "#2457c5", "#15151c", wave = true)); NPC(Npc(105, 14, "#d8373b", "#f4c430", atlas = true, scarf = true, wave = true, note = 2))
        } },
        back = {
            val bx = X(0); px("#e9d8b4", bx, Y(9), 64, 5 * T); px("#c4a87f", bx, Y(9) - 3, 64, 3); px("#3aa0c8", bx + 18, Y(10), 14, 14); txt(L("ДОМ ДЕДА", "BOBOM UYI"), bx + 4, Y(9) + 3, "#8a5422", SM)
            val hx = X(11); px("#e9d8b4", hx, Y(4), 64, 10 * T); px("#c4a87f", hx, Y(4) - 3, 64, 3); px("#3aa0c8", hx + 40, Y(10), 14, 16); px("#3aa0c8", hx + 8, Y(5), 14, 14)
            px("#8b5a2b", hx - 4, Y(7) + 6, 40, 3); for (i in 0 until 5) px("#8b5a2b", hx - 2 + i * 8, Y(7) - 6, 2, 12)
            Spr.person(hx + 6, Y(7) - 17, "#7b3fa0", "#f2f2f2", scarf = true, arm = if (throwers.firstOrNull()?.done == true) 7 else 12)
            val mx = X(74); val my = Y(12); px(OUT, mx - 2, my - 12, 36, 44); px("#c9ced6", mx - 1, my - 11, 34, 42); px("#2b2b33", mx + 4, my + 2, 24, 30)
            for (i in 0 until 5) px("#6b7280", mx + 4, my + 8 + i * 5, 24, 1); px("#2e6fd8", mx + 11, my - 21, 10, 9); txt("M", mx + 13, my - 20, "#ffffff", SM, shadow = false); txt("KOSMONAVTLAR", mx + 16, my - 31, "#2e6fd8", SM, 1)
            val sx = X(52); val sy = Y(12); px("#8b5a2b", sx, sy - 12, 26, 3); px("#6b3f1f", sx + 2, sy - 9, 2, 9); px("#6b3f1f", sx + 22, sy - 9, 2, 9)
            for (i in 0 until 3) { px(OUT, sx + 2 + i * 8, sy - 19, 8, 7); px("#e9c94a", sx + 3 + i * 8, sy - 18, 6, 5) }
            choyxona(X(115), Y(14))
        },
        front = { G.spr(Spr.QOZ, X(6) + 1, Y(13) + 9, Spr.PAL); cat(X(64) + 4, Y(11), !found.contains("mushuk")) },
        over = { waterfall(X(91) + 2, Y(5), 8) }),

    // 2 ─ CHORSU: crumbling melon crates
    LevelDef(a("Базар Чорсу", "Chorsu bozori"),
        a("Соседи видели банду на Чорсу. Под синим куполом шумит базар — берегись торговцев с мётлами!", "Qo'shnilar to'dani Chorsuda ko'rishgan. Ko'k gumbaz ostida bozor qaynayapti — supurgili sotuvchilardan ehtiyot bo'l!"),
        a("Механика: ящики с дынями ломаются под ногами. Беги, не останавливайся!", "Mexanika: qovun yashiklari oyog'ing ostida sinadi. To'xtamay yugur!"),
        1, Theme("#c9b48a", "#e2d1a8", "#a88a5c", "#8f7448", "#e9d8b4", "#c4a87f", "#d8373b", "#f5f0e6", awning = true),
        build = { b -> newMap(122, 17); with(b) {
            G(0, 14, 14); N(9, 12, 12); E('s', 12, 13, 2)
            P(15, 16, 14, 'C')
            G(17, 34, 14); P(19, 22, 11); N(19, 22, 10); P(24, 27, 8); N(24, 27, 7); E('b', 22, 8, 5); E('s', 31, 13, 2)
            G(35, 50, 14); WB(38, 39, 12, 13); P(41, 44, 10); P(45, 48, 7, 'C'); P(41, 43, 4); N(45, 48, 5); E('s', 47, 13, 2)
            P(51, 53, 14, 'C'); N(51, 53, 12)
            G(54, 70, 13); P(57, 59, 10); I('d', 58, 9); E('k', 61, 12, 3); P(65, 66, 10, 'C'); P(67, 68, 7); I('x', 68, 6, "sabzi")
            G(71, 90, 14); WB(76, 76, 12, 13); E('s', 81, 13, 3); E('b', 84, 9, 5); N(78, 82, 11); I('s', 87, 12)
            P(91, 93, 14, 'C'); P(91, 93, 10, 'C'); N(91, 93, 9)
            G(94, 121, 14); E('g', 99, 13, 3); E('s', 105, 13, 3); N(96, 100, 11); goal(112, 11)
            Z("tandir", a("Горячий нон из тандыра", "Tandirdan issiq non"), a("Нонвой достал из тандыра горячий нон. +1 жизнь!", "Novvoy tandirdan issiq non uzdi. +1 jon!"), 4, 12, 4, 2, "still", "hp")
            Z("arava", a("«Пошт-пошт!»", "«Posht-posht!»"), a("«Пошт-пошт!» — тележка носильщика мчится сквозь толпу!", "«Posht-posht!» — aravakash olomon orasidan yelib o'tmoqda!"), 18, 0, 1, 17, "pass", "veh:arava")
            Z("kurt", a("Солёный курт", "Sho'r qurt"), a("Ботир попробовал курт. Солёный! Глаза на лоб!", "Botir qurtni tatib ko'rdi. Sho'r! Ko'zlari chaqchayib ketdi!"), 29, 12, 3, 2, "touch")
            Z("gumbaz", a("Под самым куполом", "Gumbaz cho'qqisida"), a("Ты под самым куполом Чорсу! Весь базар как на ладони.", "Chorsu gumbazining eng tepasidasan! Butun bozor kaftdagidek."), 41, 2, 3, 2, "touch")
            Z("dutor", a("Дутарист", "Dutorchi"), a("Дутарист заиграл, и Ботир пустился в пляс!", "Dutorchi chala boshladi, Botir raqsga tushdi!"), 63, 11, 3, 2, "still", "dance")
            Z("sabzi", a("Жёлтая морковь", "Sariq sabzi"), a("Жёлтая морковь! Без неё настоящего плова не бывает.", "Sariq sabzi! Usiz haqiqiy osh bo'lmaydi."), 0, 0, 0, 0, "item")
            NPC(Npc(7, 14, "#f5f0e6", "#15151c", arm = 7)); NPC(Npc(30, 14, "#7b3fa0", "#f2f2f2", scarf = true)); NPC(Npc(64, 13, "#2f7d4a", "#15151c", note = 1, dancer = true))
        } },
        back = {
            val tx = X(5); val ty = Y(14)
            G.halfDisc("#c98a4a", tx + 8, ty - 10, 11); px("#c98a4a", tx - 3, ty - 10, 22, 10); px("#3b2a20", tx + 4, ty - 14, 8, 6); if (frame % 20 < 10) px("#f08a24", tx + 6, ty - 12, 4, 3)
            txt("TANDIR", tx + 8, ty - 32, "#8a5422", SM, 1)
            val kx = X(29); val ky = Y(14); px("#8b5a2b", kx, ky - 10, 40, 3); px("#6b3f1f", kx + 2, ky - 7, 2, 7); px("#6b3f1f", kx + 36, ky - 7, 2, 7)
            for (i in 0 until 8) px("#f5f0e6", kx + 3 + i * 4, ky - 13, 3, 3); txt("QURT", kx + 20, ky - 25, "#8a5422", SM, 1)
            if (veh == "arava") {
                val x = vehX; val gy = Y(14); px(OUT, x - 1, gy - 17, 34, 12); px("#8b5a2b", x, gy - 16, 32, 10); for (i in 0 until 4) px("#e9c94a", x + 2 + i * 8, gy - 22, 7, 6)
                px("#222222", x + 4, gy - 6, 6, 6); px("#222222", x + 22, gy - 6, 6, 6); Spr.person(x - 12, gy - 18, "#2457c5", "#15151c", arm = 7); txt("POSHT-POSHT!", x + 10, gy - 38, "#d8373b", SM, 1)
            }
        }),

    // 3 ─ METRO: escalators + trains
    LevelDef(a("Метро", "Metro"),
        a("Банда спустилась в ташкентское метро — самое красивое в мире! Прыгай на поезда.", "To'da Toshkent metrosiga tushdi — dunyodagi eng go'zal metro! Poyezdlarga sakra."),
        a("Механика: эскалаторы везут тебя сами (иногда против), а через пути — только на поезде.", "Mexanika: eskalatorlar o'zi olib ketadi (ba'zan teskari), yo'llardan esa faqat poyezdda o'tasan."),
        2, Theme("#e9e1cc", "#fff6e0", "#7d7468", "#6b6358", "#3a4f80", "#2c3e66", "#c9a227", "#f4c430"),
        build = { b -> newMap(118, 17); with(b) {
            G(0, 16, 14); N(4, 8, 12); E('c', 10, 13, 3)
            MP(17, 12, 4, 'x', 17, 28, 0.8f, "poezd", "train")
            G(33, 52, 14); P(34, 40, 14, 'E'); P(42, 43, 10); N(39, 41, 11); E('c', 47, 13, 3); P(49, 51, 9); I('x', 50, 8, "foto")
            MP(53, 12, 4, 'x', 53, 62, 1f, "poezd", "train")
            G(67, 85, 14); WB(72, 73, 11, 13); P(75, 77, 9); P(79, 80, 6); N(75, 77, 8); E('c', 78, 13, 3); I('d', 70, 12); P(82, 84, 10); I('x', 83, 9, "jeton")
            G(89, 117, 14); P(90, 96, 14, 'e'); E('c', 99, 13, 3); E('c', 104, 13, 3); N(90, 94, 11); I('s', 98, 12); goal(108, 11, "mgate")
            Z("eshiklar", a("«Eshiklar yopiladi»", "«Eshiklar yopiladi»"), a("Голос диктора: «Ehtiyot bo'ling, eshiklar yopiladi!»", "Diktor ovozi: «Ehtiyot bo'ling, eshiklar yopiladi!»"), 12, 12, 4, 2, "still", "chime")
            Z("poezd", a("Поездка на поезде", "Poyezdda yurish"), a("Ботир едет на крыше вагона. Не повторяй в жизни!", "Botir vagon tomida ketyapti. Hayotda takrorlama!"), 0, 0, 0, 0, "ride")
            Z("lyustra", a("Люстра", "Qandil"), a("Ты дотянулся до люстры! Станции метро — как дворцы.", "Qandilga yetding! Metro bekatlari — saroylardek."), 41, 5, 4, 3, "touch")
            Z("foto", a("Старое правило", "Eski qoida"), a("Фотоаппарат! Раньше в метро Ташкента нельзя было фотографировать.", "Fotoapparat! Ilgari Toshkent metrosida suratga olish mumkin emas edi."), 0, 0, 0, 0, "item")
            Z("navoi", a("Купола «Алишера Навои»", "«Alisher Navoiy» gumbazlari"), a("Под куполами станции «Алишер Навои» — как в сказке.", "«Alisher Navoiy» bekati gumbazlari ostida — xuddi ertakdagidek."), 79, 4, 2, 2, "touch")
            Z("jeton", a("Жетон метро", "Metro jetoni"), a("Старый жетон! Раньше в метро проходили по жетонам.", "Eski jeton! Ilgari metroga jeton bilan kirilardi."), 0, 0, 0, 0, "item")
            NPC(Npc(4, 14, "#2457c5", "#2457c5"))
        } },
        back = {
            for (r in arrayOf(17 to 32, 53 to 66)) { px("#0b0f1a", X(r.first), Y(14), (r.second - r.first + 1) * T, 3 * T); px("#6b7280", X(r.first), Y(16) - 2, (r.second - r.first + 1) * T, 2) }
            val x = X(75) - 8; for (k in 0 until 3) G.halfDisc("#2e6fd8", x + k * 24 + 12, Y(4), 12)
            txt("ALISHER NAVOIY", X(77), Y(2), "#f4c430", SM, 1); txt("M", X(12) + 4, Y(9), "#2e6fd8", BIG)
            val lx = X(43); px("#c9a227", lx, Y(4) - 30, 1, 30 + 0); G.c.save(); G.c.clipRect(lx - 12f, Y(6) - 2f, lx + 12f, Y(6) + 9f); G.circle("#f4c430", lx, Y(6) - 2, 10); G.c.restore()
            for (k in 0 until 5) px("#fff6c8", lx - 9 + k * 4, Y(6) + 1 + (k % 2) * 3, 2, 4)
        }),

    // 4 ─ ANHOR: swimming
    LevelDef(a("Анхор", "Anhor"),
        a("Следы ведут через парк у канала Анхор. Вода холодная, но плавать можно!", "Izlar Anhor bo'yidagi bog' orqali o'tadi. Suv sovuq, lekin suzsa bo'ladi!"),
        a("Механика: плавание. В воде жми прыжок, чтобы грести вверх. Течение несёт вправо.", "Mexanika: suzish. Suvda yuqoriga suzish uchun sakrashni bos. Oqim o'ngga oqizadi."),
        3, Theme("#4f9a3a", "#7cc44a", "#9a6a3a", "#86592e", "#d9b98a", "#b8956a", "#8b5a2b", "#b07a45"),
        build = { b -> newMap(125, 17); with(b) {
            G(0, 18, 14); N(4, 7, 12); E('g', 14, 13, 3); P(9, 11, 10); N(9, 11, 9)
            G(19, 34, 16); WA(19, 34, 14, 15); MP(19, 13, 3, 'x', 19, 31, 0.7f, "qayiq", "boat"); N(23, 30, 15)
            G(35, 55, 14); P(44, 47, 11); N(44, 47, 10); E('g', 48, 13, 3); I('d', 52, 12)
            G(56, 75, 14); P(58, 60, 11); P(61, 63, 8); P(64, 66, 5); N(61, 63, 7); E('k', 70, 13, 3); E('b', 62, 9, 4)
            G(76, 90, 16); WA(76, 90, 14, 15); P(78, 80, 12); P(83, 85, 11); P(88, 89, 12); N(79, 86, 15); E('b', 83, 9, 5)
            G(91, 124, 14); N(93, 97, 12); E('g', 100, 13, 3); I('s', 103, 12); E('k', 108, 13, 3); goal(116, 11)
            Z("bodom", a("Цветущий бодом", "Gullagan bodom"), a("Ботир запрыгнул на цветущий миндаль. Весна в Ташкенте!", "Botir gullagan bodomga sakradi. Toshkentda bahor!"), 9, 8, 3, 2, "touch")
            Z("qayiq", a("Лодка", "Qayiq"), a("Ботир плывёт на лодке по Анхору. Красота!", "Botir Anhorda qayiqda suzmoqda. Go'zal!"), 0, 0, 0, 0, "ride")
            Z("anhor", a("Анхор ледяной", "Anhor muzdek"), a("Бррр! Вода в Анхоре ледяная даже летом!", "Brrr! Anhor suvi yozda ham muzdek!"), 0, 0, 0, 0, "water")
            Z("baliqchi", a("Рыбак", "Baliqchi"), a("Рыбак шепчет: «Тихо! Клюёт!»", "Baliqchi shivirlaydi: «Jim! Ilinyapti!»"), 35, 12, 3, 2, "still")
            Z("sumalak", a("Сумаляк", "Sumalak"), a("Сумаляк к Навруз! Варили всю ночь. Navro'z muborak! +1 жизнь", "Navro'zga sumalak! Tun bo'yi qaynatildi. Navro'z muborak! +1 jon"), 40, 12, 2, 2, "touch", "hp")
            Z("charxpalak", a("Колесо обозрения", "Charxpalak"), a("С высоты видно весь Ташкент и телебашню!", "Balanddan butun Toshkent va teleminora ko'rinadi!"), 64, 3, 3, 2, "touch")
            NPC(Npc(36, 14, "#2f7d4a", "#8b5a2b"))
        } },
        back = {
            val tx = X(9); px("#6b4a2a", tx + 18, Y(10), 5, 4 * T); G.circle("#58a643", tx + 20, Y(9), 20)
            for (k in 0 until 12) { val h = hash(k, 4); px("#f7c6d9", tx + 4 + h % 32, Y(8) - 6 + (h shr 5) % 24, 2, 2) }
            val sx = X(40); val sy = Y(14); px("#3b2a20", sx + 4, sy - 4, 24, 4); px("#f08a24", sx + 8, sy - 6, 16, 2); G.spr(Spr.QOZ, sx + 9, sy - 14, Spr.pal('O' to "#7a4a1a")); if (frame % 16 < 8) px("#ffffff", sx + 15, sy - 20, 1, 5)
        },
        front = { val fx = X(36) + 9; val fy = Y(14) - 12; G.line("#3b2a20", fx, fy, fx + 22, fy - 14); G.line("#e8f6ff", fx + 22, fy - 14, fx + 26, fy + 22 + (if (frame % 60 < 30) 0 else 2)) }),

    // 5 ─ TV TOWER: wind gusts + lift (vertical)
    LevelDef(a("Телебашня", "Teleminora"),
        a("Жадина-ака поднимается на телебашню, 375 метров! Лезь за ним по площадкам.", "Ochko'z aka teleminoraga ko'tarilyapti, 375 metr! Maydonchalar bo'ylab orqasidan chiq."),
        a("Механика: порывы ветра сдувают в сторону. Смотри на стрелки >>> перед порывом.", "Mexanika: shamol yon tomonga uchiradi. Shamoldan oldin >>> belgilariga qara."),
        4, Theme("#9aa8b8", "#c9d3de", "#5f7287", "#506276", "#8aa0b8", "#6f849b", "#9aa1ab", "#d6dbe2"), vertical = true,
        build = { b -> newMap(12, 72); with(b) {
            WB(0, 0, 0, 71); WB(11, 11, 0, 71); G(1, 10, 69)
            var i = 0; var r = 66
            while (r >= 8) { if (i % 2 == 1) P(1, 5, r) else P(6, 10, r); if (i % 3 == 2) P(4, 7, r - 1); if (i % 2 == 1) N(2, 4, r - 1); i++; r -= 3 }
            P(3, 8, 5); MP(8, 52, 3, 'y', 36, 52, 0.6f, "lift", "lift")
            E('b', 5, 57, 4); E('b', 5, 45, 4); E('b', 5, 30, 4); E('b', 5, 18, 4); E('h', 5, 40, 4)
            I('s', 8, 47); I('d', 2, 26); I('s', 3, 14)
            WIND(48, 56, 1, 0); WIND(30, 38, -1, 80); WIND(12, 20, 1, 160)
            goal = Goal(4f * T, 1f * T, 64f, 4f * T, "deck")
            Z("lift", a("Лифт", "Lift"), a("Ботир поехал на лифте. Удобно!", "Botir liftda ketdi. Qulay!"), 0, 0, 0, 0, "ride")
            Z("humo", a("Птица Хумо", "Humo qushi"), a("Птица Хумо — символ счастья — несёт тебя ввысь!", "Baxt qushi Humo seni yuksakka olib uchmoqda!"), 0, 0, 0, 0, "humo", "carry")
            Z("kaptar", a("Укротитель голубей", "Kaptarboz"), a("Три голубя побеждены! Теперь они уважают Ботира.", "Uchta kaptar yengildi! Endi ular Botirni hurmat qiladi."), 0, 0, 0, 0, "stomp", et = 'b', cnt = 3)
            Z("bulut", a("Облако-казан", "Qozon bulut"), a("Смотри: облако в форме казана! Пахнет пловом.", "Qara: qozon shaklidagi bulut! Osh hidi keladi."), 2, 28, 8, 3, "see")
            Z("axborot", a("Сигнал «Ахборот»", "«Axborot» signali"), a("Антенна ловит сигнал: «Ахборот» начинается!", "Antenna signal tutdi: «Axborot» boshlanmoqda!"), 1, 20, 3, 2, "still")
            Z("shamol", a("Против ветра", "Shamolga qarshi"), a("Ух, какой ветер! Держи дуппи покрепче!", "Voy, qanday shamol! Do'ppingni mahkam ushla!"), 0, 0, 0, 0, "wind")
            Z("restoran", a("Вращающийся ресторан", "Aylanma restoran"), a("Ресторан на высоте! Тут подают плов с видом на город. +1 жизнь", "Balanddagi restoran! Shahar manzarasi bilan osh tortiladi. +1 jon"), 8, 4, 3, 1, "touch", "hp")
        } },
        back = {
            val y = Y(28); px("#ffffff", X(2), y + 10, 120, 14); px("#ffffff", X(3), y + 2, 90, 10); px("#dfe8f5", X(2), y + 22, 120, 2); px("#ffffff", X(2) - 6, y + 2, 8, 6); px("#ffffff", X(8), y + 2, 8, 6)
            val ax = X(1) + 4; val ay = Y(21); px("#c9ced6", ax, ay - 30, 2, 30); px("#c9ced6", ax - 6, ay - 24, 14, 2); px("#c9ced6", ax - 4, ay - 18, 10, 2); if (frame % 30 < 15) px("#e24b4a", ax, ay - 32, 2, 2)
            val rx = X(8); val ry = Y(5); px("#8b5a2b", rx, ry - 8, 30, 3); px("#f5f0e6", rx + 2, ry - 11, 26, 3); G.spr(Spr.QOZ, rx + 8, ry - 18, Spr.pal('O' to "#f0c27a")); txt("375m", X(4), Y(0) + 4, "#f4c430", SM)
        }),

    // 6 ─ TASHKENT CITY: ziplines + boss
    LevelDef(a("Tashkent City", "Tashkent City"),
        a("Логово Жадины-ака — небоскрёб в Tashkent City. Он сел в казан-робота. Прыгай ему на голову!", "Ochko'z akaning uyasi — Tashkent Citydagi osmono'par bino. U qozon-robotga o'tirdi. Boshiga sakra!"),
        a("Механика: тросы между крышами. Подпрыгни к тросу — и едешь сам. В конце — босс!", "Mexanika: tomlar orasida troslar. Trosga sakra — o'zing ketasan. Oxirida — boss!"),
        5, Theme("#8a93a6", "#b9c2d2", "#4b5563", "#3f4652", "#3f5f86", "#2a3f6a", "#6b7280", "#c9ced6"), boss = true,
        build = { b -> newMap(110, 17); with(b) {
            G(0, 14, 13); N(4, 8, 11); E('c', 10, 12, 3)
            ZIP(13.5f, 10f, 20.5f, 10.2f); N(15, 17, 9)
            G(18, 30, 12); P(24, 27, 9); N(24, 27, 8); E('c', 27, 11, 2)
            ZIP(29.5f, 9f, 36.5f, 10.4f); N(31, 33, 8)
            G(34, 46, 13); E('k', 43, 12, 2); P(47, 48, 10)
            ZIP(45.5f, 10f, 52f, 9.8f)
            G(50, 62, 12); I('s', 53, 11); I('d', 61, 10); E('c', 59, 11, 2)
            ZIP(61.5f, 9f, 66.5f, 10.5f); N(63, 64, 8)
            G(64, 74, 13); E('c', 70, 12, 3)
            G(77, 109, 14); N(78, 83, 12); E('c', 86, 13, 3); WB(108, 109, 0, 13)
            g.arenaX0 = 96f * T; g.arenaX1 = 108f * T
            Z("plovexpress", a("Вывеска «Plov-Express»", "«Plov-Express» peshlavhasi"), a("Неоновая вывеска «Plov-Express»: «Плов за 30 секунд!» Позор!", "«Plov-Express» neon yozuvi: «30 soniyada osh!» Uyat!"), 12, 0, 1, 17, "pass")
            Z("tros", a("Король тросов", "Tros qiroli"), a("Вжух! Ботир катится по тросу над Ташкентом!", "Vjuh! Botir Toshkent ustidan trosda uchmoqda!"), 0, 0, 0, 0, "zip")
            Z("kofe", a("Латте с казаном", "Qozonli latte"), a("Бариста нарисовал на пенке казан. Намёк понят!", "Barista ko'pikka qozon chizdi. Ishora tushunarli!"), 20, 10, 4, 2, "still")
            Z("fontan", a("Поющие фонтаны", "Kuylovchi favvoralar"), a("Фонтаны танцуют, и Ботир вместе с ними!", "Favvoralar raqsga tushdi, Botir ham ular bilan!"), 36, 11, 4, 2, "still", "dance")
            Z("ofis", a("Офисный планктон", "Ofis xodimi"), a("Клерк вздыхает: «Опять понедельник...»", "Xodim xo'rsinadi: «Yana dushanba...»"), 54, 10, 2, 2, "touch")
            Z("selfi", a("Туристы", "Sayyohlar"), a("Туристы просят селфи с Ботиром. Звезда!", "Sayyohlar Botir bilan selfi so'rashdi. Yulduz!"), 66, 0, 1, 17, "pass")
            Z("mukammal", a("Идеальная битва", "Mukammal jang"), a("Победа над боссом без единой царапины!", "Bossni bir marta ham tegmasdan yengding!"), 0, 0, 0, 0, "perfect")
            NPC(Npc(21, 12, "#6b3f1f", "#1c1410")); NPC(Npc(55, 12, "#2b2f3a", "#1c1410", glasses = true)); NPC(Npc(67, 13, "#f4c430", "#d8373b", wave = true)); NPC(Npc(68, 13, "#2e9b6b", "#f5f0e6", wave = true))
        } },
        back = {
            val nx = X(11); val ny = Y(6); px(OUT, nx - 2, ny - 2, 74, 18); px("#2b1a3a", nx - 1, ny - 1, 72, 16); txt("PLOV-EXPRESS", nx + 35, ny + 4, if (frame % 40 < 30) "#ff5fa2" else "#7a2a50", SM, 1)
            val kx = X(20); val ky = Y(12); px("#6b3f1f", kx, ky - 14, 44, 14); px("#f5f0e6", kx, ky - 18, 44, 4); txt("KOFE", kx + 22, ky - 12, "#f4c430", SM, 1)
            val fx = X(37); val fy = Y(13); px("#86c5ef", fx, fy - 4, 48, 4)
            for (k in 0 until 4) { val h = 8 + abs(sin(frame / 15f + k)) * 18; px("#cfeeff", fx + 5 + k * 12, fy - 4 - h, 3, h) }
            px("#2b1a3a", X(96), Y(4), 192, 2); txt(L("ОФИС ЖАДИНЫ", "OCHKO'Z OFISI"), X(98), Y(3) - 4, "#f4c430", SM)
        })
)
