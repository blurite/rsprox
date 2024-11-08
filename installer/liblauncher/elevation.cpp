#include <Windows.h>
#include <jni.h>

extern "C" JNIEXPORT jboolean JNICALL Java_net_runelite_launcher_Launcher_isProcessElevated(JNIEnv *env, jclass clazz, jlong pid) {
    HANDLE process = OpenProcess(PROCESS_QUERY_INFORMATION, FALSE, (DWORD)pid);
    if (process == nullptr) {
        return false;
    }

    BOOL ret = false;
    HANDLE hToken = nullptr;
    if (OpenProcessToken(process, TOKEN_QUERY, &hToken)) {
        TOKEN_ELEVATION elevation;
        DWORD returnLength;
        if (GetTokenInformation(hToken, TokenElevation, &elevation, sizeof(elevation), &returnLength)) {
            ret = elevation.TokenIsElevated;
        }
        CloseHandle(hToken);
    }

    CloseHandle(process);

    return (jboolean)ret;
}

extern "C" JNIEXPORT jlong JNICALL Java_net_runelite_launcher_Launcher_runas(JNIEnv *env, jclass clazz, jstring pathObj, jstring argsObj) {
    const jchar *pathString = env->GetStringChars(pathObj, nullptr);
    const jchar *argsString = env->GetStringChars(argsObj, nullptr);

    INT_PTR ret = (INT_PTR)ShellExecuteW(nullptr, L"runas", reinterpret_cast<const wchar_t *>(pathString), reinterpret_cast<const wchar_t *>(argsString),
                                         nullptr, SW_SHOWNORMAL);

    env->ReleaseStringChars(pathObj, pathString);
    env->ReleaseStringChars(argsObj, argsString);

    return static_cast<jlong>(ret);
}