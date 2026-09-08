# NezamMonitor Android

## سیستم مانیتور نظم مهندسی یزد — نسخه اندروید

### نحوه Build

**Cloud Build (توصیه شده):**
```bash
git push origin main
# GitHub Actions خودکار APK تولید می‌کند
# Artifact را از GitHub Releases دانلود کنید
```

**Local Build (فقط اگر Android Studio نصب باشد):**
```bash
./gradlew assembleDebug
# APK در app/build/outputs/apk/debug/
```

### ساختار پروژه
```
app/src/main/java/com/nezammonitor/app/
├── MainActivity.kt          ← Activity اصلی
├── data/
│   ├── CaseEntity.kt         ← Room Entity
│   ├── NezamDao.kt           ← DAO
│   ├── NezamDatabase.kt      ← Database
│   └── ImportEngine.kt       ← Import از .nzmdata
└── ui/
    └── NezamMonitorApp.kt    ← Compose UI
```

### Import Data
1. از Windows: منوی "خروجی اندروید" → فایل .nzmdata
2. در Android: دکمه "ورود اطلاعات" → انتخاب فایل .nzmdata

### نیازمندی‌ها
- **بدون Android Studio** ← فقط Git + Push
- **Cloud Build** ← GitHub Actions APK تولید می‌کند
