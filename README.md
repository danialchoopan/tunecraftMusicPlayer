# 🎵 TuneCraft Music Player | موزیک پلیر تیون کرافت

<p align="center">
  <img src="app/src/main/res/mipmap-xhdpi/ic_launcher.png" alt="TuneCraft Logo" width="120" />
</p>

<p align="center">
  <b>یک موزیک پلیر مدرن، قدرتمند، هوشمند و فوق‌العاده سریع اختصاصی اندروید با پشتیبانی کامل از زبان فارسی و انگلیسی</b><br>
  <i>A modern, ultra-fast & feature-packed Android Music Player built with Jetpack Compose & Clean Architecture.</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-SDK%2034%2B-brightgreen.svg" alt="Android SDK" />
  <img src="https://img.shields.io/badge/Language-Kotlin%20100%25-blue.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions%20%28production%29-orange.svg" alt="GitHub Actions" />
</p>

---

## 🌟 ویژگی‌های جدید و پیشرفته | Latest Features & Enhancements

### 🔀 دکمه پخش تصادفی سریع (Quick Shuffle Buttons)
* **شفل یک‌لمسی در صفحه اصلی**: افزودن دکمه شفل مستقیم (Shuffle) روی کارت‌های «علاقه‌مندی‌ها» و «کل آهنگ‌ها» در صفحه خانه جهت شروع پخش تصادفی بدون نیاز به ورود به لیست.
* **پخش دیوانه‌وار لیست‌ها**: پشتیبانی کامل از شفل آنی در تمام لیست‌های تفکیک‌شده و فیلتر شده.

### 🎛️ اکولایزر پیشرفته و افکت‌های صوتی DSP (Advanced Equalizer & Sound FX)
* **حالت پیش‌فرض خاموش (Default OFF State)**: اکولایزر به طور پیش‌فرض خاموش است تا اصالت صدای اصلی حفظ شود و کاربر در صورت نیاز آن را فعال کند.
* **یکپارچه‌سازی نوار عنوان (Unified Toolbar Navigation)**: انتقال عنوان اکولایزر به TopAppBar اصلی برنامه و حذف عناصر اضافی برگشت جهت تجربه‌ کاربری یکدست.
* **پروفایل‌های صوتی سریع (Quick Sound Profiles)**: تغییر حالت صوتی با یک لمس برای «حالت هدفون»، «بیس ماشین»، «اسپیکر قوی» و «پادکست/وکال».
* **نمودار فرکانسی زنده (Live Frequency Curve Canvas)**: رسم منحنی پاسخ فرکانسی زنده با قابلیت شخصی‌سازی رنگ نئونی نمودار.
* **افکت‌های صوتی پیشرفته (DSP Effects)**:
  * **Bass Boost**: تقویت عمیق فرکانس‌های پایین
  * **3D Spatializer**: سه‌بعدی‌سازی و گسترش میدان استریو
  * **Loudness Enhancer**: افزایش قدرتمند حجم صدای خروجی (Volume Punch)
  * **Reverb Environment**: طنین محیطی (اتاق کوچک، سالن بزرگ، کنسرت، Plate)
  * **Stereo Balance**: تنظیم دقیق بالانس باند چپ و راست هدفون (L/R)
* **ذخیره و مدیریت پیش‌فرض‌های شخصی (Custom Preset Manager)**.

### ⚙️ گردش کاری اختصاصی CI/CD روی شاخه Production (GitHub Actions Workflow)
* **بیلد خودکار روی Branch `production`**: تنظیم اکشن GitHub Actions جهت کامپایل و بیلد خودکار فایل‌های APK و Android App Bundle (AAB) تنها زمانی که تغییرات روی شاخه `production` پوش شوند.
* **خروجی‌های مستقیم (Artifacts)**: ارائه لینک دانلود مستقیم نسخه Debug APK، Release APKs و Release AAB در رزومه‌های بیلد.

---

## 🌟 سایر ویژگی‌های کلیدی | Key General Features

### ⚡ بهینه‌سازی کارایی و سرعت (High Performance & Smooth Scrolling)
* **رابط کاربری بدون لگ (Zero Lag Library)**: بازطراحی کامل لیست کتابخانه با کلیدهای یکتا و پایدار (`key = { it.id }`) جهت پیمایش فوق‌العاده روان حتی در کتابخانه‌های با هزاران آهنگ.
* **ترتیب پیش‌فرض جدیدترین‌ها (Recently Added Default)**: نمایش خودکار آهنگ‌های اخیراً اضافه شده در بالای کتابخانه.
* **نشانگر بارگذاری و اسکن حافظه (Scan Loading Indicators)**: نمایش لودینگ و نوار پیشرفت زنده هنگام باز شدن برنامه و اسکن فایل‌های صوتی حافظه.

### 📱 پشتیبانی کامل از تبلت‌ها و مانیتورهای خودرو (Tablet & Car Screen Responsive Layout)
* **طراحی واکنش‌گرا (Adaptive UI)**: چیدمان شبکه‌ای (Grid View) ۲ ستونه در صفحات عریض (`> 600dp`) ویژه تبلت‌ها، مانیتورهای خودرو (Car Head Units) و گوشی‌های تاشو.
* **سازگاری کامل با سیستم صوتی و مانیتور خودرو**: دکمه‌های کنترل بزرگ و خوانا با کنتراست بالا برای استفاده راحت هنگام رانندگی.

### 🔤 تایپوگرافی اختصاصی و فونت وزیرمتن (Vazirmatn Persian Typography)
* **فونت وزیرمتن فارسی (Vazirmatn Font)**: استفاده مستقیم از فونت زیبای وزیرمتن در تمامی وزن‌ها جهت نمایش استاندارد و چشم‌نواز متون فارسی.

### 🧠 شفل هوشمند و الگوریتم‌های هوش مصنوعی (Smart AI Shuffle)
* **Smart Shuffle**: پخش هوشمند ترانه‌ها بر اساس زمان روز (صبح، عصر، شب)، میزان استقبال شما از آهنگ‌ها و الگوی اخیر.
* **Smart DJ & Auto Queue**: پیشنهاد آهنگ بعدی به صورت خودکار بر اساس سبک و حال‌وهوای ترانه فعلی.

### 🚘 حالت اختصاصی رانندگی (Advanced Car Mode)
* **کلیدهای فوق‌العاده بزرگ**: دکمه‌های کنترل پخش با اندازه بزرگ (۹۲dp) جهت استفاده آسان و بدون حواس‌پرت در حین رانندگی.
* **جستجوی صوتی (Hands-Free Voice Search)**: امکان پخش مستقیم ترانه با گفتن نام آهنگ یا خواننده.
* **ژست‌های حرکتی سریع (Swipe Gestures)**: تعویض آهنگ با کشیدن انگشت روی کارت پخش به سمت چپ یا راست.
* **کنتراست بالای شب (AMOLED Night Vision)**: تم تاریک خالص با کنتراست فوق‌العاده بالا برای عدم ایجاد چشم‌زدگی در تاریکی خودرو.

### 🎨 انیمیشن‌ها و تم‌های بصری قابل شخصی‌سازی (Customizable Animations & Themes)
* **کنترل سبک انیمیشن (Animation Motion Styles)**: Dynamic Spring, Smooth Slide, Soft Fade, Bounce Accent و حالت خاموش (Instant).
* **۹ پوسته و تم رنگی خیره‌کننده**: بنفش نئونی، کهربایی، زمردی، آبی اقیانوسی، رز و شرابی، مشکی خالص (AMOLED)، هلویی، خاکستری نوردیک و تم پویای اندروید (Material You).

---

## 🛠️ معماری و تکنولوژی‌ها | Tech Stack & Architecture

* **زبان برنامه نویسی**: 100% Kotlin
* **رابط کاربری (UI)**: Jetpack Compose + Material Design 3 (M3) Adaptive
* **تایپوگرافی**: Vazirmatn Custom Font
* **داده‌های محلی (Database)**: Room Database (KSP)
* **تنظیمات برنامه (Preferences)**: Jetpack DataStore Preferences
* **سرویس پخش صدا (Audio Engine)**: Media3 / ExoPlayer + AudioEffect DSP Pipeline
* **معماری نرم‌افزار**: MVVM + Clean Architecture + StateFlow & Coroutines
* **اتوماسیون و CI/CD**: GitHub Actions (Production Branch Target, JDK 17 + Gradle setup)

---

## 🚀 راهنمای اجرا و ساخت پروژه | Build & Setup Instructions

### پیش‌نیازها (Prerequisites):
* Android Studio Ladybug (2024.2) یا جدیدتر
* JDK 17
* Android SDK 34 یا بالاتر

### مراحل اجرا (Steps):
1. پروژه را کپی یا دانلود کنید (Clone repository):
   ```bash
   git clone https://github.com/danialchoopan/TuneCraftMusicPlayer.git
   ```
2. پروژه را در **Android Studio** باز کنید.
3. اجازه دهید Gradle Sync کامل انجام شود.
4. پروژه را روی دستگاه واقعی، تبلت یا شبیه‌ساز (Emulator) اجرا نمائید:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📜 لیسانس | License

توسعه داده شده توسط **دانیال چوپان** (Danial Choopan).
طراحی شده با عشق برای تجربه عالی شنیدن موسیقی! ❤️
