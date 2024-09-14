# Notes

Project setup using min api level 24

Create android virtual device:
https://developer.android.com/studio/run/managing-avds#createavd

Using:
- Medium Phone
- System Image: Nougat (API Level = 24)

Build and run using created AVD:
https://developer.android.com/studio/run

---
## Project structure

- `app/src/main/java/`: Java/Kotlin source code
    - Kotlin source files should be organized in folders, `XFragment.kt` and `XViewModel.kt` should be in folder `X`
        - `Fragment.kt` files are responsible for creating and managing the view using the `XViewModel.kt` files
        - ViewModels are observed by the Fragment files
        - Any changes to the ViewModel's LiveData are automatically reflected in the UI

- `app/src/main/res/`: Resource files: XML layouts, menu files, other defined resources

- `app/src/main/AndroidManifest.xml`: Fundamental charactertics of the entire app and defins each component

- `.xml` files can reference other resources like string values or other fragments

- `activity_main.xml` defines the layout/user interface of the MainActivity
    - Logic is handled by `MainActivity.kt`

    - Uses `@menu/bottom_nav_menu.xml` as the design/layout for the built-in android `BottomNavigationView` class
        - purely just for the layout (id, icon, title)

    - Contains built-in `NavHostFragment` fragment which is a container for swapping in/out different fragments
        - `app:navGraph="@navigation/mobile_navigation"` - points to `@navigation/mobile_navigation.xml` file.

- `@navigation/mobile_navigation.xml` defines the navigation graph of the application - contains fragments
    - each fragment has:
        - android:id - this should match the id in `@menu/bottom_nav_menu.xml`

        - android:name - this should reference the kotlin source code fragment file

        - android:label - just a label for the fragment

        - tools:layout - the `.xml` file defining the layout/UI