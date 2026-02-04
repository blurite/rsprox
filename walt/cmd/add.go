package cmd

import (
	"fmt"

	"github.com/blurite/rsprox/walt/internal"
	"github.com/spf13/cobra"
)

var addCmd = &cobra.Command{
	Use:   "add",
	Short: "Add a whitelisted loopback alias",
	Run: func(cmd *cobra.Command, args []string) {
		added, warnCount, errCount := 0, 0, 0
		syncNeeded := false
		for w := minWorld; w <= maxWorld; w++ {
			ip := internal.IPForWorld(w, group)
			status, err := internal.Alias(ip)
			if err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "error: alias create for %s failed: %v\n", ip, err)
				errCount++
				continue
			}
			if !status {
				fmt.Fprintf(cmd.ErrOrStderr(), "warn: alias %s already set\n", ip)
				warnCount++
				continue
			}
			if err := internal.Append(ip); err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "error: alias %s was added but the registry failed to update. error: %v\n", ip, err)
				syncNeeded = true
				errCount++
			}
			added++
		}
		fmt.Fprintf(cmd.OutOrStdout(), "Added %d aliases for %d..%d (group %d).", added, minWorld, maxWorld, group)
		fmt.Fprintf(cmd.OutOrStdout(), "(%d) warnings\n", warnCount)
		fmt.Fprintf(cmd.ErrOrStderr(), "(%d) errors\n", errCount)
		if syncNeeded {
			fmt.Fprintln(cmd.OutOrStdout(), "One or more of your errors were related to registry synchronization; Try `walt sync` to fix.")
		}
	},
}

func init() {
	rootCmd.AddCommand(addCmd)
}
