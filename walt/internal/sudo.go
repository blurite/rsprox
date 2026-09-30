package internal

import (
	"bytes"
	"errors"
	"os"
	"os/exec"
	"strings"
)

// Runs a command (name args...) as sudo. If interactive, will re-prompt for sudo password if required (very unlikely).
func RunElevated(interactive bool, name string, args ...string) error {
	var all []string
	if interactive {
		all = []string{"-p", "[walt] sudo password for %u:", name}
	} else {
		all = []string{"-n", name}
	}
	all = append(all, args...)

	cmd := exec.Command("sudo", all...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout

	var stderr bytes.Buffer
	cmd.Stderr = &stderr

	if err := cmd.Run(); err != nil {
		if msg := strings.TrimSpace(stderr.String()); msg == "" {
			return errors.New(msg)
		}
		return err
	}
	return nil
}
