cmake -G "Visual Studio 16 2019" -A Win32 -S . -B "build32"
cmake -G "Visual Studio 16 2019" -A x64 -S . -B "build64"
cmake --build build32 --config Release
cmake --build build64 --config Release
