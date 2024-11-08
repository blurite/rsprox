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
#include <windows.h>

#include <io.h>
#include <fcntl.h>
#include <iostream>
#include <direct.h>

#include "../packr.h"

using namespace std;

const char __CLASS_PATH_DELIM = ';';

extern "C"
{
	__declspec(dllexport) DWORD NvOptimusEnablement = 1;
	__declspec(dllexport) int AmdPowerXpressRequestHighPerformance = 1;
}

static void waitAtExit(void) {
	cout << "Press ENTER key to exit.";
	cin.get();
}

static bool attachToConsole(int argc, char** argv) {

	bool allocConsole = false;

	// pre-parse command line here to have a console in case of command line parse errors
	for (int arg = 0; arg < argc && !allocConsole; arg++) {
		allocConsole = (argv[arg] != nullptr && stricmp(argv[arg], "--console") == 0);
	}

	bool hasConsole;
	if (allocConsole) {
		FreeConsole();
		hasConsole = AllocConsole();
	} else {
		hasConsole = AttachConsole(ATTACH_PARENT_PROCESS);
	}

	if (hasConsole) {
		freopen("CONIN$", "r", stdin);
		freopen("CONOUT$", "w", stdout);
		freopen("CONOUT$", "w", stderr);

		if (allocConsole) {
			atexit(waitAtExit);
		}
	}

	return hasConsole;
}

static void printLastError(const char* reason) {

	LPTSTR buffer;
	DWORD errorCode = GetLastError();

	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
				  nullptr, errorCode, MAKELANGID(LANG_ENGLISH, SUBLANG_DEFAULT), (LPTSTR) &buffer, 0, nullptr);

	cerr << "Error code [" << errorCode << "] when trying to " << reason << ": " << buffer;

	LocalFree(buffer);
}

int CALLBACK WinMain(
	HINSTANCE hInstance,
	HINSTANCE hPrevInstance,
	LPSTR lpCmdLine,
	int nCmdShow) {

	int argc = __argc;
	char** argv = __argv;

	attachToConsole(argc, argv);

	if (!setCmdLineArguments(argc, argv)) {
		return EXIT_FAILURE;
	}

	launchJavaVM(defaultLaunchVMDelegate);

	return 0;
}

int main(int argc, char** argv) {

	if (!setCmdLineArguments(argc, argv)) {
		return EXIT_FAILURE;
	}

	launchJavaVM(defaultLaunchVMDelegate);

	return 0;
}

bool loadJNIFunctions(GetDefaultJavaVMInitArgs* getDefaultJavaVMInitArgs, CreateJavaVM* createJavaVM) {
	if (!SetDllDirectory(TEXT("jre\\bin"))) {
		printLastError("SetDllDirectory");
		return false;
	}

	LPCTSTR jvmDLLPath = TEXT("jre\\bin\\server\\jvm.dll");

	HINSTANCE hinstLib = LoadLibrary(jvmDLLPath);
	if (hinstLib == nullptr) {
		printLastError("load jvm.dll");
		return false;
	}

	*getDefaultJavaVMInitArgs = (GetDefaultJavaVMInitArgs) GetProcAddress(hinstLib, "JNI_GetDefaultJavaVMInitArgs");
	if (*getDefaultJavaVMInitArgs == nullptr) {
		printLastError("obtain JNI_GetDefaultJavaVMInitArgs address");
		return false;
	}

	*createJavaVM = (CreateJavaVM) GetProcAddress(hinstLib, "JNI_CreateJavaVM");
	if (*createJavaVM == nullptr) {
		printLastError("obtain JNI_CreateJavaVM address");
		return false;
	}

	return true;
}

const char* getExecutablePath(const char* argv0) {
	return argv0;
}

bool changeWorkingDir(const char* directory) {
	return _chdir(directory) == 0;
}

void packrSetEnv(const char *key, const char *value) {
	SetEnvironmentVariable(key, value);
}

string acpToUtf8(const char *str) {
	// ACP -> UTF-16
	int len = MultiByteToWideChar(CP_ACP, 0, str, -1, NULL, 0);
	if (!len)
	{
		return string(str);
	}
	LPWSTR out16 = new wchar_t[len];
	MultiByteToWideChar(CP_ACP, 0, str, -1, out16, len);

	// UTF-16 -> UTF-8
	len = WideCharToMultiByte(CP_UTF8, 0, out16, -1, NULL, 0, NULL, NULL);
	if (!len)
	{
		delete[] out16;
		return string(str);
	}
	LPSTR out8 = new char[len];
	WideCharToMultiByte(CP_UTF8, 0, out16, -1, out8, len, NULL, NULL);

	delete[] out16;
	string out(out8);
	delete[] out8;

	return out;
}