package cmd

import (
	"fmt"

	"github.com/blurite/rsprox/walt/internal"
	"github.com/spf13/cobra"
)

var (
	clearDryRun      bool
	clearPruneOrphan bool // remove L\R as well
)

var clearCmd = &cobra.Command{
	Use:   "clear",
	Short: "Remove aliases and wipe the registry file",
	Long: `Clear removes all aliases tracked in the registry. With --prune-orphans (default),
it also removes any live aliases not present in the registry (L\R), so the system
and registry end up in a clean state.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		reg, err := internal.Load()
		if err != nil {
			return fmt.Errorf("load registry: %w", err)
		}
		live, err := internal.GetLiveAliases()
		if err != nil {
			return fmt.Errorf("fetch live loopback aliases on lo0: %w", err)
		}

		// This is repeated in sync command implementation... can probably migrate to a utility in the future.
		regSet := make(map[string]struct{}, len(reg))
		for _, ip := range reg {
			regSet[ip] = struct{}{}
		}
		liveSet := make(map[string]struct{}, len(live))
		for _, ip := range live {
			liveSet[ip] = struct{}{}
		}

		var toRemove []string
		toRemove = append(toRemove, reg...)
		if clearPruneOrphan {
			for _, ip := range live {
				if _, ok := regSet[ip]; !ok {
					toRemove = append(toRemove, ip)
				}
			}
		}

		removed, skipped, errCount := 0, 0, 0

		for _, ip := range toRemove {
			if clearDryRun {
				fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] remove %s\n", ip)
				removed++
				continue
			}
			status, err := internal.Unalias(ip)
			if err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "unalias %s failed: %v\n", ip, err)
				errCount++
				continue
			}
			if status {
				removed++
			} else {
				// not present on lo0
				skipped++
			}
			// Also remove from registry if its still somehow in there
			internal.Remove(ip)
		}

		if clearDryRun {
			fmt.Fprintln(cmd.OutOrStdout(), "[dry-run] delete registry file")
		} else if err := internal.Clear(); err != nil {
			return fmt.Errorf("delete registry: %w", err)
		}

		fmt.Fprintf(
			cmd.OutOrStdout(),
			"Clear summary: removed=%d skipped=%d\n",
			removed, skipped,
		)
		if errCount > 0 {
			return fmt.Errorf("(%d) errors", errCount)
		}
		return nil
	},
}

func init() {
	rootCmd.AddCommand(clearCmd)
	clearCmd.Flags().BoolVar(&clearDryRun, "dry-run", false, "preview actions without making changes")
	clearCmd.Flags().BoolVar(&clearPruneOrphan, "prune-orphans", true, "also remove live aliases that are not in the registry (L\\R)")
}
