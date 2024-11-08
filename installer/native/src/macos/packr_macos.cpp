/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
#include "../packr.h"

#include <dlfcn.h>
#include <iostream>
#include <pthread.h>
#include <CoreFoundation/CoreFoundation.h>
#include <sys/param.h>
#include <unistd.h>
#include <stdlib.h>

using namespace std;

// getExecutablePath runs prior to cli args being parsed, so verbose isn't set
//#define VERBOSE

const char __CLASS_PATH_DELIM = ':';

void sourceCallBack(void* info) {

}


/*
    Simple wrapper to call std::function from a C-style function
    signature. Usually one would use func.target<c-func>() to do
    conversion, but I failed to get this compiling with XCode.
*/
static LaunchJavaVMDelegate s_delegate = NULL;
void* launchVM(void* param) {
    s_delegate();
    return nullptr;
}

int main(int argc, char** argv) {

    if (!setCmdLineArguments(argc, argv)) {
        return EXIT_FAILURE;
    }

    launchJavaVM([](LaunchJavaVMDelegate delegate, const JavaVMInitArgs& args) {

        for (jint arg = 0; arg < args.nOptions; arg++) {
            const char* optionString = args.options[arg].optionString;
            if (strcmp("-XstartOnFirstThread", optionString) == 0) {

                if (verbose) {
                    cout << "Starting JVM on main thread (-XstartOnFirstThread found) ..." << endl;
                }

                delegate();
                return;
            }
        }

        // copy delegate; see launchVM() for remarks
        s_delegate = delegate;

        CFRunLoopSourceContext sourceContext;
        pthread_t vmthread;
        struct rlimit limit;
        size_t stack_size = 0;
        int rc = getrlimit(RLIMIT_STACK, &limit);
        if (rc == 0) {
            if (limit.rlim_cur != 0LL) {
                stack_size = (size_t)limit.rlim_cur;
            }
        }

        pthread_attr_t thread_attr;
        pthread_attr_init(&thread_attr);
        pthread_attr_setscope(&thread_attr, PTHREAD_SCOPE_SYSTEM);
        pthread_attr_setdetachstate(&thread_attr, PTHREAD_CREATE_DETACHED);
        if (stack_size > 0) {
            pthread_attr_setstacksize(&thread_attr, stack_size);
        }
        pthread_create(&vmthread, &thread_attr, launchVM, 0);
        pthread_attr_destroy(&thread_attr);

        /* Create a a sourceContext to be used by our source that makes */
        /* sure the CFRunLoop doesn't exit right away */
        sourceContext.version = 0;
        sourceContext.info = NULL;
        sourceContext.retain = NULL;
        sourceContext.release = NULL;
        sourceContext.copyDescription = NULL;
        sourceContext.equal = NULL;
        sourceContext.hash = NULL;
        sourceContext.schedule = NULL;
        sourceContext.cancel = NULL;
        sourceContext.perform = &sourceCallBack;

        CFRunLoopSourceRef sourceRef = CFRunLoopSourceCreate(NULL, 0, &sourceContext);
        CFRunLoopAddSource(CFRunLoopGetCurrent(), sourceRef, kCFRunLoopCommonModes);
        CFRunLoopRun();

    });

    return 0;
}

bool loadJNIFunctions(GetDefaultJavaVMInitArgs* getDefaultJavaVMInitArgs, CreateJavaVM* createJavaVM) {

    char buf[MAXPATHLEN];
    string cwd;

    if (getcwd(buf, sizeof(buf))) {
        cwd.append(buf).append("/");
    }

    string path = cwd + "jre/lib/libjli.dylib";

    void* handle = dlopen(path.c_str(), RTLD_LAZY);
    if (handle == NULL) {
        path = cwd + "jre/lib/jli/libjli.dylib";
        handle = dlopen(path.c_str(), RTLD_LAZY);
        if (handle == NULL) {
            cerr << dlerror() << endl;
            return false;
        }
    }

	*getDefaultJavaVMInitArgs = (GetDefaultJavaVMInitArgs) dlsym(handle, "JNI_GetDefaultJavaVMInitArgs");
	*createJavaVM = (CreateJavaVM) dlsym(handle, "JNI_CreateJavaVM");

    if ((*getDefaultJavaVMInitArgs == nullptr) || (*createJavaVM == nullptr)) {
        cerr << dlerror() << endl;
        return false;
    }

	return true;
}

extern "C" {
    int _NSGetExecutablePath(char* buf, uint32_t* bufsize);
}

const char* getExecutablePath(const char* argv0) {

    static char buf[MAXPATHLEN];
    uint32_t size = sizeof(buf);

    // first, try to obtain the MacOS bundle resources folder

    char resourcesDir[MAXPATHLEN];
    bool foundResources = false;

    CFBundleRef bundle = CFBundleGetMainBundle();
    if (bundle != NULL) {
        CFURLRef resources = CFBundleCopyResourcesDirectoryURL(bundle);
        if (resources != NULL) {
            foundResources = CFURLGetFileSystemRepresentation(resources, true, (UInt8*) resourcesDir, size);
            CFRelease(resources);
        }
    }

    // as a fallback, default to the executable path

    char executablePath[MAXPATHLEN];
    bool foundPath = _NSGetExecutablePath(executablePath, &size) != -1;

    // mangle path and executable name; the main application divides them again

    if (foundResources && foundPath) {
        const char* executableName = strrchr(executablePath, '/') + 1;
        strcpy(buf, resourcesDir);
        strcat(buf, "/");
        strcat(buf, executableName);
#ifdef VERBOSE
        cout << "Using bundle resource folder [1]: " << resourcesDir << "/[" << executableName << "]" << endl;
#endif
    } else if (foundResources) {
        strcpy(buf, resourcesDir);
        strcat(buf, "/packr");
#ifdef VERBOSE
        cout << "Using bundle resource folder [2]: " << resourcesDir << endl;
#endif
    } else if (foundPath) {
        strcpy(buf, executablePath);
#ifdef VERBOSE
        cout << "Using executable path: " << executablePath << endl;
#endif
    } else {
        strcpy(buf, argv0);
#ifdef VERBOSE
        cout << "Using [argv0] path: " << argv0 << endl;
#endif
    }

    return buf;
}

bool changeWorkingDir(const char* directory) {
	return chdir(directory) == 0;
}

void packrSetEnv(const char *key, const char *value) {
	setenv(key, value, 1);
}