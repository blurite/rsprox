package cmd

import (
	"bytes"
	"os"
	"os/exec"
	"runtime"
	"slices"
	"strings"
	"testing"

	"github.com/blurite/rsprox/walt/internal"
)

func requireDarwin(t *testing.T) {
	t.Helper()
	if runtime.GOOS != "darwin" {
		t.Skip("e2e: macOS only")
	}
}

func requireE2EEnabled(t *testing.T) {
	t.Helper()
	if os.Getenv("WALT_E2E") != "1" {
		t.Skip("e2e: set WALT_E2E=1 to run CLI integration tests")
	}
}

func requireSudo(t *testing.T) {
	t.Helper()
	// CI on macOS runners via GH actions supports passwordless sudo
	// locally this will pass only if cahced ticket present or passwordless sudo.
	if err := exec.Command("sudo", "-v").Run(); err != nil {
		t.Skip("e2e: sudo -v failed. Run locally with WALT_E2E=1 after elevating or configure CI on macOS.")
	}
}

func setTempRegistry(t *testing.T) {
	t.Helper()
	dir := t.TempDir()
	t.Setenv("XDG_STATE_HOME", dir)
}

func runRootCmd(t *testing.T, args ...string) (stdout, stderr string, err error) {
	t.Helper()
	var out, errb bytes.Buffer
	rootCmd.SetOut(&out)
	rootCmd.SetErr(&errb)
	rootCmd.SetArgs(args)
	err = rootCmd.Execute()
	return out.String(), errb.String(), err
}

func TestE2E(t *testing.T) {
	requireDarwin(t)
	requireE2EEnabled(t)
	requireSudo(t)
	setTempRegistry(t)

	const (
		world = 300
		group = 3
	)
	ip := internal.IPForWorld(world, group)

	// ensure a clean slate for this IP
	internal.Unalias(ip) // ignore if absent
	internal.Remove(ip)  // ignore if absent

	// add one world
	stdout, stderr, err := runRootCmd(t, "add", "--min=300", "--max=300", "--group=3")
	if err != nil {
		t.Fatalf("add failed: err=%v, out=%q, errout=%q", err, stdout, stderr)
	}

	// verify live contains ip
	live, err := internal.GetLiveAliases()
	if err != nil {
		t.Fatalf("GetLiveAliases: %v", err)
	}
	if !slices.Contains(live, ip) {
		t.Fatalf("expected %s live after add; live=%v", ip, live)
	}
	// verify registry contains ip
	reg, err := internal.Load()
	if err != nil {
		t.Fatalf("Load registry: %v", err)
	}
	if !slices.Contains(reg, ip) {
		t.Fatalf("expected %s in registry after add; reg=%v", ip, reg)
	}

	// remove it
	stdout, stderr, err = runRootCmd(t, "remove", "-m", "300", "-M", "300", "-g", "3")
	if err != nil {
		t.Fatalf("remove failed: err=%v, out=%q, errout=%q", err, stdout, stderr)
	}

	live, _ = internal.GetLiveAliases()
	if slices.Contains(live, ip) {
		t.Fatalf("expected %s not live after remove; live=%v", ip, live)
	}
	reg, _ = internal.Load()
	if slices.Contains(reg, ip) {
		t.Fatalf("expected %s not in registry after remove; reg=%v", ip, reg)
	}

	// create drift: alias live but DO NOT add to registry
	if status, err := internal.Alias(ip); err != nil || !status {
		t.Fatalf("drift setup alias: changed=%v err=%v", status, err)
	}
	// ensure not in registry
	internal.Remove(ip)

	// run sync: should adopt ip into registry
	stdout, stderr, err = runRootCmd(t, "sync")
	if err != nil {
		t.Fatalf("sync failed: err=%v, out=%q, errout=%q", err, stdout, stderr)
	}
	if !strings.Contains(stdout+stderr, "adopted=") {
		t.Logf("sync output: %s%s", stdout, stderr)
	}

	reg, _ = internal.Load()
	if !slices.Contains(reg, ip) {
		t.Fatalf("expected %s adopted into registry; reg=%v", ip, reg)
	}

	// cleanup
	if _, err := internal.Unalias(ip); err != nil {
		t.Fatalf("cleanup unalias: %v", err)
	}
	_ = internal.Remove(ip)
}

func TestE2EDryRun(t *testing.T) {
	if runtime.GOOS != "darwin" {
		t.Skip("e2e: macOS only")
	}
	if os.Getenv("WALT_E2E") != "1" {
		t.Skip("e2e: set WALT_E2E=1 to run")
	}
	setTempRegistry(t)

	const (
		world = 301
		group = 3
	)
	ip := internal.IPForWorld(world, group)

	internal.Unalias(ip)
	internal.Remove(ip)

	if err := internal.Save([]string{ip}); err != nil {
		t.Fatalf("seed registry: %v", err)
	}

	// run sync --dry-run (should NOT add to lo0)
	var out, errb bytes.Buffer
	rootCmd.SetOut(&out)
	rootCmd.SetErr(&errb)
	rootCmd.SetArgs([]string{"sync", "--dry-run"})
	if err := rootCmd.Execute(); err != nil {
		t.Fatalf("sync --dry-run failed: %v, out=%q err=%q", err, out.String(), errb.String())
	}

	// verify still NOT live
	live, err := internal.GetLiveAliases()
	if err != nil {
		t.Fatalf("GetLiveAliases: %v", err)
	}
	for _, v := range live {
		if v == ip {
			t.Fatalf("dry-run mutated live state: %s became live", ip)
		}
	}
	// registry should still contain ip (dry-run doesn't modify registry)
	reg, err := internal.Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}
	found := slices.Contains(reg, ip)
	if !found {
		t.Fatalf("dry-run mutated registry: %s missing", ip)
	}

	internal.Remove(ip)
}
