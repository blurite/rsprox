package cmd

import (
	"errors"
	"fmt"
	"io"
	"rsprox/walt/internal"
	"sort"

	"github.com/spf13/cobra"
)

type IPSets struct {
	// Set of loopback aliases currently found in the registry
	reg map[string]struct{}
	// Set of loopback aliases currently live on lo0
	live map[string]struct{}
}

type SyncConflicts struct {
	// All Loopback IPs in the registry file
	reg []string
	// Loopback IPs which are tracked in the registry file, but not currently live on lo0 netiface
	rml []string
	// Loopback IPs which are live on the netiface, but not tracked in the registry file
	lmr []string
}

type SyncResult struct {
	// Number of loopback IPs that were added to lo0 from the registry during sync
	added int
	// Number of loopback IPs that were missing from, and as a result added to the registry from lo0
	adopted int
	// Number of loopback IPs that were live on lo0 but unaliased due to not being in the registry
	pruned int
	// Number of errors encountered during sync
	errCount int
}

// R\L = tracked in the registry but not present on lo0
// L\R = live on lo0 but untracked (not found in reigstry file)
// adopt only = push live lo0 back to registry if needed - don't push any non-live loopbacks in the registry onto lo0.

var (
	syncMode  string
	adoptOnly bool
	preview   bool
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
		var result *SyncResult
		switch syncMode {
		case "merge":
			res, err := merge(cmd, conflicts)
			if err != nil {
				return err
			}
			result = &res
		case "up":
			res, err := up(cmd, &conflicts.rml)
			if err != nil {
				return err
			}
			result = &res
		case "down":
			res, err := down(cmd, &conflicts.lmr)
			if err != nil {
				return err
			}
			result = &res
		default:
			return fmt.Errorf("invalid --mode %q (use merge|up|down)", syncMode)
		}
		fmt.Fprintf(
			cmd.OutOrStdout(),
			"Sync mode=%s	added=%d	adopted=%d	pruned=%d",
			syncMode, result.added, result.adopted, result.pruned,
		)
		var w io.Writer
		if result.errCount > 0 {
			w = cmd.ErrOrStderr()
		} else {
			w = cmd.OutOrStdout()
		}
		fmt.Fprintf(w, "(%d) errors", result.errCount)
		return nil
	},
}

func merge(cmd *cobra.Command, conflicts *SyncConflicts) (SyncResult, error) {
	added, adopted, errCount := 0, 0, 0
	if !adoptOnly {
		for _, ip := range conflicts.rml {
			if preview {
				fmt.Fprintf(cmd.OutOrStdout(), "[preview] add %s\n", ip)
				added++
				continue
			}
			if status, err := internal.Alias(ip); err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "error: alias create for %s failed: %v\n", ip, err)
				errCount++
			} else if status {
				added++
			}
		}
	}
	if len(conflicts.lmr) > 0 {
		union := append(append([]string{}, conflicts.reg...), conflicts.lmr...)
		if preview {
			for _, ip := range conflicts.lmr {
				fmt.Fprintf(cmd.OutOrStdout(), "[preview] adopt into registry %s\n", ip)
				adopted++
			}
		} else {
			if err := internal.Save(union); err != nil {
				fmt.Fprintf(cmd.ErrOrStderr(), "error: registry adopt failed: %v\n", err)
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
	}, nil
}

func up(cmd *cobra.Command, rml *[]string) (SyncResult, error) {
	return SyncResult{}, nil
}

func down(cmd *cobra.Command, lmr *[]string) (SyncResult, error) {
	return SyncResult{}, nil
}

func findSyncConflicts() (*SyncConflicts, error) {
	curReg, err := internal.Load()
	if err != nil {
		return &SyncConflicts{}, errors.New("error: failed to load the registry file: error: " + err.Error())
	}
	sets, err := makeSets(&curReg)
	if err != nil {
		return &SyncConflicts{}, err
	}
	var rml, lmr []string
	for _, ip := range curReg {
		if _, ok := sets.live[ip]; !ok {
			rml = append(rml, ip)
		}
	}
	for ip := range sets.live {
		if _, ok := sets.reg[ip]; !ok {
			lmr = append(lmr, ip)
		}
	}
	sort.Strings(rml)
	sort.Strings(lmr)
	return &SyncConflicts{curReg, rml, lmr}, nil
}

func makeSets(ips *[]string) (IPSets, error) {
	l, err := internal.GetLiveAliases()
	if err != nil {
		return IPSets{}, errors.New("error: failed to fetch live loopback aliases on lo0: error: " + err.Error())
	}
	reg := make(map[string]struct{}, len(*ips))
	for _, ip := range *ips {
		reg[ip] = struct{}{}
	}
	live := make(map[string]struct{}, len(l))
	for _, ip := range l {
		live[ip] = struct{}{}
	}
	return IPSets{
		reg,
		live,
	}, nil
}
