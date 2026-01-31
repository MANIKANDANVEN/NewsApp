A modern, offline-first News application built with Jetpack Compose, following Clean Architecture principles and Senior-level architectural patterns.

🛠 Technical Stack
UI: Jetpack Compose (Material 3) with Stateful/Stateless patterns.

Architecture: MVVM + Clean Architecture (Data, Domain, Presentation).

Threading/Async: Kotlin Coroutines & Flow (Reactive UI).

Dependency Injection: Hilt (Dagger) with Constructor Injection.

Local Storage: Room Database (Offline-first caching).

Networking: Retrofit + OkHttp + GSON.

Background Tasks: WorkManager for periodic data synchronization.

🏗 Key Engineering Highlights
1. Robust Architecture & "Base" Layer

To ensure scalability and reduce boilerplate, I implemented a centralized architecture:

BaseViewModel: Created a foundation for all ViewModels that handles Coroutine lifecycle, centralized error catching, and automatic UI state orchestration (Loading/Success/Error).

Interface Delegation: Implemented a SearchDelegate using Kotlin's by keyword. This decouples search logic from ViewModels, allowing for easy reuse across Headlines, Sources, and Saved screens.

2. Advanced Dependency Injection

Utilized Dagger Hilt to manage complex dependency graphs:

Modular DI: Separated NetworkModule and DatabaseModule to isolate third-party library configurations.

Constructor Injection: Achieved 100% decoupling in the Repository layer, facilitating easier unit testing.

On-Demand Initialization: Configured Hilt with WorkManager to allow background workers to receive injected dependencies without bloating the App startup.

3. Reactive & Offline-First Strategy

The app is designed to be functional even without an active internet connection:

Room Persistence: Articles and user preferences (Source selections) are stored locally.

WorkManager Sync: Implemented a NewsSyncWorker that runs periodically to fetch the latest headlines in the background, ensuring the cache is always fresh.

Flow Combine: Used the combine operator to merge the data stream with the user's search query, creating a high-performance, reactive filtering system.

4. Navigation & UI Safety

State Hoisting: Every screen is split into a Stateful version (handling ViewModel interaction) and a Stateless version (handling UI rendering), enabling rapid UI prototyping and testing.

Safe Argument Passing: Implemented URL encoding for Compose Navigation to ensure complex strings (like news URLs) are passed between screens without breaking routing logic.

📺 Demoing the Engineering
Background Sync: Can be verified via the Android Studio Background Task Inspector.

Error Handling: Triggered automatically via the BaseViewModel if the API key is invalid or the network is disconnected.

Search Responsiveness: Real-time filtering implemented via reactive StateFlow updates in the SearchDelegate.

📂 Project Structure
Plaintext
com.example.newsapp
├── data             # Local (Room), Remote (Retrofit), and Repositories
├── di               # Hilt Modules (Network, Database)
├── services         # API Interfaces
├── ui               # Compose Screens, Components, and Theme
└── viewmodel
    ├── base         # BaseViewModel for centralized logic
    ├── state        # UI State Sealed Classes
    └── delegates    # SearchDelegate implementations
