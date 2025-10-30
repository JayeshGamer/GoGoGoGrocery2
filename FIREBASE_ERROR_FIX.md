# Firebase Connection Error - Troubleshooting Guide

## Error Analysis

**Error:** `java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'`
**Warning:** `Firestore Stream closed with status: CANCELLED - Disconnecting idle stream`

These errors indicate:
1. Google Play Services authentication issue
2. Firestore connection timeout/idle stream closure

## Fixes Applied

### ✅ Fix 1: Added Required Permissions to AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### ✅ Fix 2: Enabled Cleartext Traffic
```xml
android:usesCleartextTraffic="true"
```

### ✅ Fix 3: Updated FirebaseManager with Offline Persistence
- Enabled Firestore offline persistence
- Set unlimited cache size
- Prevents connection timeouts

## Additional Solutions to Try

### Solution 1: Update Google Play Services on Device/Emulator

#### For Physical Device:
1. Open Google Play Store
2. Search for "Google Play Services"
3. Update to latest version
4. Restart device

#### For Emulator:
1. AVD Manager → Your Device → Edit
2. Ensure "Google Play" is enabled (not "Google APIs")
3. Start emulator
4. Wait for Play Store to update services automatically

### Solution 2: Clear App Data & Reinstall

```cmd
# Uninstall app
adb uninstall com.grocerygo.app

# Clear Google Play Services cache (on device/emulator)
adb shell pm clear com.google.android.gms

# Rebuild and install
cd C:\Users\vmand\AndroidStudioProjects\GoGoGoGrocery
gradlew.bat clean
gradlew.bat assembleDebug
gradlew.bat installDebug
```

### Solution 3: Check Firebase Console Settings

1. Go to: https://console.firebase.google.com/
2. Select project: **gogogogrocery**
3. Project Settings → General
4. Verify package name is: `com.grocerygo.app`
5. Download latest `google-services.json` if needed

### Solution 4: Add SHA-1 Certificate (Important!)

The error may occur because Firebase doesn't recognize your app's signing certificate.

#### Get SHA-1 from Debug Keystore:
```cmd
cd C:\Users\vmand\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copy the SHA-1 fingerprint, then:

1. Go to Firebase Console → Project Settings
2. Scroll to "Your apps" section
3. Click your Android app
4. Click "Add fingerprint"
5. Paste the SHA-1
6. Download the updated `google-services.json`
7. Replace the file in your `app/` folder
8. Rebuild the app

### Solution 5: Enable Firestore in Firebase Console

1. Go to Firebase Console → Firestore Database
2. Click "Create Database"
3. Choose "Start in Test Mode" (for development)
4. Select a location (closest to you)
5. Click "Enable"

**Test Mode Rules** (good for 30 days):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.time < timestamp.date(2025, 11, 12);
    }
  }
}
```

### Solution 6: Verify Internet Connection

The app MUST have internet access to connect to Firebase:

```cmd
# Test from emulator/device
adb shell ping -c 4 8.8.8.8
```

If no response, check:
- Emulator network settings
- Device WiFi/mobile data
- Firewall settings

### Solution 7: Use Firebase Emulator (Alternative for Development)

If you keep having connection issues, use local Firebase emulators:

```cmd
# Install Firebase CLI
npm install -g firebase-tools

# Login and init
firebase login
firebase init emulators

# Start emulators
firebase emulators:start
```

Then update your app to point to local emulators (in Application class or MainActivity):

```java
// For Firestore Emulator
FirebaseFirestore db = FirebaseFirestore.getInstance();
db.useEmulator("10.0.2.2", 8080); // For emulator
// db.useEmulator("localhost", 8080); // For physical device

// For Auth Emulator
FirebaseAuth auth = FirebaseAuth.getInstance();
auth.useEmulator("10.0.2.2", 9099);
```

## Testing After Fixes

### Step 1: Rebuild & Reinstall
```cmd
cd C:\Users\vmand\AndroidStudioProjects\GoGoGoGrocery
gradlew.bat clean
gradlew.bat assembleDebug
gradlew.bat installDebug
```

### Step 2: Check Logcat
```cmd
adb logcat | findstr "Firestore"
adb logcat | findstr "FirebaseAuth"
adb logcat | findstr "GooglePlayServices"
```

### Step 3: Test Firebase Connection

Open the app and try to:
1. Register/Login (tests Firebase Auth)
2. Go to Profile → Click "Populate Firebase Data" (tests Firestore write)
3. Go to Home screen (tests Firestore read)

### Expected Success Logs:
```
D/FirebaseManager: Firestore settings configured successfully
D/FirebaseAuth: Firebase Auth initialized
D/Firestore: Connection established
```

## Common Causes & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| Unknown package name | Outdated Play Services | Update Google Play Services |
| Stream CANCELLED | Network timeout | Enable offline persistence ✅ |
| Permission denied | Missing SHA-1 | Add SHA-1 to Firebase Console |
| Connection failed | Firestore not enabled | Enable in Firebase Console |
| Security exception | Package mismatch | Verify package name in Firebase |

## Quick Checklist

- [x] Added INTERNET permission
- [x] Added ACCESS_NETWORK_STATE permission
- [x] Enabled cleartext traffic
- [x] Configured Firestore offline persistence
- [ ] Updated Google Play Services on device
- [ ] Added SHA-1 to Firebase Console (IMPORTANT!)
- [ ] Verified Firestore is enabled in Firebase Console
- [ ] Checked internet connectivity
- [ ] Rebuilt and reinstalled app

## Most Likely Fix for Your Error

Based on the error `Unknown calling package name 'com.google.android.gms'`, you need to:

### 🎯 **Add SHA-1 Certificate to Firebase Console** (Do this first!)

1. Get your debug SHA-1:
```cmd
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

2. Copy the SHA-1 line (looks like: `A1:B2:C3:D4:...`)

3. Add it to Firebase Console:
   - https://console.firebase.google.com/project/gogogogrocery/settings/general
   - Your apps → Android app → Add fingerprint
   - Paste SHA-1 → Save

4. Rebuild the app

This should resolve the Google Play Services authentication error!

## Still Not Working?

If issues persist after trying all solutions:

1. Check Firebase project quota/billing
2. Verify Firebase project is active (not deleted/suspended)
3. Try creating a new Firebase project
4. Check Android Studio Logcat for more specific errors
5. Enable Firebase debug logging:
```cmd
adb shell setprop log.tag.FirebaseAuth DEBUG
adb shell setprop log.tag.FirebaseFirestore DEBUG
```

## Support Resources

- Firebase Documentation: https://firebase.google.com/docs/android/setup
- Stack Overflow: Search for your specific error
- Firebase Support: https://firebase.google.com/support

---

**Note:** The fixes have already been applied to your code. Just rebuild the app and follow the SHA-1 certificate steps above!

