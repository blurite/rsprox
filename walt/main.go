package main

import (
	"fmt"
	"os"
	"runtime"
)

/*
The default parameters from the legacy shell script.

Note that we intentionally constrain the world range to be a single world by default,
so as not to bog down any networking by default on the host.
*/
const (
	defaultMinWorld = 255
	defaultMaxWorld = 255
	deafultGroup    = 2
)

func checkOs() {
	if runtime.GOOS != "darwin" {
		fmt.Fprintln(os.Stderr, "WALT is only supported on (and required by) macOS devices.")
		os.Exit(1)
	}
}

func main() {
	checkOs()
	fmt.Fprintln(os.Stdout, "All good.")
}
