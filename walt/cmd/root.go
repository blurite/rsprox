/*
Copyright © 2025 David O'Neill

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package cmd

import (
	"fmt"
	"os"
	"runtime"

	"github.com/spf13/cobra"
)

// default parameters from the legacy shell script.
// Note that we intentionally constrain the world range to be a single world by default,
// so as not to bog down networking on the host.

const (
	defaultMinWorld = 255
	defaultMaxWorld = 255
	defaultGroup    = 3
)

var (
	minWorld int
	maxWorld int
	group    int
)

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "walt",
	Short: "Manage loopback address aliases for RSProx on macOS.",
	Long: `
 __        ___    _   _____ 
 \ \      / / \  | | |_   _|
  \ \ /\ / / _ \ | |   | |  
   \ V  V / ___ \| |___| |  
    \_/\_/_/   \_\_____|_|  
                                                       	
An easy-to-use CLI tool for managing loopback address aliases on macOS. Intended
to be used in parallel with RSProx.`,
	PersistentPreRun: rootPreRun,
}

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, "error:", err)
		os.Exit(1)
	}
}

func init() {
	rootCmd.PersistentFlags().IntVarP(
		&minWorld,
		"min",
		"m",
		defaultMinWorld,
		"world number lower bound (inclusive)",
	)
	rootCmd.PersistentFlags().IntVarP(
		&maxWorld,
		"max",
		"M",
		defaultMaxWorld,
		"world number upper bound (inclusive)",
	)
	rootCmd.PersistentFlags().IntVarP(
		&group,
		"group",
		"g",
		defaultGroup,
		"group ID (2 => OSRS (DO NOT USE! Will fail), 3+ => custom targets)",
	)
}

func rootPreRun(command *cobra.Command, args []string) {
	if runtime.GOOS != "darwin" {
		panic("walt is only intended to be use on macOS")
	}
	if minWorld > maxWorld {
		minWorld, maxWorld = maxWorld, minWorld
	}
	if group < 3 {
		panic(fmt.Errorf("macOS requires group >= 3, got %d", group))
	}
}
