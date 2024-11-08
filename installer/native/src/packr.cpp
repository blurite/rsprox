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
#include "packr.h"

#include <dropt.h>
#include <sajson.h>

#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

using namespace std;

bool verbose = false;

static string workingDir;
static string executableName;
static string configurationPath("config.json");

static vector<string> cmdLineArgs;

static vector<string> vmArgs;
static vector<string> classPath;
static string mainClassName;

static sajson::document readConfigurationFile(string fileName) {

	ifstream in(fileName.c_str(), std::ios::in | std::ios::binary);
	string content((istreambuf_iterator<char>(in)), (istreambuf_iterator<char>()));

	sajson::document json = sajson::parse(sajson::literal(content.c_str()));
	return json;
}

static bool hasJsonValue(sajson::value jsonObject, const char* key, sajson::type expectedType) {
	size_t index = jsonObject.find_object_key(sajson::literal(key));
	if (index == jsonObject.get_length()) {
		return false;
	}
	sajson::value value = jsonObject.get_object_value(index);
	return value.get_type() == expectedType;
}

static sajson::value getJsonValue(sajson::value jsonObject, const char* key) {
	size_t index = jsonObject.find_object_key(sajson::literal(key));
	return jsonObject.get_object_value(index);
}

static vector<string> extractClassPath(const sajson::value& classPath) {

	size_t count = classPath.get_length();
	vector<string> paths;

	for (size_t cp = 0; cp < count; cp++) {

		string classPathURL = classPath.get_array_element(cp).as_string();

		// TODO: don't just test for file extension
		if (classPathURL.rfind(".txt") != classPathURL.length() - 4) {

			paths.push_back(classPathURL);

		} else {

			ifstream txt(classPathURL.c_str());
			string line;

			while (!txt.eof()) {

				txt >> line;

				if (line.find("-classpath") == 0) {

					txt >> line;

					istringstream iss(line);
					string path;

					while (getline(iss, path, __CLASS_PATH_DELIM)) {
						paths.push_back(path);
					}

					break;
				}

			}

			txt.close();

		}

	}

	return paths;
}

string getExecutableDirectory(const char* executablePath) {

	const char* delim = strrchr(executablePath, '/');
	if (delim == nullptr) {
		delim = strrchr(executablePath, '\\');
	}

	if (delim != nullptr) {
		return string(executablePath, delim - executablePath);
	}

	return string("");
}

string getExecutableName(const char* executablePath) {

	const char* delim = strrchr(executablePath, '/');
	if (delim == nullptr) {
		delim = strrchr(executablePath, '\\');
	}

	if (delim != nullptr) {
		return string(++delim);
	}

	return string(executablePath);
}

static dropt_error handle_vec_opt(dropt_context* context,
                                  const dropt_option* option,
                                  const dropt_char* optionArgument,
                                  void* dest) {
  vector<string>* v = static_cast<vector<string>*>(dest);
  if (optionArgument != nullptr) {
	v->push_back(optionArgument);
  }
  return dropt_error_none;
}

bool setCmdLineArguments(int argc, char** argv) {

	const char* executablePath = getExecutablePath(argv[0]);
	workingDir = getExecutableDirectory(executablePath);
	executableName = getExecutableName(executablePath);

	dropt_bool showHelp = 0;
	dropt_char* cwd = nullptr;
	dropt_char* config = nullptr;
	dropt_bool _verbose = 0;
	dropt_bool _console = 0;
	dropt_bool _cli = 0;

	dropt_option options[] = {
		{ 'c', "cli", "Enables this command line interface.", NULL, dropt_handle_bool, &_cli, dropt_attr_optional_val },
		{ 'h',  "help", "Shows help.", NULL, dropt_handle_bool, &showHelp, dropt_attr_halt },
		{ '?', NULL, NULL, NULL, dropt_handle_bool, &showHelp, dropt_attr_halt | dropt_attr_hidden },
		{ '\0', "cwd", "Sets the working directory.", NULL, dropt_handle_string, &cwd, dropt_attr_optional_val },
		{ '\0', "config", "Specifies the configuration file.", "config.json", dropt_handle_string, &config, dropt_attr_optional_val },
		{ 'v', "verbose", "Prints additional information.", NULL, dropt_handle_bool, &_verbose, dropt_attr_optional_val },
		{ '\0', "console", "Attachs a console window. [Windows only]", NULL, dropt_handle_bool, &_console, dropt_attr_optional_val },
		{ 'J', NULL, "JVM argument", "-Xmx512m", handle_vec_opt, &vmArgs, 0},
		{ 0, NULL, NULL, NULL, NULL, NULL, 0 }
	};

	dropt_context* droptContext = dropt_new_context(options);

	if (droptContext == nullptr) {
		cerr << "Error: failed to parse command line!" << endl;
		exit(EXIT_FAILURE);
	}

	if (argc > 1) {

		char** remains = nullptr;

		if ((strcmp("--cli", argv[1]) == 0) || (strcmp("-c", argv[1]) == 0)) {

			// only parse command line if the first argument is "--cli"

			remains = dropt_parse(droptContext, -1, &argv[1]);

			if (dropt_get_error(droptContext) != dropt_error_none) {
				cerr << dropt_get_error_message(droptContext) << endl;
				exit(EXIT_FAILURE);
			}

			if (showHelp) {

				cout << "Usage: " << executableName << " [java arguments]" << endl;
				cout << "       " << executableName << " -c [options] [-- [java arguments]]" << endl;
				cout << endl << "Options:" << endl;

				dropt_print_help(stdout, droptContext, nullptr);

			} else {

				// evalute parameters

				verbose = _verbose != 0;

				if (cwd != nullptr) {
					if (verbose) {
						cout << "Using working directory " << cwd << " ..." << endl;
					}
					workingDir = string(cwd);
				}

				if (config != nullptr) {
					if (verbose) {
						cout << "Using configuration file " << config << " ..." << endl;
					}
					configurationPath = string(config);
				}

			}

		} else {
			// treat all arguments as "remains"
			remains = &argv[1];
		}

		// copy unparsed arguments
		while (*remains != nullptr) {
#ifdef _WIN32
			// On Windows convert the argument using the current ANSI code page
			// to UTF-8 for the jstring creation later
			cmdLineArgs.push_back(acpToUtf8(*remains));
#else
			cmdLineArgs.push_back(*remains);
#endif
			remains++;
		}

	}

	dropt_free_context(droptContext);

	return showHelp == 0;
}

static void loadConfiguration() {
	// read settings

	sajson::document json = readConfigurationFile(configurationPath);

	if (!json.is_valid()) {
		cerr << "Error: failed to load configuration: " << configurationPath << endl;
		exit(EXIT_FAILURE);
	}

	sajson::value jsonRoot = json.get_root();

	// setup the env
	if (hasJsonValue(jsonRoot, "env", sajson::TYPE_OBJECT)) {
		sajson::value env = getJsonValue(jsonRoot, "env");

		if (verbose) {
			cout << "Setting up env..." << endl;
		}

		for (size_t i = 0; i < env.get_length(); i++) {
			string key = env.get_object_key(i).as_string();
			string value = env.get_object_value(i).as_string();
			if (verbose) {
				cout << "  " << key << "=" << value << endl;
			}

			packrSetEnv(key.c_str(), value.c_str());
		}
	}

	// setup vm args if not specified via -J
	if (vmArgs.empty() && hasJsonValue(jsonRoot, "vmArgs", sajson::TYPE_ARRAY)) {
		sajson::value vmArgs = getJsonValue(jsonRoot, "vmArgs");
		for (size_t vmArg = 0; vmArg < vmArgs.get_length(); vmArg++) {
			string vmArgValue = vmArgs.get_array_element(vmArg).as_string();
			::vmArgs.push_back(vmArgValue);
		}
	}

	if (!hasJsonValue(jsonRoot, "mainClass", sajson::TYPE_STRING)) {
		cerr << "Error: no 'mainClass' element found in config!" << endl;
		exit(EXIT_FAILURE);
	}

	mainClassName = getJsonValue(jsonRoot, "mainClass").as_string();

	if (!hasJsonValue(jsonRoot, "classPath", sajson::TYPE_ARRAY)) {
		cerr << "Error: no 'classPath' array found in config!" << endl;
		exit(EXIT_FAILURE);
	}

	sajson::value jsonClassPath = getJsonValue(jsonRoot, "classPath");
	classPath = extractClassPath(jsonClassPath);
}

void launchJavaVM(LaunchJavaVMCallback callback) {

	// change working directory

	if (!workingDir.empty()) {
		if (verbose) {
			cout << "Changing working directory to " << workingDir << " ..." << endl;
		}
		if (!changeWorkingDir(workingDir.c_str())) {
			cerr << "Warning: failed to change working directory to " << workingDir << endl;
		}
	}

	// load configuration
	loadConfiguration();

	// load JVM library, get function pointers

	if (verbose) {
		cout << "Loading JVM runtime library ..." << endl;
	}

	GetDefaultJavaVMInitArgs getDefaultJavaVMInitArgs = nullptr;
	CreateJavaVM createJavaVM = nullptr;

	if (!loadJNIFunctions(&getDefaultJavaVMInitArgs, &createJavaVM)) {
		cerr << "Error: failed to load VM runtime library!" << endl;
		exit(EXIT_FAILURE);
	}

	// get default init arguments

	JavaVMInitArgs args;
	args.version = JNI_VERSION_1_8;
	args.options = nullptr;
	args.nOptions = 0;
	args.ignoreUnrecognized = JNI_TRUE;

	if (getDefaultJavaVMInitArgs(&args) < 0) {
		cerr << "Error: failed to load default Java VM arguments!" << endl;
		exit(EXIT_FAILURE);
	}

	// fill VM options

	if (verbose) {
		cout << "Passing VM options ..." << endl;
	}

	size_t vmArgc = 0;
	JavaVMOption* options = new JavaVMOption[1 + vmArgs.size()];

	string javaClassPath = "-Djava.class.path=";
	for (unsigned int i = 0; i < classPath.size(); ++i) {
		if (i > 0) {
			javaClassPath.append(1, __CLASS_PATH_DELIM);
		}
		javaClassPath.append(classPath[i]);
	}
	options[vmArgc].optionString = strdup(javaClassPath.c_str());
	options[vmArgc++].extraInfo = nullptr;

	for (const string &vmArgValue : vmArgs) {
		if (verbose) {
			cout << "  # " << vmArgValue << endl;
		}
		options[vmArgc].optionString = strdup(vmArgValue.c_str());
		options[vmArgc++].extraInfo = nullptr;
	}

	assert(vmArgc == 1 + vmArgs.size());

	args.nOptions = vmArgc;
	args.options = options;

	/*
		Reroute JVM creation through platform-dependent code.

		On OS X this is used to decide if packr needs to spawn an additional thread, and create
		its own RunLoop.

		Done as lambda to capture local variables, and remain in function scope.
	*/

	callback([&]() {

		// create JVM

		JavaVM* jvm = nullptr;
		JNIEnv* env = nullptr;

		if (verbose) {
			cout << "Creating Java VM ..." << endl;
		}

		if (createJavaVM(&jvm, (void**) &env, &args) < 0) {
			cerr << "Error: failed to create Java VM!" << endl;
			exit(EXIT_FAILURE);
		}

		// create array of arguments to pass to Java main()

		if (verbose) {
			cout << "Passing command line arguments ..." << endl;
		}

		jobjectArray appArgs = env->NewObjectArray(cmdLineArgs.size(), env->FindClass("java/lang/String"), nullptr);
		for (size_t i = 0; i < cmdLineArgs.size(); i++) {
			if (verbose) {
				cout << "  # " << cmdLineArgs[i] << endl;
			}
			jstring arg = env->NewStringUTF(cmdLineArgs[i].c_str());
			env->SetObjectArrayElement(appArgs, i, arg);
		}

		// load main class & method from classpath

		if (verbose) {
			cout << "Loading main class ..." << endl;
		}

		string binaryClassName = mainClassName;
		replace(binaryClassName.begin(), binaryClassName.end(), '.', '/');

		jclass mainClass = env->FindClass(binaryClassName.c_str());
		if (!mainClass) {
			cerr << "Error: failed to find main class " << binaryClassName << endl;
			exit(EXIT_FAILURE);
		}

		jmethodID mainMethod = env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
		if (!mainMethod) {
			cerr << "Error: failed to find main method in " << binaryClassName << endl;
			exit(EXIT_FAILURE);
		}

		// call main() method

		if (verbose) {
			cout << "Invoking static " << mainClassName << ".main() function ..." << endl;
		}

		env->CallStaticVoidMethod(mainClass, mainMethod, appArgs);

		// check if main thread threw an exception
		jboolean exception = env->ExceptionCheck();
		int status = EXIT_SUCCESS;
		if (exception) {
			env->ExceptionDescribe();
			status = EXIT_FAILURE;
		}

		// blocks this thread until all non-daemon threads unload
		jvm->DestroyJavaVM();

		if (verbose) {
			cout << "Destroyed Java VM ..." << endl;
		}

		// cleanup

		for (size_t vmArg = 0; vmArg < vmArgc; vmArg++) {
			free(options[vmArg].optionString);
		}

		delete[] options;

		// on macOS this is run in a thread, so use exit() to exit the process
		exit(status);
	}, args);
}
