# Webview Data Collector

## Overview

**Webview Data Collector** is a multi-component project designed for collecting and processing data from web interactions. The project includes an Android app, a browser extension, and server-side components for managing and analyzing collected data. It is designed to facilitate research by providing tools to study user interactions with web-based applications.

## Features

- **Android App**: A mobile application that provides an interface for data collection, surveys, and user interaction analysis.
- **Admin Panel**: A server-side dashboard to manage collected data, run queries, and oversee system configurations.
- **Browser Extension**: A lightweight extension for data interception and logging.
- **Interceptor Module**: A module to facilitate advanced data logging and analysis.

## Directory Structure

### 1. **Admin**
- Contains Docker and deployment scripts.
- Includes server-side code for managing data and rendering templates.
- **Key Files**: `Dockerfile`, `docker-compose.yml`, `requirements.txt`

### 2. **App**
- The Android application with a modular architecture.
- Includes activities for survey management, webview interactions, and logging.
- **Key Components**:
  - `src/main/java`: Core Kotlin code for Android functionalities.
  - `res`: UI resources including layouts, images, and strings.
  - `assets`: Bundled JavaScript for intercepting web requests.

### 3. **Extension**
- A browser extension to collect and process web data.
- Written in TypeScript with support for Webpack bundling.
- **Key Files**: `content.ts`, `manifest.json`

### 4. **Interceptor**
- Facilitates network request logging and data serialization.
- Designed as a separate module for extensibility.
- **Key File**: `index.ts`

## Installation

### Prerequisites
- Docker & Docker Compose for the admin panel.
- Android Studio for building and running the Android app.
- Node.js and Yarn for the browser extension and interceptor.

### Steps
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Setup the **Admin Panel**:
   ```bash
   cd admin
   docker-compose up
   ```
3. Update the code inside to match the URL where the admin panel is deployed. Example: Find and replace all instances of `kiran-research2.comminfo.rutgers.edu`.
4. Build the updated extension code:
  ```bash
  npm run build
  ```
4. Copy the generated `bundle.js` file into the `res/raw` directory inside the Android app code.
5. Build the **Android App**:
   - Open the `app` directory in Android Studio.
   - Build and run the app on an emulator or connected device.

## Android App Architecture

The Android app is designed as a combination of native Android components and GeckoView, a browser engine from Firefox.

- **Native Android Components**:
  - Surveys, permission checking, and ancillary features like detecting usage of other Android apps are implemented using native Android components.

- **GeckoView Integration**:
  - Initially, WebView was used to handle web interactions within the app. However, due to limitations in features and control, GeckoView replaced WebView. GeckoView offers greater flexibility and advanced capabilities for handling web content.

- **Custom Extension**:
  - To control and enhance interactions with Instagram and Uber, a custom extension is developed and packaged with the browser in the app. This extension is designed to:
    - Override Content Security Policy (CSP).
    - Intercept network requests.
    - Handle page loads.
    - Inject custom scripts to interact with web applications.
  - This level of control is essential for integrating with the specific functionalities of Instagram and Uber.

- **Messaging APIs**:
  - Communication between the native Android code and the extension code is facilitated by GeckoView's messaging APIs. These APIs enable bidirectional messaging, allowing the Android app to send commands to the extension and receive responses.

## Usage

- Launch the admin panel and log in to view collected data.
- Use the Android app to start a survey or collect user interaction data.
- Enable the browser extension for live web request interception and logging.

## Contribution

Contributions to this project are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Submit a pull request with a detailed explanation of your changes.

## License

This project is licensed under the [MIT License](LICENSE).
