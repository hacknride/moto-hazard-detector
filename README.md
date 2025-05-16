# CPSC 481 - Motorcycle Hazard Detector
This project is working, but still needs a lot of debugging. Additional features are planned post semester
because I had a lot of fun with this!
## Project Structure
Each class I made should have comment blocks describing what each do. However, the overall format is highlighted
here:
```
├── app                          // All app source files
│   ├── build.gradle.kts         // The dependencies used in this project
│   └── src
│       └── main
│           ├── AndroidManifest.xml                                    // The android manifest for permissions used
│           ├── java
│           │   └── dev
│           │       └── mattdebinion                                
│           │           └── onex3streamer
│           │               ├── camera                                // CAMERA PACKAGE 
│           │               │   ├── CameraConnectionManager.kt        // Handles camera connection
│           │               │   ├── CameraPreviewManager.kt           // Handles camera preview for the UI
│           │               │   ├── CameraProperties.kt               // Data class with camera properties
│           │               │   ├── CameraStateObserver.kt            // Observes change in the CameraViewModel
│           │               │   └── CameraViewModel.kt                // Holds dynamically updating variables about the camera
│           │               ├── MainActivity.kt                       // The main activity
│           │               ├── networking                            // NETWORKING PACKAGE
│           │               │   └── ConnectivityHandler.kt            // Handles network connectivity on the phone to a hotspot
│           │               ├── permissions                           // PERMISSIONS PACKAGE
│           │               │   ├── AppPermissionManager.kt           // Handles user granted or revoked permissions for app functionality
│           │               │   ├── PermissionGroup.kt                // Enum class to handle permissions
│           │               │   └── PermissionsViewModel.kt           // Holds dynamically updating variables about permission
│           │               ├── SharedPreferencesManager.kt           // Ensures data like camera credentials persist across restarts.
│           │               └── ui                                    // UI PACKAGE
│           │                   ├── home                              // UI.HOME PACKAGE
│           │                   │   ├── ConnectTypeDialogFragment.kt  // The UI logic for dialog boxes
│           │                   │   ├── HomeFragment.kt               // The home screen fragment which shows camera feed and alert log
│           │                   │   └── HomeViewModel.kt              // Holds dynamically updating variables about the home fragment.
│           │                   └── settings                          // UI.SETTINGS PACKAGE
│           │                       ├── GeneralFragment.kt            // The general settings fragment to update permissions and camera credentials
│           │                       ├── GeneralViewModel.kt           // Holds dynamically updating variables about general settings.
│           └── res                                          // The resource folder
│               ├── drawable/                                // Contains all icons used for the app
│               ├── layout                                   // Contains all screens used for the app
│               │   ├── activity_main.xml                    // Main activity screen
│               │   ├── app_bar_main.xml                     // App bar fragment
│               │   ├── content_main.xml                     // Main screen layout
│               │   ├── fragment_home.xml                    // Home fragment layout
│               │   ├── fragment_settings_app.xml            // Settings fragment layout
│               │   ├── nav_camera_info.xml                  // Camera info layout
│               │   ├── nav_header_main.xml                  // Navigation header layout
│               │   ├── nav_label_camerainfo.xml             // Camera info label layout
│               │   ├── nav_label_help.xml                   // Help label layout
│               │   └── nav_label_settings.xml               // Settings label layout
│               ├── menu                                     // Menu layouts
│               │   ├── activity_main_drawer.xml             // More app drawyer layouts
│               └── values/                                  // Contains persistent values such as strings, colors, and dimensions
├── build/                    // All files relating to the project once it's built
├── build.gradle.kts          // Global libraries?
├── gradle/                   // Gradle  build files
├── gradle.properties         // Gradle config properties
├── gradlew                   // Gradle binary
├── gradlew.bat               // Gradle batch file
└── settings.gradle.kts       // Root package wide gradle properties.
```

This chart shows a brief layout and description of the entire app with only important files included.
