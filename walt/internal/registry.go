package internal

import (
	"errors"
	"os"
	"path/filepath"
	"sort"
	"strings"
)

type _void struct{}

var void _void

func Load() ([]string, error) {
	path, err := checkRegistryPath()
	if err != nil {
		return nil, err
	}
	b, err := os.ReadFile(path)
	if errors.Is(err, os.ErrNotExist) {
		return []string{}, nil
	}
	if err != nil {
		return nil, err
	}
	seen := make(map[string]_void)
	for ln := range strings.SplitSeq(string(b), "\n") {
		ip := strings.TrimSpace(ln)
		if ip != "" {
			seen[ip] = void
		}
	}
	out := make([]string, 0, len(seen))
	for ip := range seen {
		out = append(out, ip)
	}
	sort.Strings(out)
	return out, nil
}

func getRegistryPath() (string, error) {
	if stateHome := os.Getenv("XDG_STATE_HOME"); stateHome != "" {
		return filepath.Join(stateHome, "walt", "aliases.txt"), nil
	}
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	return filepath.Join(home, ".local", "state", "walt", "aliases.txt"), nil
}

func checkRegistryPath() (string, error) {
	path, err := getRegistryPath()
	baseMessage := "Failed to create alias list file in local state directory: "
	if err != nil {
		return "", errors.New(baseMessage + err.Error())
	}
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return "", errors.New(baseMessage + err.Error())
	}
	return path, nil
}
