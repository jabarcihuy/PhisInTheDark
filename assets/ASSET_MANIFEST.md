# Phis in the Dark Generated Assets

Phis in the Dark memakai asset dasar non-kosong yang digenerate otomatis oleh `com.nulltrace.assets.AssetGenerator`.

## Images

- `wallpaper_desktop.png`
- `terminal_background.png`
- `loading_screen.png`
- `popup_warning.png`
- `browser_ui_asset.png`
- `glitch_overlay.png`
- `crt_overlay.png`
- `fake_error_screen.png`
- `notification_asset.png`
- `button_normal.png`
- `button_hover.png`
- `button_pressed.png`
- `icon_browser.png`
- `icon_terminal.png`
- `icon_notes.png`
- `icon_mail.png`
- `icon_save.png`
- `icon_load.png`
- `website_hacker_forum.png`
- `website_hidden_wiki.png`
- `website_encrypted_login.png`
- `website_creepy_marketplace.png`
- `website_abandoned_chatroom.png`
- `website_corrupted_website.png`
- `website_mysterious_blog.png`
- `website_government_archive.png`
- `website_conspiracy_forum.png`
- `website_deep_web_search.png`

## Sounds

- `typing.wav`
- `notification.wav`
- `glitch.wav`
- `ambience_loop.wav`
- `static_noise.wav`
- `error.wav`
- `button_click.wav`
- `creepy_whisper.wav`

## Fonts

- `nulltrace-terminal.ttf`
- `nulltrace-hacker.ttf`
- `nulltrace-retro.ttf`

## Regenerate

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.nulltrace.assets.AssetGenerator --force
```

Generator juga dipanggil otomatis saat game start jika asset belum ada.
