# 🥦 Food Health Rating App

An Android application that scans the barcode of a food product and displays its **nutritional information** along with a calculated **health score** (out of 10), using data from Open Food Facts and USDA FoodData Central APIs.

---

## 📱 Features

- 📷 Real-time **barcode scanning** using Google ML Kit
- 🔎 Fetches nutritional data from:
  - [Open Food Facts](https://world.openfoodfacts.org/)
  - [USDA FoodData Central](https://fdc.nal.usda.gov/)
- ⚖️ Calculates a simple **health score** based on sugar, fat, sodium, fiber, and protein content
- 🧠 Uses a refined scoring algorithm inspired by **Nutri-Score principles**
- 🕹 Smooth and responsive **CameraX** integration
- ⏳ Displays loading indicator while fetching data
- 📊 Clean and readable **nutritional summary UI**

---

## 🎯 How It Works

1. Open the app — the camera launches automatically.
2. Scan the barcode of a food product.
3. App fetches data from **Open Food Facts**; if unavailable, it queries **USDA**.
4. Nutritional values are extracted and displayed.
5. A **health score** is calculated and shown based on:
   - ✅ Low sugar, fat, sodium
   - ✅ High fiber and protein

---

## 🧮 Health Score Calculation (0–10)

| Nutrient   | Condition           | Points |
|------------|---------------------|--------|
| Sugar      | < 10g/100g          | +2     |
| Fat        | < 5g/100g           | +2     |
| Sodium     | < 200mg/100g        | +2     |
| Fiber      | ≥ 3g/100g           | +2     |
| Protein    | ≥ 5g/100g           | +2     |

- Maximum score: **10**
- The healthier the product, the higher the score!

---

## 🛠 Tech Stack

- **Kotlin**
- **Android Studio**
- **CameraX API**
- **Google ML Kit Barcode Scanner**
- **OkHttp** for HTTP requests
- **OpenFoodFacts API**
- **USDA FoodData Central API**

---

## 📸 Screenshots

| Camera View | Health Score + Nutrition Info |
|-------------|-------------------------------|
| 📷 Barcode scanning in real-time | 🧾 Health summary at the bottom |


<img width="1080" height="2400" alt="Screenshot_20250907_215422" src="https://github.com/user-attachments/assets/a1b1a608-fbfb-4c0d-af94-1317476c26c7" />

<img width="1080" height="2400" alt="Screenshot_20250907_215538" src="https://github.com/user-attachments/assets/692e186e-fd69-4c64-aa93-406f8279a072" />

<img width="1080" height="2400" alt="Screenshot_20250907_215658" src="https://github.com/user-attachments/assets/90823ed8-d60a-4bd2-8cae-4f7c34046e2a" />


---

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK: 21
- Permissions: Camera access

### Clone and Build

```bash
git clone https://github.com/yourusername/food-health-rating-app.git
cd food-health-rating-app
