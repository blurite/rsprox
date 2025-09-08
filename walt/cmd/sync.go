package cmd

import (
	"fmt"
	"rsprox/walt/internal"
	"sort"

	"github.com/spf13/cobra"
)

type SyncConflicts struct {
	reg []string // All Loopback IPs in the registry file
	rml []string // Loopback IPs which are tracked in the registry file, but not currently live on lo0 netiface
	lmr []string // Loopback IPs which are live on the netiface, but not tracked in the registry file
}

type SyncResult struct {
	added    int // Number of loopback IPs that were added to lo0 from the registry during sync
	adopted  int // Number of loopback IPs that were missing from, and as a result added to the registry from lo0
	errCount int // Number of errors encountered during sync
}

// R\L = tracked in the registry but not present on lo0
// L\R = live on lo0 but untracked (not found in reigstry file)
// adopt only = push live lo0 back to registry if needed - don't push any non-live loopbacks in the registry onto lo0.

var (
	dryRun bool
)

var syncCmd = &cobra.Command{
	Use:   "sync",
	Short: "Bidirectional synchronization of lo0 & loopback registry file",
	Long: `Sync definitions:
	R\L: set of relevant loopback IPs which are tracked in the registry file, but not currently live on lo0 netiface
	L\R: set of relevant loopback IPs which are live on the netiface, but not tracked in the registry file
Sync strategy:
	merge (default): 	adopt L\R into the registry, and add R\L to lo0
`,
	RunE: func(cmd *cobra.Command, args []string) error {
		conflicts, err := findSyncConflicts()
		if err != nil {
			return err
		}
		result := merge(cmd, conflicts)
		fmt.Fprintf(
			cmd.OutOrStdout(),
			"Sync mode=merge	added=%d adopted=%d\n",
			result.added, result.adopted,
		)
		if result.errCount > 0 {
			return fmt.Errorf("(%d) errors", result.errCount)
		}
		fmt.Fprintf(cmd.OutOrStdout(), "(%d) errors", result.errCount)
		return nil
	},
}

func merge(cmd *cobra.Command, conflicts *SyncConflicts) SyncResult {
	plan := NewSyncPlan(conflicts.rml, conflicts.lmr)

	added, adopted, errCount := 0, 0, 0
	var toAdopt []string

	for _, action := range plan.Actions {
		switch action.Type {
		case Add:
			if dryRun {
				fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] add %s\n", action.Ip)
				added++
				continue
			}
			status, err := internal.Alias(action.Ip)
			if err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "alias create for %s failed: %v\n", action.Ip, err)
				errCount++
				continue
			}
			if status {
				added++
			}

		case Adopt:
			if dryRun {
				fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] adopt into registry %s\n", action.Ip)
				adopted++
			} else {
				toAdopt = append(toAdopt, action.Ip)
			}
		}
	}

	if !dryRun && len(toAdopt) > 0 {
		union := append(append([]string{}, conflicts.reg...), toAdopt...)
		if err := internal.Save(union); err != nil {
			fmt.Fprintf(cmd.ErrOrStderr(), "registry adopt failed: %v\n", err)
			errCount++
		} else {
			adopted = len(toAdopt)
		}
	}

	return SyncResult{
		added:    added,
		adopted:  adopted,
		errCount: errCount,
	}
}

func findSyncConflicts() (*SyncConflicts, error) {
	curReg, err := internal.Load()
	if err != nil {
		return &SyncConflicts{}, fmt.Errorf("load registry: %w", err)
	}
	l, err := internal.GetLiveAliases()
	if err != nil {
		return &SyncConflicts{}, fmt.Errorf("fetch live loopback aliases on lo0: %w", err)
	}
	reg := make(map[string]struct{}, len(curReg))
	for _, ip := range curReg {
		reg[ip] = struct{}{}
	}
	live := make(map[string]struct{}, len(l))
	for _, ip := range l {
		live[ip] = struct{}{}
	}
	var rml, lmr []string
	for _, ip := range curReg {
		if _, ok := live[ip]; !ok {
			rml = append(rml, ip)
		}
	}
	for ip := range live {
		if _, ok := reg[ip]; !ok {
			lmr = append(lmr, ip)
		}
	}
	sort.Strings(rml)
	sort.Strings(lmr)
	return &SyncConflicts{curReg, rml, lmr}, nil
}

func init() {
	rootCmd.AddCommand(syncCmd)
	syncCmd.Flags().BoolVar(&dryRun, "dry-run", false, "preview actions without making changes")
}
