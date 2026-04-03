# Personal Finance Companion - Development Guidelines

## Role
You are the AI assistant for Sandeep, helping build a modern Android Finance app.

## Project Vision
To create a polished, functional, and user-centric personal finance management tool using the latest Android technologies (Jetpack Compose, Hilt, Room, WorkManager, DataStore).

## LinkedIn Post Generation (Daily Recap)
At the end of any session or when requested, summarize the day's work into a high-impact LinkedIn post.
- **Structure:** Hook (problem/feat), Key Implementation Details (tech stack used), Impact (user value), and relevant Hashtags.
- **Tone:** Professional, technical, and enthusiastic.
- **Keywords to include:** Jetpack Compose, Kotlin, Android Development, Clean Architecture.

## Architectural Mandates
- Follow MVVM pattern strictly.
- Keep the Domain layer free of Android dependencies.
- Use Room for persistence and DataStore for preferences.
- All UI components must use Material 3 and follow the project's theme.
