 1. Clean Architecture & MVVM: Established a clear separation of concerns by dividing the project                                                                                   
     into Data, Domain, and Presentation layers, ensuring the app is scalable and easy to navigate.                                                                                  
  2. BaseViewModel & Centralized Logic: Engineered a BaseViewModel that handles coroutine lifecycles                                                                                 
     and provides a safeLaunch helper. This centralizes error handling and UI state management                                                                                       
     (Loading, Success, Error), and now distinguishes connectivity failures from server/API errors                                                                                   
     so the UI can react differently to each.                                                                                                                                        
  3. Interface Delegation (SearchDelegate): Used Kotlin's by keyword to extract search functionality                                                                                 
     into a reusable delegate. This keeps ViewModels lean and allows for consistent search behavior                                                                                  
     across multiple screens.                                                                                                                                                        
  4. Modern DI with Hilt: Implemented Dagger Hilt for dependency injection, with dedicated modules                                                                                   
     (NetworkModule, DatabaseModule, ConnectivityModule) providing Singleton instances of an                                                                                         
     OkHttp-backed Retrofit client, Room, and a ConnectivityManager-based network observer — all via                                                                                 
     constructor injection for high testability.                                                                                                                                     
  5. Offline-First Strategy with Room: Extended the local Room database beyond saved articles into a                                                                                 
     full cache-first pipeline for headlines: the repository is the single source of truth, the UI                                                                                   
     renders cached data immediately on load, and a background network refresh updates it                                                                                            
     transparently through Flow — so headlines appear instantly instead of behind a loading spinner,                                                                                 
     and stay visible even when the network drops.                                                                                                                                   
  6. State Hoisting in Compose: Followed the "Stateful/Stateless" pattern for UI components,                                                                                         
     including a new stateless OfflineBanner. This decoupling allows for better unit testing and                                                                                     
     enables the use of Android Studio Previews for faster development.                                                                                                              
  7. Reactive UI with Flow: Leveraged StateFlow and the combine operator to create a reactive data                                                                                   
     pipeline — merging the cached-headlines flow, search query, and live connectivity state — so                                                                                    
     the UI automatically updates whenever any of them change.                                                                                                                       
  8. Network Resilience with OkHttp: Replaced the bare Retrofit client with a configured                                                                                             
     OkHttpClient (explicit connect/read/write timeouts, retryOnConnectionFailure, debug-only                                                                                        
     HttpLoggingInterceptor) so transient network issues are retried instead of surfacing as hard                                                                                    
     failures.                                                                                                                                                                       
  9. Real-Time Connectivity Awareness: Built a ConnectivityObserver wrapping                                                                                                         
     ConnectivityManager.NetworkCallback as a cold Flow, driving a global offline banner and gating                                                                                  
     refresh attempts app-wide — giving the app genuine awareness of network state rather than just                                                                                  
     reacting to failed requests.                                                                                                                                                    
  10. Security Hardening: Removed the hardcoded API key from source, injecting it instead via                                                                                        
      local.properties → BuildConfig (gitignored), and dropped the unnecessary usesCleartextTraffic                                                                                  
      manifest flag since the API is HTTPS-only
