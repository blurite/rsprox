#include <Windows.h>
#include <aclapi.h>
#include <jni.h>
#include <sddl.h>

#include <memory>

extern void rlThrow(JNIEnv *env, const char *msg);

// https://learn.microsoft.com/en-us/windows/win32/secauthz/creating-a-security-descriptor-for-a-new-object-in-c--?redirectedfrom=MSDN
extern "C" JNIEXPORT void JNICALL Java_net_runelite_launcher_Launcher_setFileACL(JNIEnv *env, jclass clazz, jstring folderJs, jobjectArray sidsJa) {
    SECURITY_DESCRIPTOR securityDescriptor;
    if (!InitializeSecurityDescriptor(&securityDescriptor, SECURITY_DESCRIPTOR_REVISION)) {
        rlThrow(env, "unable to initialize security descriptor");
        return;
    }

    int numSid = env->GetArrayLength(sidsJa);
    EXPLICIT_ACCESSW *explicitAccesses = new EXPLICIT_ACCESSW[numSid];
    ZeroMemory(explicitAccesses, sizeof(EXPLICIT_ACCESSW) * numSid);

    for (int i = 0; i < numSid; ++i) {
        jstring sidJs = static_cast<jstring>(env->GetObjectArrayElement(sidsJa, i));
        const jchar *sid = env->GetStringChars(sidJs, nullptr);

        PSID pSid = NULL;
        if (!ConvertStringSidToSidW(reinterpret_cast<LPCWSTR>(sid), &pSid)) {
            rlThrow(env, "unable to convert string SID to SID");
            env->ReleaseStringChars(sidJs, sid);
            goto freesid;
        }

        EXPLICIT_ACCESSW &explicitAccess = explicitAccesses[i];
        explicitAccess.grfAccessPermissions = GENERIC_ALL;
        explicitAccess.grfAccessMode = SET_ACCESS;
        explicitAccess.grfInheritance = OBJECT_INHERIT_ACE | CONTAINER_INHERIT_ACE;
        explicitAccess.Trustee.TrusteeForm = TRUSTEE_IS_SID;
        explicitAccess.Trustee.TrusteeType = TRUSTEE_IS_GROUP;
        explicitAccess.Trustee.ptstrName = (LPWSTR)pSid;

        env->ReleaseStringChars(sidJs, sid);
    }

    PACL pAcl = NULL;
    DWORD dwResult = SetEntriesInAclW(numSid, explicitAccesses, NULL, &pAcl);
    if (dwResult != ERROR_SUCCESS) {
        rlThrow(env, "unable to set entries in ACL");
        goto freesid;
    }

    if (!SetSecurityDescriptorDacl(&securityDescriptor, TRUE, pAcl, FALSE)) {
        rlThrow(env, "error setting security descriptor DACL");
        goto freeacl;
    }

    const jchar *folder = env->GetStringChars(folderJs, nullptr);
    if (!SetFileSecurityW(reinterpret_cast<LPCWSTR>(folder), DACL_SECURITY_INFORMATION, &securityDescriptor)) {
        rlThrow(env, "error setting file security");
    }
    env->ReleaseStringChars(folderJs, folder);

freeacl:
    LocalFree(pAcl);

freesid:
    for (int i = 0; i < numSid; ++i) {
        EXPLICIT_ACCESSW &explicitAccess = explicitAccesses[i];
        if (explicitAccess.Trustee.ptstrName) {
            LocalFree((PSID)explicitAccess.Trustee.ptstrName);
        }
    }

    delete[] explicitAccesses;
}

extern "C" JNIEXPORT jstring JNICALL Java_net_runelite_launcher_Launcher_getUserSID(JNIEnv *env, jclass clazz) {
    HANDLE hToken;
    if (!OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hToken)) {
        rlThrow(env, "error opening process token");
        return nullptr;
    }

    DWORD returnLength = 0;
    GetTokenInformation(hToken, TokenUser, nullptr, 0, &returnLength);
    if (GetLastError() != ERROR_INSUFFICIENT_BUFFER) {
        rlThrow(env, "unable to get TokenUser buffer size");
        CloseHandle(hToken);
        return nullptr;
    }

    std::unique_ptr<char[]> tokenInfoBuffer(new char[returnLength]);
    if (!GetTokenInformation(hToken, TokenUser, tokenInfoBuffer.get(), returnLength, &returnLength)) {
        rlThrow(env, "error getting token information");
        CloseHandle(hToken);
        return nullptr;
    }

    CloseHandle(hToken);

    PTOKEN_USER tokenUser = (PTOKEN_USER)tokenInfoBuffer.get();

    LPWSTR pstrSid;
    if (!ConvertSidToStringSidW(tokenUser->User.Sid, &pstrSid)) {
        rlThrow(env, "error converting SID to string");
        return nullptr;
    }

    jstring ret = env->NewString(reinterpret_cast<const jchar *>(pstrSid), static_cast<jsize>(wcslen(pstrSid)));

    LocalFree(pstrSid);

    return ret;
}
