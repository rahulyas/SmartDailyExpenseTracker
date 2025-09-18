# Smart Daily Expense Tracker 💰📊

Smart Daily Expense Tracker is an **Android application** designed to help users efficiently manage and analyze their daily expenses.  
Built with **Jetpack Compose**, **Clean Architecture**, and **MVVM**, the app provides a seamless and modern experience for tracking spending, generating reports, and visualizing financial data.

---

## ✨ Features

- **Expense Management** – Add, edit, and delete daily expense entries with category-based organization.
- **Real-Time Charts** – Interactive expense charts powered by **MPAndroidChart** for quick financial insights.
- **PDF Export** – Generate detailed expense reports in PDF format using **iText7** and **Html2Pdf**.
- **Data Persistence** – Securely store expenses using **Room Database** and user preferences with **DataStore**.
- **Dark/Light Theme** – Dynamic UI powered by **Jetpack Compose Material 3**.
- **Backup & Restore** – Keep your financial data safe and portable.
- **Search & Filter** – Quickly find expenses by date, category, or keyword.

---

## 🏗️ Tech Stack

| Category           | Technology / Library           |
|--------------------|---------------------------------|
| **UI**            | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Architecture**  | Clean Architecture + MVVM       |
| **Dependency Injection** | [Hilt (Dagger)](https://developer.android.com/training/dependency-injection/hilt-android) |
| **Database**      | [Room](https://developer.android.com/training/data-storage/room) |
| **Preferences**   | [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) |
| **Charts**        | [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) |
| **PDF Generation**| [iText7](https://itextpdf.com/) , [Html2Pdf](https://github.com/openhtmltopdf/openhtmltopdf) |

---

## 📂 Project Structure

The app follows **Clean Architecture** and **MVVM**, separating concerns into:
```
data/          -> Room DB, DataStore, repositories
domain/        -> Use-cases and business logic
ui/            -> Jetpack Compose screens, ViewModels
di/            -> Hilt modules for dependency injection
```

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/rahulyas/SmartDailyExpenseTracker.git
   ```
2. Open the project in **Android Studio** (Giraffe+).
3. Sync Gradle to download dependencies.
4. Run the app on an Android device or emulator (**minSdk 24+**).

---

## 📸 Screenshots

| Dashboard | Add Expense | Charts | Reports |
|-----------|-------------|-------|--------|
| <img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/edd1eeac-8f37-4a51-9d1b-be2a3742ee37" /> |<img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/c1a5b2cf-322b-4c84-8f73-e6db4335f843" />| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/363b3a3e-8fae-4992-ad5f-d445214fffcb" />| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/11d0f1fc-faf3-488c-8b22-62d77d4ff9c0" /> |
| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/8880a851-8373-46e8-b835-76cac95c3931" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/fb3e7c7e-106e-4ff7-9274-062396dfeec4" /> | <img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/408aa8f5-b8d6-4e81-87f7-a83688d9e2f5" />| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/7496ac9d-b346-481f-b097-fa7ae4631389" /> |
| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/15b1dffb-e5ce-4c10-b405-ad5329330f8d" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/1a048a7a-3a57-49aa-9495-2184f1795453" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/bdc31c5e-a3e0-4edb-81ff-1b8fea3c6ec1" /> | <img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/2dd2654a-1331-4765-9f30-fd2a2c66678b" /> |
| <img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/4e501a82-6be4-4c94-8345-43551881eff0" />| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/dc70aef1-b310-46d3-a1e8-26e9ec5fb973" />| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/614ae2ed-8da6-49cd-81e3-ba45b21de7fd" />| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/aba92340-f585-4b1f-86df-3c315c97ae63" /> |
| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/8944377a-a653-4537-aa99-8b16c944e252" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/60efd5e3-b88d-40a3-a275-771febde0ad6" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/c966a257-a7d4-4dd1-9859-433f62b2e44d" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/99d89a6f-c1c7-4ef8-a29c-00ef4814f73a" /> |
|| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/d7a9b6d4-dd6a-4f69-bf6c-0d34df89fe22" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/6bf78587-c93f-4928-9c7d-ffa0bfb1db2c" />||
*(More screenshots are available in the repository)*

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 💡 Future Enhancements

- Cloud backup & sync with Google Drive
- Multi-currency support
- Advanced analytics and AI-powered budgeting tips
