# Jai Kisan 🌾

**Jai Kisan** is a comprehensive Android application designed to empower farmers by providing essential tools, real-time data, and intelligent assistance. The app offers a wide range of features aimed at maximizing agricultural productivity, from disease detection to yield estimation, weather forecasts, and market prices.

## 🚀 Features

*   **Weather Forecast:** Get real-time weather updates and forecasts tailored to your location to plan farming activities securely.
*   **Mandi Prices:** Access up-to-date market prices for various crops and agricultural commodities.
*   **Farming Tools Hub:** A centralized portal for vital farming calculators:
    *   **Cost Calculator:** Estimate and track detailed farming expenses.
    *   **Fertilizer Calculator:** Determine the exact amount of fertilizer required for optimal soil health.
    *   **Irrigation Planner:** Schedule and manage optimal crop watering cycles.
    *   **Yield Estimator:** Predict your harvest yield based on crop type, area, and current conditions.
*   **Disease Detection:** Quickly identify crop diseases using intuitive detection tools.
*   **Voice Assistant:** An intelligent, voice-activated assistant powered by the **Gemini API** that helps you find answers and guidance hands-free while working in the field.
*   **How To Use Guide:** Detailed instructions and an intuitive help section ensuring farmers can easily navigate the app.

## 🛠️ Tech Stack & Architecture

*   **Language:** Java (Android API 24 to API 36)
*   **UI/Design:** Material Design (`com.google.android.material:material:1.12.0`), ConstraintLayout, CardView
*   **Local Storage:** Room Database (`androidx.room:room-runtime:2.6.1`) for persistent, offline-capable storage.
*   **Architecture Components:** Lifecycle ViewModel & LiveData (`androidx.lifecycle:lifecycle-viewmodel:2.6.1`)
*   **Networking & APIs:**
    *   OkHttp3 (`com.squareup.okhttp3:okhttp:4.12.0`) & HttpLoggingInterceptor
    *   Gson (`com.google.code.gson:gson:2.10.1`) for JSON parsing
*   **Image Loading:** Picasso (`com.squareup.picasso:picasso:2.71828`)
*   **Location Services:** Google Play Services Location (`libs.play.services.location`)
*   **Markdown Rendering:** Markwon Core (`io.noties.markwon:core:4.6.2`)

## ⚙️ Prerequisites

Before you begin, ensure you have met the following requirements:
*   Android Studio Ladybug or newer.
*   Android SDK version 36 (Target SDK).
*   A physical Android device or Emulator running Android 7.0 (API 24) or higher.

## 🔑 Setup & API Keys

This application integrates with the **Google Gemini API** for its Voice Assistant feature.

1.  Clone the repository:
    ```bash
    git clone https://github.com/yourusername/jai-kisan.git
    cd jai-kisan
    ```
2.  Open the project in Android Studio.
3.  In the root directory of the project, locate or create a file named `local.properties`.
4.  Add your Gemini API Key to `local.properties`:
    ```properties
    GEMINI_API_KEY=your_gemini_api_key_here
    ```
    *Note: The `build.gradle` file automatically reads this local property and injects it securely into the app using `BuildConfig`.*
5.  Sync the project with Gradle files.
6.  Build and run the app on your desired device/emulator.

## 🛡️ Permissions

The app requires the following key permissions to function fully:
*   `INTERNET`: For fetching weather data, mandi prices, and answering voice assistant queries via Gemini.
*   `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: For providing localized weather forecasts and relevant crop information.

## 👨‍💻 Developed By & Ownership

This application is conceptualized, designed, and developed by **Prateek Kumar**.

## ⚖️ License & Intellectual Property

**© 2025-Present Prateek Kumar and RSVP Studios. All Rights Reserved.**

*   **Proprietary Software:** This is **NOT** an open-source project. The source code is hosted on GitHub strictly for demonstrative, educational, and explanatory purposes.
*   **Ownership:** The **Jai Kisan** app, its core ideas, source code, UI/UX designs, and all related intellectual property are the sole property of **Prateek Kumar**.
*   **Publisher:** This application is published and distributed under **RSVP Studios**.
*   **Studio Ownership:** **RSVP Studios** is wholly and exclusively owned by **Prateek Kumar**.
*   **Exclusive Usage:** The application, its source code, and all associated functionalities are strictly intended for demonstrative purposes and are authorized for use **exclusively by Prateek Kumar and his designated team members**.
*   **Rights Reserved:** All current assets, mechanics, and any future associated work or updates are strictly protected. No part of this application, its concept, or its presentation may be reproduced, distributed, copied, or transmitted in any form or by any means without the prior written permission of the owner.
*   **Protection:** This project is protected as intellectual property to prevent unauthorized replication of the app, its ideas, or its execution.

---
*Empowering farmers, securing the future.*
