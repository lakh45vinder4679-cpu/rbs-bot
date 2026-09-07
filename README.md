# RBS Bot (by Lakhvinder)

Android app for **Rajasthan Board exams prep** (RPSC / CET / Board exams) with an AI assistant built into it.

- 4 study pillars on the home screen: **Rajasthan History**, **Geography**, **Art & Culture**, **Polity & Current Affairs**
- **Live AI Quiz** — 10 MCQs per round with scorecard, explanations, and star-bookmarks
- **AI Teacher** bottom sheet explains every answer after you pick it
- **AI Chat** for doubt-solving (history stays saved on your phone)
- **Bookmarks** saved locally (Room database) — review anytime, even offline
- Global **Hindi / English** language toggle
- BYO API key — works with **Google Gemini** (optional Google Search grounding) or **OpenRouter** (free models)
- No ads, no trackers, no account required. Data stays on your device.

Built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit.

## Install

**Tap to download RBS-Bot.apk (signed):**

[**Download RBS-Bot.apk**](https://github.com/lakh45vinder4679-cpu/rbs-bot/releases/latest/download/RBS-Bot.apk)

Direct link (tap karke kholo): <https://github.com/lakh45vinder4679-cpu/rbs-bot/releases/latest/download/RBS-Bot.apk>

Release page: [github.com/lakh45vinder4679-cpu/rbs-bot/releases](https://github.com/lakh45vinder4679-cpu/rbs-bot/releases)

Then enable "Install unknown apps" for your file manager / browser and open the APK.

## Getting an API key (free)

Open the app → Settings → press **"Free API key kaise banayein?"** for step-by-step help. In short:

- **Gemini**: go to https://aistudio.google.com/apikey → "Create API key" (free tier available).
- **OpenRouter**: go to https://openrouter.ai/keys → create key, then add $0 credit to unlock free models.

Paste the key into Settings and you are ready to quiz and chat.

## Build from source

```
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Privacy

Your API keys and chat/bookmark data never leave the device except direct calls to the AI provider you chose (Gemini or OpenRouter). No analytics, no third-party SDKs.
