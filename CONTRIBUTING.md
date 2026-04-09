# 🤝 Contributing to Personal Finance Companion

Thank you for your interest in contributing to the Personal Finance Companion project! Following these guidelines ensures a clean, consistent, and maintainable codebase for everyone.

---

## 🛠 Development Workflow

1. **Fork the repo** and create your branch from `main`.
2. **Setup environment**: Use **Android Studio (Ladybug or newer)**.
3. **Commit often**: Small, atomic commits are easier to review.

---

## 📋 Coding Standards

### 1. Kotlin & Compose
- **Kotlin Style**: Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html).
- **Compose Naming**: Composable functions should be `PascalCase`.
- **State Management**: Use `UiState` (Sealed Interface) in ViewModels.
- **Preview Support**: Always provide `@Preview` for Compose components with mock data.

### 2. Architecture (MVVM + Clean Architecture)
- **Dependency Rule**: Never import Android dependencies (like `Context`, `View`, `Lifecycle`) into the **Domain** layer.
- **Use Cases**: Every significant business operation should have a dedicated `UseCase`.
- **Repository Interface**: Define repository contracts in the **Domain** layer and implementations in the **Data** layer.

### 3. Git Commit Messages
- Always use **English** for commit messages.
- Use the imperative mood (e.g., `"Add transaction filtering"` instead of `"Added transaction filtering"`).
- Prefix commits with a scope (optional but encouraged):
    - `feat:` for new features.
    - `fix:` for bug fixes.
    - `docs:` for documentation changes.
    - `refactor:` for code restructuring.

---

## 🧪 Testing Guidelines

- **Unit Tests**: Place in `app/src/test`. Use `MockK` or `fakes` for repository/usecase testing.
- **UI Tests**: Place in `app/src/androidTest`. Use `ComposeTestRule` for component testing.
- **Room Persistence**: Test DAO queries to ensure data integrity during schema migrations.

---

## 🏗 Submitting a Pull Request

1. **Self-Review**: Ensure no debug logs (`println`, `Log.d`) are left in the code.
2. **Format Code**: Run `./gradlew ktlintCheck` (if configured) or use `Ctrl + Alt + L` in Android Studio.
3. **Describe Changes**: Provide a clear "What" and "Why" in your PR description.

---

Developed with ❤️ by Sandeep.
