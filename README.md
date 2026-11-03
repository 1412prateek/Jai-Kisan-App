Jai Kisan 🌾

Jai Kisan is a free Android app that gives farmers practical tools, real-time information and AI-powered guidance in one place: weather forecasts, mandi (market) prices, farming calculators, crop disease detection and a hands-free voice assistant.

The app is built and maintained by a solo developer, and it is used by real farmers who regularly send feedback and crop questions that shape each update.

📌 Project Status
Status: Active and maintained
In use: Farmers were still actively using the app and sending feedback as of October 2026
Reach: 1,000+ installs on Google Play
Latest production release: v1.0.3 (Safety & UI Update), March 2026
🚀 Features
Weather Forecast: Real-time weather and forecasts for your location to help plan farming activities.
Mandi Prices: Up-to-date market prices for crops and agricultural commodities.
Farming Tools Hub:
Cost Calculator: Estimate and track farming expenses.
Fertilizer Calculator: Work out the amount of fertilizer needed for healthy soil and crops.
Irrigation Planner: Plan and manage crop watering cycles.
Yield Estimator: Predict harvest yield from crop type, area and conditions.
Disease Detection: Identify common crop diseases using simple detection tools.
Voice Assistant: A voice-activated assistant powered by the Google Gemini API that answers farming questions hands-free while working in the field.
How To Use Guide: A built-in help section so farmers can easily learn the app.
📱 Get the App

Available for free on Google Play:

👉 https://play.google.com/store/apps/details?id=com.rcc.jai_kisan


📅 Project Timeline
Date	Milestone
Oct 21, 2025	Initial alpha release (closed testing)
Oct 24, 2025	Internal testing release
Nov 17, 2025	Open testing and full rollout, live on Google Play
Mar 16, 2026	Production release v1.0.3 (Safety & UI Update)
Oct 20, 2026   Major Update, bug fixes and UI Improvements v1.0.5
Oct 2026	Still active and used by farmers

All releases are verifiable through Google Play Console records.

🛠️ Tech Stack & Architecture
Language: Java (Android API 24 to API 36)
UI/Design: Material Design (com.google.android.material:material:1.12.0), ConstraintLayout, CardView
Local Storage: Room Database (androidx.room:room-runtime:2.6.1) for persistent, offline-capable storage
Architecture Components: Lifecycle ViewModel & LiveData (androidx.lifecycle:lifecycle-viewmodel:2.6.1)
Networking & APIs:
OkHttp3 (com.squareup.okhttp3:okhttp:4.12.0) with HttpLoggingInterceptor
Gson (com.google.code.gson:gson:2.10.1) for JSON parsing
Google Gemini API for the voice assistant
Image Loading: Picasso (com.squareup.picasso:picasso:2.71828)
Location Services: Google Play Services Location
Markdown Rendering: Markwon Core (io.noties.markwon:core:4.6.2)
⚙️ Prerequisites
Android Studio Ladybug or newer
Android SDK 36 (target SDK)
A physical device or emulator running Android 7.0 (API 24) or higher
🔑 Setup & API Keys

The Voice Assistant uses the Google Gemini API, so you need your own API key to run that feature.

Clone the repository:
bash
   git clone https://github.com/1412prateek/Jai-Kisan-App.git
   cd Jai-Kisan-App
Open the project in Android Studio.
In the project root, create or open local.properties and add your key:
properties
   GEMINI_API_KEY=your_gemini_api_key_here

build.gradle reads this value and injects it into the app through BuildConfig. 4. Sync the project with Gradle files. 5. Build and run on a device or emulator.

Security note: Never commit local.properties or any API key to the repository.

🛡️ Permissions
INTERNET: fetch weather data and mandi prices, and answer voice assistant queries via Gemini
ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION: provide localized weather forecasts and relevant crop information
🤝 Contributing

Contributions are welcome. Good ways to help:

Report bugs or suggest features by opening an issue
Improve translations and make the app easier for more farmers to use
Improve disease detection accuracy and crop coverage
Improve documentation and add tests

To contribute:

Fork the repository
Create a branch (git checkout -b feature/your-feature)
Commit your changes and push the branch
Open a pull request describing what you changed and why
🗺️ Roadmap
Wider crop and disease coverage
More languages and simpler voice interaction
Better offline support
Improved accuracy of AI-generated farming guidance
👨‍💻 Maintainer

Created and maintained by Prateek Kumar (@1412prateek), published under RSVP Studios.

Farmers and users can reach out through the Google Play listing or by opening an issue here.

⚖️ License

This project is licensed under the MIT License. See the LICENSE file for details.

The "Jai Kisan" name, logo and RSVP Studios branding are not covered by the license, so please do not use them to publish a competing or misleading app.

Empowering farmers, securing the future.
