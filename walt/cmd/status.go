package cmd

import (
	"fmt"
	"sort"

	"github.com/blurite/rsprox/walt/internal"
	"github.com/spf13/cobra"
)

var statusCmd = &cobra.Command{
	Use:   "status",
	Short: "Compare registry to live lo0 aliases",
	RunE: func(cmd *cobra.Command, args []string) error {
		reg, err := internal.Load()
		if err != nil {
			return err
		}
		live, err := internal.GetLiveAliases()
		if err != nil {
			return err
		}
		regSet := map[string]struct{}{}
		liveSet := map[string]struct{}{}
		for _, ip := range reg {
			regSet[ip] = struct{}{}
		}
		for _, ip := range live {
			liveSet[ip] = struct{}{}
		}

		var regOnly, liveOnly []string
		for _, ip := range reg {
			if _, ok := liveSet[ip]; !ok {
				regOnly = append(regOnly, ip)
			}
		}
		for _, ip := range live {
			if _, ok := regSet[ip]; !ok {
				liveOnly = append(liveOnly, ip)
			}
		}
		sort.Strings(regOnly)
		sort.Strings(liveOnly)

		fmt.Fprintf(cmd.OutOrStdout(), "Registry (%d):\n", len(reg))
		for _, ip := range reg {
			fmt.Fprintf(cmd.OutOrStdout(), "  %s\n", ip)
		}
		fmt.Fprintf(cmd.OutOrStdout(), "Live on lo0 (%d):\n", len(live))
		for _, ip := range live {
			fmt.Fprintf(cmd.OutOrStdout(), "  %s\n", ip)
		}

		fmt.Fprintln(cmd.OutOrStdout(), "Registry but NOT live:")
		if len(regOnly) == 0 {
			fmt.Fprintln(cmd.OutOrStdout(), "  (none)")
		} else {
			for _, ip := range regOnly {
				fmt.Fprintf(cmd.OutOrStdout(), "  %s\n", ip)
			}
		}

		fmt.Fprintln(cmd.OutOrStdout(), "Live but NOT in registry:")
		if len(liveOnly) == 0 {
			fmt.Fprintln(cmd.OutOrStdout(), "  (none)")
		} else {
			for _, ip := range liveOnly {
				fmt.Fprintf(cmd.OutOrStdout(), "  %s\n", ip)
			}
		}
		return nil
	},
}

func init() {
	rootCmd.AddCommand(statusCmd)
}
