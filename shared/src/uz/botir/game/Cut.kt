package uz.botir.game

/** Short pixel cutscenes: speaker + line, drawn over the level's background. */
class Line(val who: Char, val ru: String, val uz: String)
class Cut(val bg: Int, val lines: List<Line>)

object Cuts {
    private fun l(w: Char, ru: String, uz: String) = Line(w, ru, uz)

    val ALL = mapOf(
        "c1" to Cut(0, listOf(
            l('D', "Ботир! Беда! Наш казан пропал!", "Botir! Voy, baloga qoldik! Qozonimiz yo'qoldi!"),
            l('B', "Как пропал? Завтра же той!", "Qanday yo'qoldi? Ertaga to'y-ku!"),
            l('D', "Это Жадина-ака. Хочет открыть свой «Plov-Express».", "Bu Ochko'z akaning ishi. «Plov-Express» ochmoqchi."),
            l('B', "Не волнуйся, бобо. Я верну казан!", "Xavotir olmang, bobo. Qozonni qaytaraman!"))),
        "c2" to Cut(1, listOf(
            l('B', "Соседи сказали, банда побежала на Чорсу.", "Qo'shnilar aytdi, to'da Chorsuga qochibdi."),
            l('N', "Ищешь казан? На базаре найдётся всё, кроме совести Жадины!", "Qozon izlayapsanmi? Bozorda hamma narsa bor, faqat Ochko'zning vijdoni yo'q!"))),
        "c3" to Cut(2, listOf(
            l('O', "Попробуй догони, малыш! Ха-ха!", "Yetib ol-chi, bolakay! Ha-ha!"),
            l('B', "Он спустился в метро! За ним!", "U metroga tushdi! Ortidan!"))),
        "c4" to Cut(3, listOf(
            l('B', "Следы ведут через Анхор...", "Izlar Anhor orqali o'tadi..."),
            l('N', "Вода холодная, но ты же батыр! Плыви или садись в лодку.", "Suv sovuq, lekin sen botirsan-ku! Suz yoki qayiqqa o't."))),
        "c5" to Cut(4, listOf(
            l('O', "Я на самом верху! Отсюда весь Ташкент мой!", "Men eng tepadaman! Bu yerdan butun Toshkent meniki!"),
            l('B', "Ну всё, я поднимаюсь!", "Bo'ldi, men chiqyapman!"))),
        "c6" to Cut(5, listOf(
            l('O', "Добро пожаловать в мой офис! Мой казан-робот тебя раздавит!", "Ofisimga xush kelibsan! Qozon-robotim seni yanchib tashlaydi!"),
            l('B', "Посмотрим, Жадина-ака!", "Ko'ramiz, Ochko'z aka!"))),
        "end1" to Cut(0, listOf(
            l('O', "Ой-ой... Забирай свой казан.", "Voy-voy... Qozoningni olaver."),
            l('D', "Ботир, ты настоящий батыр! Вечером плов на всю махаллю!", "Botir, sen haqiqiy botirsan! Kechqurun butun mahallaga osh!"),
            l('O', "А можно... и мне тарелочку?", "Menga ham... bir likopcha bo'ladimi?"),
            l('D', "Садись, сосед. Плов — для всех.", "O'tiring, qo'shni. Osh — hamma uchun."))),
        "c7" to Cut(6, listOf(
            l('D', "Ботир! Пропал мой золотой кафгир!", "Botir! Oltin kapgirim yo'qoldi!"),
            l('O', "Это не я! Это мой брат Зикна-ака. Он увёз кафгир по Шёлковому пути!", "Men emasman! Bu akam Ziqna aka. U kapgirni Ipak yo'li bo'ylab olib ketdi!"),
            l('B', "Тогда — в Самарканд!", "Unda — Samarqandga!"))),
        "c8" to Cut(7, listOf(
            l('Z', "Ха! Кафгир уже в Бухаре. Пустыня тебя остановит!", "Ha! Kapgir allaqachon Buxoroda. Cho'l seni to'xtatadi!"),
            l('B', "Меня не остановит даже верблюд!", "Meni hatto tuya ham to'xtata olmaydi!"))),
        "c9" to Cut(8, listOf(
            l('Z', "Последний шанс, малыш. Хива — моя крепость!", "Oxirgi imkoniyat, bolakay. Xiva — mening qal'am!"),
            l('B', "Ичан-Кала видала батыров посильнее тебя!", "Ichan qal'a sendan kuchliroq botirlarni ko'rgan!"))),
        "end2" to Cut(8, listOf(
            l('Z', "Ладно-ладно... Забирай свой кафгир.", "Mayli-mayli... Kapgiringni olaver."),
            l('D', "Ботир, весь Узбекистан гордится тобой!", "Botir, butun O'zbekiston sen bilan faxrlanadi!"),
            l('B', "А теперь — плов! Самый вкусный в мире.", "Endi esa — osh! Dunyodagi eng mazali osh."))),
        "c10" to Cut(9, listOf(
            l('D', "Ботир! Плов готов, а лагана нет! Риштанский ляган моего деда пропал!", "Botir! Osh tayyor, lagan esa yo'q! Bobomdan qolgan Rishton lagani yo'qoldi!"),
            l('X', "Ха! Я — Хасис-ака, младший брат. Ляган теперь мой, и я никому его не одолжу!", "Ha! Men — Xasis aka, kenja uka. Lagan endi meniki va uni hech kimga bermayman!"),
            l('B', "Опять эти братья! За ним — в Ферганскую долину!", "Yana shu aka-ukalar! Ortidan — Farg'ona vodiysiga!"))),
        "c11" to Cut(10, listOf(
            l('X', "В Риштане наделаю тысячу копий и продам как оригинал!", "Rishtonda mingta nusxa yasab, asl deb sotaman!"),
            l('B', "Настоящий риштанский ляган не подделаешь, Хасис-ака!", "Haqiqiy Rishton laganini soxtalashtirib bo'lmaydi, Xasis aka!"))),
        "c12" to Cut(11, listOf(
            l('X', "Я уплыву на корабле! ...Ой. А где море?", "Kemada suzib ketaman! ...Voy. Dengiz qani?"),
            l('B', "Арал высох, Хасис-ака. Пустыня тебя не спрячет!", "Orol qurigan, Xasis aka. Cho'l seni yashira olmaydi!"))),
        "end3" to Cut(11, listOf(
            l('X', "Сдаюсь... Забирай свой ляган.", "Taslim... Laganingni olaver."),
            l('D', "Казан, кафгир и ляган снова дома! Зовите всех, даже трёх братьев!", "Qozon, kapgir va lagan yana uyda! Hammani chaqiring, hatto uch aka-ukani ham!"),
            l('X', "Правда? И мне тарелку плова?", "Rostdanmi? Menga ham bir likopcha oshmi?"),
            l('B', "Конечно! Плов на одном лагане — для всех.", "Albatta! Bir lagandagi osh — hamma uchun.")))
    )
}
