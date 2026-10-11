# JIGAR AI — एक साथ Android प्रोजेक्ट

इस ZIP में Android ऐप और Node.js backend दोनों हैं। यह शुरुआती project है, तैयार APK नहीं।

## शामिल सुविधाएँ
- हिंदी voice input (Android speech recognition)
- हिंदी में बोलकर जवाब (फोन में Hindi TTS voice उपलब्ध होने पर)
- AI chat backend
- AI image-generation endpoint
- YouTube/Chrome खोलने के basic voice commands
- Google internet search और browser में मौसम खोज
- `Hey Jigar` phrase को active voice session में पहचानने का basic व्यवहार

## ज़रूरी बात
- `Hey Jigar` हमेशा background में सुनने वाला wake word अभी नहीं है। इसके लिए अलग wake-word engine/SDK और Android foreground service, permissions, battery/privacy handling की ज़रूरत होती है।
- मौसम अभी weather API से structured forecast नहीं लाता; Google search खोलता है।
- `/video` endpoint अभी स्पष्ट रूप से unconfigured है। वीडियो generation के लिए compatible provider/API, job polling और output storage जोड़ना होगा।
- Image API का access/model availability आपके API account पर निर्भर है। Generated media की hosting भी production में जोड़नी चाहिए।
- API key कभी Android app में न डालें। केवल server environment variable में रखें। API usage पर शुल्क हो सकता है।
- यह प्रोजेक्ट phone-only Android Studio build नहीं करता; ZIP को कंप्यूटर पर Android Studio में खोलकर APK बनाया जा सकता है।

## Android app चलाएँ
1. ZIP extract करें।
2. Android Studio में `JIGAR_AI_AllInOne` फ़ोल्डर खोलें।
3. Gradle sync पूरा होने दें।
4. `app/src/main/java/com/jigar/ai/MainActivity.kt` में `backendUrl` को अपने deployed backend के HTTPS URL से बदलें।
5. Android फोन जोड़कर Run दबाएँ और microphone permission दें।

## Backend चलाएँ
Node.js 18+ install करें। Terminal में `backend` फ़ोल्डर खोलें:

```bash
npm install
```

Linux/macOS:
```bash
export OPENAI_API_KEY="अपनी_API_KEY"
npm start
```

Windows PowerShell:
```powershell
$env:OPENAI_API_KEY="अपनी_API_KEY"
npm start
```

Backend डिफ़ॉल्ट रूप से port 3000 पर चलता है। फोन पर इस्तेमाल करने के लिए इसे HTTPS hosting पर deploy करें। फोन में `localhost` फोन को ही दर्शाता है, आपके कंप्यूटर को नहीं।

## Build APK
Android Studio → Build → Build APK(s). मेनू का नाम Android Studio version के अनुसार थोड़ा अलग हो सकता है।

## Production से पहले
Backend authentication, rate limiting, request limits, privacy notice, error monitoring और secure media storage जोड़ें। बिना authentication के सार्वजनिक API endpoint deploy न करें।
