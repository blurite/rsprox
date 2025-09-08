package cmd

import "fmt"

type ActionType int

const (
	Add   ActionType = iota // Add an IP present in the registry file to lo0
	Adopt                   // Add an IP present on lo0 to the registry file (due to missing)
	Prune                   // Remove an IP from lo0 due to not being present in registry file
)

// Encapsulation of an action taken on a loopabck IP address
type Action struct {
	Type ActionType
	Ip   string
}

type SyncPlan struct {
	Actions []Action // Collection of actions required to synchronize registry file with lo0
}

func NewSyncPlan(mode string, adoptOnly bool, rml, lmr []string) (SyncPlan, error) {
	rml = unique(rml)
	lmr = unique(lmr)
	var plan SyncPlan
	switch mode {
	case "merge":
		if !adoptOnly {
			for _, ip := range rml {
				plan.Actions = append(plan.Actions, Action{Type: Add, Ip: ip})
			}
		}
		for _, ip := range lmr {
			plan.Actions = append(plan.Actions, Action{Type: Adopt, Ip: ip})
		}
	case "up":
		for _, ip := range rml {
			plan.Actions = append(plan.Actions, Action{Type: Add, Ip: ip})
		}
	case "down":
		for _, ip := range lmr {
			plan.Actions = append(plan.Actions, Action{Type: Prune, Ip: ip})
		}
	default:
		return plan, fmt.Errorf("invalid mode %q (use merge|up|down)", mode)
	}
	return plan, nil
}

// Safety de-duplication of a list of loopback IPs
func unique(ips []string) []string {
	seen := make(map[string]struct{}, len(ips))
	out := make([]string, 0, len(ips))
	for _, ip := range ips {
		if _, in := seen[ip]; in {
			continue
		}
		seen[ip] = struct{}{}
		out = append(out, ip)
	}
	return out
}
