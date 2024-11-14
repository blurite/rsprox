#include <jni.h>
#include <stdio.h>
#include <windows.h>

#include <set>
#include <shared_mutex>
#include <string>

#include "detours.h"

using namespace std;

static_assert(sizeof(wchar_t) == 2);
static_assert(sizeof(jchar) == 2);

static HMODULE K32;
static HMODULE KB;

static decltype(LoadLibraryA) *K32LoadLibraryA;
static decltype(LoadLibraryExA) *K32LoadLibraryExA;
static decltype(LoadLibraryW) *K32LoadLibraryW;
static decltype(LoadLibraryExW) *K32LoadLibraryExW;

static decltype(LoadLibraryA) *KBLoadLibraryA;
static decltype(LoadLibraryExA) *KBLoadLibraryExA;
static decltype(LoadLibraryW) *KBLoadLibraryW;
static decltype(LoadLibraryExW) *KBLoadLibraryExW;

static set<string> blacklistedDlls;
static set<wstring> blacklistedDllsW;
static shared_mutex dllMutex;

static string getFileName(const string &path) { return path.substr(path.find_last_of("/\\") + 1); }
static wstring getFileName(const wstring &path) { return path.substr(path.find_last_of(L"/\\") + 1); }

static bool isBlacklistedDll(LPCSTR lpLibFileName) {
    string dll = getFileName(lpLibFileName);

    shared_lock<shared_mutex> g(dllMutex);
    return blacklistedDlls.find(dll) != blacklistedDlls.end();
}

static bool isBlacklistedDll(LPCWSTR lpLibFileName) {
    wstring dll = getFileName(lpLibFileName);

    shared_lock<shared_mutex> g(dllMutex);
    return blacklistedDllsW.find(dll) != blacklistedDllsW.end();
}

// kernel32
static HMODULE WINAPI RuneLiteK32LoadLibraryA(LPCSTR lpLibFileName) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }

    return K32LoadLibraryA(lpLibFileName);
}

static HMODULE WINAPI RuneLiteK32LoadLibraryExA(LPCSTR lpLibFileName, HANDLE hFile, DWORD dwFlags) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }

    return K32LoadLibraryExA(lpLibFileName, hFile, dwFlags);
}

static HMODULE WINAPI RuneLiteK32LoadLibraryW(LPCWSTR lpLibFileName) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }

    return K32LoadLibraryW(lpLibFileName);
}

static HMODULE WINAPI RuneLiteK32LoadLibraryExW(LPCWSTR lpLibFileName, HANDLE hFile, DWORD dwFlags) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }

    return K32LoadLibraryExW(lpLibFileName, hFile, dwFlags);
}

// kernelbase
static HMODULE WINAPI RuneLiteKBLoadLibraryA(LPCSTR lpLibFileName) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }
    return KBLoadLibraryA(lpLibFileName);
}

static HMODULE WINAPI RuneLiteKBLoadLibraryExA(LPCSTR lpLibFileName, HANDLE hFile, DWORD dwFlags) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }
    return KBLoadLibraryExA(lpLibFileName, hFile, dwFlags);
}

static HMODULE WINAPI RuneLiteKBLoadLibraryW(LPCWSTR lpLibFileName) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }
    return KBLoadLibraryW(lpLibFileName);
}

static HMODULE WINAPI RuneLiteKBLoadLibraryExW(LPCWSTR lpLibFileName, HANDLE hFile, DWORD dwFlags) {
    if (isBlacklistedDll(lpLibFileName)) {
        SetLastError(ERROR_NOT_SUPPORTED);
        return nullptr;
    }
    return KBLoadLibraryExW(lpLibFileName, hFile, dwFlags);
}

extern "C" JNIEXPORT void JNICALL Java_net_runelite_launcher_Launcher_setBlacklistedDlls(JNIEnv *env, jclass clazz, jobjectArray arrDlls) {
    lock_guard<shared_mutex> g(dllMutex);
    jsize len = env->GetArrayLength(arrDlls);

    blacklistedDlls.clear();
    for (jsize i = 0; i < len; ++i) {
        jstring jsDll = static_cast<jstring>(env->GetObjectArrayElement(arrDlls, i));

        {
            const char *str = env->GetStringUTFChars(jsDll, nullptr);
            if (str != nullptr) {
                blacklistedDlls.insert(str);
            }
            env->ReleaseStringUTFChars(jsDll, str);
        }

        {
            const jchar *str = env->GetStringChars(jsDll, nullptr);
            if (str != nullptr) {
                blacklistedDllsW.insert(reinterpret_cast<const wchar_t *>(str));
            }
            env->ReleaseStringChars(jsDll, str);
        }
    }
}

#define LoadAndAttach(module, name)                                     \
    do {                                                                \
        module##name = (decltype(name) *)GetProcAddress(module, #name); \
        if (module##name != nullptr) {                                  \
            DetourAttach(&module##name, RuneLite##module##name);        \
        }                                                               \
    } while (0)

#define Detach(module, name)                                     \
    do {                                                         \
        if (module##name != nullptr) {                           \
            DetourDetach(&module##name, RuneLite##module##name); \
        }                                                        \
    } while (0)

BOOL WINAPI DllMain(HINSTANCE hinst, DWORD dwReason, LPVOID reserved) {
    if (DetourIsHelperProcess()) {
        return TRUE;
    }

    if (dwReason == DLL_PROCESS_ATTACH) {
        K32 = LoadLibraryW(L"kernel32.dll");
        KB = LoadLibraryW(L"kernelbase.dll");
        if (K32 == nullptr || KB == nullptr) {
            return TRUE;
        }

        DetourRestoreAfterWith();

        DetourTransactionBegin();
        DetourUpdateThread(GetCurrentThread());

        LoadAndAttach(K32, LoadLibraryA);
        LoadAndAttach(K32, LoadLibraryExA);
        LoadAndAttach(K32, LoadLibraryW);
        LoadAndAttach(K32, LoadLibraryExW);

        LoadAndAttach(KB, LoadLibraryA);
        LoadAndAttach(KB, LoadLibraryExA);
        LoadAndAttach(KB, LoadLibraryW);
        LoadAndAttach(KB, LoadLibraryExW);

        LONG error = DetourTransactionCommit();

        if (error != NO_ERROR) {
            fprintf(stderr, "error attaching detours: %ld\n", error);
            fflush(stderr);
        }
    } else if (dwReason == DLL_PROCESS_DETACH) {
        DetourTransactionBegin();
        DetourUpdateThread(GetCurrentThread());

        Detach(K32, LoadLibraryA);
        Detach(K32, LoadLibraryExA);
        Detach(K32, LoadLibraryW);
        Detach(K32, LoadLibraryExW);

        Detach(KB, LoadLibraryA);
        Detach(KB, LoadLibraryExA);
        Detach(KB, LoadLibraryW);
        Detach(KB, LoadLibraryExW);

        LONG error = DetourTransactionCommit();

        if (error != NO_ERROR) {
            fprintf(stderr, "error detaching detours: %ld\n", error);
            fflush(stderr);
        }

        if (K32 != nullptr) {
            FreeLibrary(K32);
            K32 = nullptr;
        }
        if (KB != nullptr) {
            FreeLibrary(KB);
            KB = nullptr;
        }
    }
    return TRUE;
}