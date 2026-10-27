# Botir как Telegram Mini App

Веб-версия собирается автоматически из того же Kotlin-кода, что и Android (`shared/` + `web/`), и публикуется в ветку `gh-pages`.

## 1. Включить GitHub Pages (один раз)
Репозиторий → **Settings → Pages** → Source: **Deploy from a branch** → Branch: **gh-pages**, папка **/ (root)** → Save.
Через минуту игра будет доступна по адресу: `https://utkirov.github.io/test/`

## 2. Создать бота и Mini App в @BotFather
1. `/newbot` → придумай имя и username (например, `BotirGameBot`).
2. `/newapp` → выбери бота → название **Botir**, описание, картинка 640×360 (можно взять `feature_1024x500.png` и обрезать) →
   URL: `https://utkirov.github.io/test/` → короткое имя, например `play`.
   Ссылка на игру будет вида `https://t.me/BotirGameBot/play`.
3. (По желанию) `/mybots` → бот → **Bot Settings → Menu Button** → URL игры, текст «Играть».

## 3. Кнопка «Поделиться»
В `web/static/index.html` замени `window.BOTIR_LINK = null;` на свою ссылку, например
`window.BOTIR_LINK = "https://t.me/BotirGameBot/play";` — тогда друзья будут открывать игру прямо в Telegram.

## Что умеет версия для Telegram
- Прогресс сохраняется в облаке Telegram (CloudStorage) — продолжай на любом устройстве.
- Язык берётся из Telegram (узбекский или русский).
- Системная кнопка «Назад» Telegram работает как в игре.
- Вибрация через Telegram Haptic Feedback.
- Свайп вниз не закрывает игру во время прыжков, цвета шапки под игру.
- «Поделиться» рекордом и победой в чатах.
- Работает и в обычном браузере на телефоне и компьютере (клавиатура: стрелки + пробел).
