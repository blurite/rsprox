package cmd

import (
	"fmt"
	"rsprox/walt/internal"

	"github.com/spf13/cobra"
)

var removeCmd = &cobra.Command{
	Use:   "remove",
	Short: "Remove a whitelisted loopback alias",
	Run: func(cmd *cobra.Command, args []string) {
		removed, warnCount, errCount := 0, 0, 0
		syncNeeded := false
		for w := minWorld; w <= maxWorld; w++ {
			ip := internal.IPForWorld(w, group)
			status, err := internal.Unalias(ip)
			if err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "error: alias %s failed: %v\n", ip, err)
				errCount++
				continue
			}
			if !status {
				fmt.Fprintf(cmd.ErrOrStderr(), "warn: alias %s not currently set - nothing to remove\n", ip)
				warnCount++
				continue
			}
			if err := internal.Remove(ip); err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "warn: alias %s was removed but the registry failed to update - use the sync command. error: %v\n", ip, err)
				warnCount++
				syncNeeded = true
			}
			removed++
		}
		if removed > 0 {
			fmt.Fprintf(cmd.OutOrStdout(), "Successfully removed %d aliases for %d..%d (group %d)\n", removed, minWorld, maxWorld, group)
		} else {
			fmt.Fprintln(cmd.OutOrStdout(), "No aliases removed")
		}
		fmt.Fprintf(cmd.OutOrStdout(), "(%d) warnings\n", warnCount)
		fmt.Fprintf(cmd.ErrOrStderr(), "(%d) errors\n", errCount)
		if syncNeeded {
			fmt.Fprintln(cmd.OutOrStdout(), "One or more of your warnings were related to registry synchronization. Use the sync command to fix.")
		}
	},
}

func init() {
	rootCmd.AddCommand(removeCmd)
}
