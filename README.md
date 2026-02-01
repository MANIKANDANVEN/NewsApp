Summary

1. Clean Architecture & MVVM: Established a clear separation of concerns by dividing the project into Data, Domain, and Presentation layers, ensuring the app is scalable and easy to navigate.

2. BaseViewModel & Centralized Logic: Engineered a BaseViewModel that handles coroutine lifecycles and provides a safeLaunch helper. This centralizes error handling and UI state management (Loading, Success, Error), making the app more robust against crashes.

3. Interface Delegation (SearchDelegate): Used Kotlin's by keyword to extract search functionality into a reusable delegate. This keeps ViewModels lean and allows for consistent search behavior across multiple screens.

4. Modern DI with Hilt: Implemented Dagger Hilt for dependency injection. I used specific modules (NetworkModule, DatabaseModule) to provide Singleton instances of Retrofit and Room, facilitating constructor injection and high testability.

5. Offline-First Strategy with Room: Integrated a local Room Database to cache news articles. This ensures the user has a seamless experience even when the network is unavailable.

6. State Hoisting in Compose: Followed the "Stateful/Stateless" pattern for UI components. This decoupling allows for better unit testing and enables the use of Android Studio Previews for faster development.

7. Reactive UI with Flow: Leveraged StateFlow and the combine operator to create a reactive data pipeline. The UI automatically updates whenever the underlying data or search queries change.
