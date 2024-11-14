#include <Windows.h>
#include <jni.h>

void rlThrow(JNIEnv *env, const char *msg) {
    if (env->ExceptionCheck()) {
        return;
    }

    jclass clazz = env->FindClass("java/lang/RuntimeException");
    int lastError = GetLastError();
    if (lastError) {
        char buf[256] = {0};
        snprintf(buf, sizeof(buf), "%s (%d)", msg, lastError);
        env->ThrowNew(clazz, buf);
    } else {
        env->ThrowNew(clazz, msg);
    }
}

extern "C" JNIEXPORT jstring JNICALL Java_net_runelite_launcher_Launcher_regQueryString(JNIEnv *env, jclass clazz, jstring lpSubKeyJs, jstring lpValueJs) {
    const jchar *lpSubKey = env->GetStringChars(lpSubKeyJs, nullptr);
    const jchar *lpValue = env->GetStringChars(lpValueJs, nullptr);
    wchar_t pvData[MAX_PATH];
    DWORD pcbData = sizeof(pvData);

    LSTATUS status = RegGetValueW(HKEY_CURRENT_USER, reinterpret_cast<const wchar_t *>(lpSubKey), reinterpret_cast<const wchar_t *>(lpValue), RRF_RT_REG_SZ,
                                  nullptr, pvData, &pcbData);

    env->ReleaseStringChars(lpValueJs, lpValue);
    env->ReleaseStringChars(lpSubKeyJs, lpSubKey);

    if (status != ERROR_SUCCESS) {
        rlThrow(env, "RegGetValue() failed");
        return nullptr;
    }

    if (pcbData / 2 < 1) {
        // pcbData includes the terminating null character, so should always be at least 2 bytes
        rlThrow(env, "RegGetValue() returned no data");
        return nullptr;
    }

    return env->NewString(reinterpret_cast<const jchar *>(pvData), pcbData / 2 - 1);
}

extern "C" JNIEXPORT jboolean JNICALL Java_net_runelite_launcher_Launcher_regDeleteValue(JNIEnv *env, jclass clazz, jstring keyObj, jstring subKeyObj,
                                                                                         jstring valueObj) {
    const jchar *keyString = env->GetStringChars(keyObj, nullptr);
    const jchar *subKeyString = env->GetStringChars(subKeyObj, nullptr);
    const jchar *valueString = env->GetStringChars(valueObj, nullptr);
    jboolean success = false;

    HKEY hKey;
    if (wcscmp(reinterpret_cast<const wchar_t *>(keyString), L"HKCU") == 0) {
        hKey = HKEY_CURRENT_USER;
    } else if (wcscmp(reinterpret_cast<const wchar_t *>(keyString), L"HKLM") == 0) {
        hKey = HKEY_LOCAL_MACHINE;
    } else {
        rlThrow(env, "invalid keyObj");
        goto out;
    }

    HKEY hKeyDel = nullptr;
    if (RegOpenKeyExW(hKey, reinterpret_cast<const wchar_t *>(subKeyString), 0, KEY_SET_VALUE, &hKeyDel) == ERROR_SUCCESS) {
        success = RegDeleteValueW(hKeyDel, reinterpret_cast<const wchar_t *>(valueString)) == ERROR_SUCCESS;
        RegCloseKey(hKeyDel);
    }

out:
    env->ReleaseStringChars(keyObj, keyString);
    env->ReleaseStringChars(subKeyObj, subKeyString);
    env->ReleaseStringChars(valueObj, valueString);

    return success;
}