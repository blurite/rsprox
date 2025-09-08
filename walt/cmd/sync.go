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
	pruned   int // Number of loopback IPs that were live on lo0 but unaliased due to not being in the registry
	errCount int // Number of errors encountered during sync
}

// R\L = tracked in the registry but not present on lo0
// L\R = live on lo0 but untracked (not found in reigstry file)
// adopt only = push live lo0 back to registry if needed - don't push any non-live loopbacks in the registry onto lo0.

var (
	syncMode  string
	adoptOnly bool
	dryRun    bool
)

var syncCmd = &cobra.Command{
	Use:   "sync",
	Short: "Bidirectional synchronization of lo0 & loopback registry file",
	Long: `Sync definitions:
	R\L: set of relevant loopback IPs which are tracked in the registry file, but not currently live on lo0 netiface
	L\R: set of relevant loopback IPs which are live on the netiface, but not tracked in the registry file
Sync policies:
	merge (default): 	adopt L\R into the registry, and add R\L to lo0 (unless --adopt-only flag present)
	up:								add R\L to lo0 only
	down:							remove L\R from lo0 - prune with no adds/appends
`,
	RunE: func(cmd *cobra.Command, args []string) error {
		conflicts, err := findSyncConflicts()
		if err != nil {
			return err
		}
		var result SyncResult
		switch syncMode {
		case "merge":
			result = merge(cmd, conflicts)
		case "up":
			result = up(cmd, conflicts.rml)
		case "down":
			result = down(cmd, conflicts.lmr)
		default:
			return fmt.Errorf("invalid --mode %q (use merge|up|down)", syncMode)
		}

		fmt.Fprintf(
			cmd.OutOrStdout(),
			"Sync mode=%s	added=%d adopted=%d pruned=%d\n",
			syncMode, result.added, result.adopted, result.pruned,
		)
		if result.errCount > 0 {
			return fmt.Errorf("(%d) errors", result.errCount)
		}
		fmt.Fprintf(cmd.OutOrStdout(), "(%d) errors", result.errCount)
		return nil
	},
}

func merge(cmd *cobra.Command, conflicts *SyncConflicts) SyncResult {
	added, adopted, errCount := 0, 0, 0
	if !adoptOnly {
		for _, ip := range conflicts.rml {
			if dryRun {
				fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] add %s\n", ip)
				added++
				continue
			}
			status, err := internal.Alias(ip)
			if err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "alias create for %s failed: %v\n", ip, err)
				errCount++
				continue
			}
			if status {
				added++
			}
		}
	}
	if len(conflicts.lmr) > 0 {
		if dryRun {
			for _, ip := range conflicts.lmr {
				fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] adopt into registry %s\n", ip)
				adopted++
			}
		} else {
			union := append(append([]string{}, conflicts.reg...), conflicts.lmr...)
			if err := internal.Save(union); err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "registry adopt failed: %v\n", err)
				errCount++
			} else {
				adopted = len(conflicts.lmr)
			}
		}
	}
	return SyncResult{
		added:    added,
		adopted:  adopted,
		pruned:   0,
		errCount: errCount,
	}
}

func up(cmd *cobra.Command, rml []string) SyncResult {
	added, errCount := 0, 0
	for _, ip := range rml {
		if dryRun {
			fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] add %s\n", ip)
			added++
			continue
		}
		status, err := internal.Alias(ip)
		if err != nil {
			fmt.Fprintf(cmd.ErrOrStderr(), "alias create for %s failed: %v\n", ip, err)
			errCount++
			continue
		}
		if status {
			added++
		}
	}
	return SyncResult{
		added:    added,
		adopted:  0,
		pruned:   0,
		errCount: errCount,
	}
}

func down(cmd *cobra.Command, lmr []string) SyncResult {
	pruned, errCount := 0, 0
	for _, ip := range lmr {
		if dryRun {
			fmt.Fprintf(cmd.OutOrStdout(), "[dry-run] remove %s\n", ip)
			pruned++
			continue
		}
		status, err := internal.Unalias(ip)
		if err != nil {
			fmt.Fprintf(cmd.ErrOrStderr(), "unalias %s failed: %v\n", ip, err)
			errCount++
			continue
		}
		if status {
			pruned++
		}
		// Also remove from registry if its still somehow in there
		internal.Remove(ip)
	}
	return SyncResult{
		added:    0,
		adopted:  0,
		pruned:   pruned,
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
	syncCmd.Flags().StringVar(&syncMode, "mode", "merge", "sync policy: merge|up|down")
	syncCmd.Flags().BoolVar(&adoptOnly, "adopt-only", false, "merge mode: adopt live-but-untracked into registry, but do not add missing live aliases")
	syncCmd.Flags().BoolVar(&dryRun, "dry-run", false, "preview actions without making changes")
}
