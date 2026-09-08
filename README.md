# RBS Bot (by Lakhvinder)

Android app for **Rajasthan Board exams prep** (RPSC / CET / Board exams) with an AI assistant built into it.

- 4 study pillars on the home screen: **Rajasthan History**, **Geography**, **Art & Culture**, **Polity & Current Affairs**
- **Live AI Quiz** — 10 MCQs per round with scorecard, explanations, and star-bookmarks
- **AI Teacher** bottom sheet explains every answer after you pick it
- **AI Chat** for doubt-solving (history stays saved on your phone)
- **Bookmarks** saved locally (Room database) — review anytime, even offline
- Global **Hindi / English** language toggle
- **Education Mode** — AI behaves like a friendly personal teacher (step-by-step, simple Hindi/Hinglish)
- **Auto Model Selection** — topic ke hisaab se best verified free model khud choose hota hai (Math/Science → reasoning model, general → Gemma 4, big factual → Nemotron 3)
- **Free Models Only** — live-verified against the OpenRouter catalogue at build time (Gemma 4, Nemotron 3, Inkling, Dots) + Google Gemini 3.5 Flash free tier; koi paid model kabhi auto-use nahi hota
- **Fallback** — model fail ho jaye to dusra free model khud try hota hai
- **Show Current Model** — kaunsa model chal raha hai, Quiz/Chat/Chat Settings me dikhta hai
- BYO API key — works with **Google Gemini** or **OpenRouter** (both free-tier routes only)
- No ads, no trackers, no account required. Data stays on your device.

Built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit.

## Install

**Tap to download the signed APK:**

[Download RBS-Bot.apk](https://github.com/lakh45vinder4679-cpu/rbs-bot/releases/latest/download/RBS-Bot.apk)

Then enable "Install unknown apps" for your file manager / browser and open the APK.

## Getting an API key (free)

Open the app → Settings → press **"Free API key kaise banaye?"** for one-tap help. In short:

- **Gemini**: go to https://aistudio.google.com/apikey → "Create API key" (free tier). App ab **Gemini 3.5 Flash** (current free) use karta hai by default.
- **OpenRouter**: go to https://openrouter.ai/keys → create key → top up $0 to unlock free models. App sirf **live-verified free models** use karta hai (Gemma 4 31B, Nemotron 3 Super, Inkling, etc.).

Paste the key into Settings and you are ready to quiz and chat.

## Build from source

```
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Privacy

Your API keys and chat/bookmark data never leave the device except direct calls to the AI provider you chose (Gemini or OpenRouter). No analytics, no third-party SDKs.
