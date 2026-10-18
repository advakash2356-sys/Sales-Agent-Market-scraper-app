# Sales-Agent-Market-scraper-app

Sales Agent Market scraper app developed by Adv. Akash Sharma.

## Features

- Local lead data persistence using an application database (`AppDatabase`, `LeadDao`, `LeadEntity`)
- Data repository layer for lead management (`LeadRepository`)
- API client integration (`ApiClients`)
- UI Dashboard and state handling (`DashboardScreen`, `DashboardViewModel`, `MainActivity`)
- Application UI theming (`Theme.kt`, `Color.kt`, `Type.kt`)
- Test suites covering unit testing, instrumented testing, Robolectric testing, and screenshot testing

## Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **Build System:** Gradle (Kotlin DSL)
- **Data & Architecture:** Room / SQLite database components, Repository pattern, ViewModel
- **Testing:** AndroidX Test, Robolectric

## Setup

Specific setup instructions are not provided in the repository files. As an Android Gradle project, standard setup typically involves opening the project in Android Studio or using the Gradle wrapper. A `.env.example` file is present for environment configuration.

## Usage

Detailed usage workflows and command-line instructions are not documented in the repository. The application entry point is `MainActivity`, which presents the `DashboardScreen`.

## Project Structure

```text
├── .env.example
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── metadata.json
├── gradle/
│   └── libs.versions.toml
├── assets/
│   └── .aistudio/
│       └── .gitignore
└── app/
    ├── .gitignore
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/
        │   │   ├── MainActivity.kt
        │   │   ├── data/
        │   │   │   ├── api/
        │   │   │   │   └── ApiClients.kt
        │   │   │   ├── database/
        │   │   │   │   ├── AppDatabase.kt
        │   │   │   │   ├── LeadDao.kt
        │   │   │   │   └── LeadEntity.kt
        │   │   │   └── repository/
        │   │   │       └── LeadRepository.kt
        │   │   └── ui/
        │   │       ├── DashboardScreen.kt
        │   │       ├── DashboardViewModel.kt
        │   │       └── theme/
        │   │           ├── Color.kt
        │   │           ├── Theme.kt
        │   │           └── Type.kt
        │   └── res/
        │       ├── drawable/
        │       ├── mipmap-*/
        │       ├── values/
        │       └── xml/
        ├── test/java/com/example/
        │   ├── ExampleRobolectricTest.kt
        │   ├── ExampleUnitTest.kt
        │   └── GreetingScreenshotTest.kt
        └── androidTest/java/com/example/
            └── ExampleInstrumentedTest.kt

## Add your API key

AI features need your own key (free Gemini key: https://aistudio.google.com/apikey). No key is committed to this repo.

1. Create a `local.properties` file in the project root (it is git-ignored).
2. Add: `GEMINI_API_KEY=your_key_here` (and `APIFY_API_KEY=your_token_here` for scraping`
3. Rebuild in Android Studio. The Secrets Gradle plugin exposes it as `BuildConfig`. Without a key the app uses its built-in fallbacks.

**Privacy:** the key stays in your own `local.properties` / your build, is never committed or logged, and is sent only to the AI provider's API. Delete the line and rebuild to remove it. There is no in-app key screen yet because these Android apps are untested in this environment; a settings screen is a planned follow-up.
