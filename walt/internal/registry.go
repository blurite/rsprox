package internal

import (
	"errors"
	"os"
	"path/filepath"
	"slices"
	"sort"
	"strings"
)

// Reads from the loopback alias registry file and returns all the entries
func Load() ([]string, error) {
	path, err := registryPath()
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

	seen := make(map[string]struct{})
	for ln := range strings.SplitSeq(string(b), "\n") {
		ip := strings.TrimSpace(ln)
		if ip != "" {
			seen[ip] = struct{}{}
		}
	}

	out := make([]string, 0, len(seen))
	for ip := range seen {
		out = append(out, ip)
	}
	sort.Strings(out)
	return out, nil
}

// Saves all supplied loopback IPs to the loopback alias registry file
func Save(ips []string) error {
	path, err := ensureRegistryPath()
	if err != nil {
		return err
	}

	seen := make(map[string]struct{}, len(ips))
	for _, ip := range ips {
		if ip = strings.TrimSpace(ip); ip != "" {
			seen[ip] = struct{}{}
		}
	}

	out := make([]string, 0, len(seen))
	for ip := range seen {
		out = append(out, ip)
	}
	sort.Strings(out)

	data := strings.Join(out, "\n")
	if data != "" {
		data += "\n"
	}

	// atomic write - do a tmp file and then rename
	dir := filepath.Dir(path)
	tmp, err := os.CreateTemp(dir, "walt-registry-*.tmp")
	if err != nil {
		return err
	}
	tmpName := tmp.Name()
	_, werr := tmp.WriteString(data)
	cerr := tmp.Close()
	if werr != nil {
		os.Remove(tmpName)
		return werr
	}
	if cerr != nil {
		os.Remove(tmpName)
		return cerr
	}

	return os.Rename(tmpName, path)
}

// Appends a single IP to the loopback alias registry file
func Append(ip string) error {
	ips, err := Load()
	if err != nil {
		return err
	}
	for _, x := range ips {
		if x == ip {
			return nil
		}
	}
	ips = append(ips, ip)
	return Save(ips)
}

// Removes an entry from the loopback alias registry file. If empty after removal, torch the file.
func Remove(ip string) error {
	ips, err := Load()
	if err != nil {
		return err
	}

	ips = slices.DeleteFunc(ips, func(a string) bool {
		return a == ip
	})
	if len(ips) == 0 {
		// already called Load(), no need to explicit error handle
		path, _ := registryPath()
		if path != "" {
			os.Remove(path)
		}
		return nil
	}

	return Save(ips)
}

// Deletes the registry file entirely.
func Clear() error {
	path, err := registryPath()
	if err != nil {
		return err
	}

	if path != "" {
		// don't create error if the registry file doesn't exist yet
		if err := os.Remove(path); err != nil && !errors.Is(err, os.ErrNotExist) {
			return err
		}
	}
	return nil
}

// Gets the theoretically correct path to the registry file. Does not ensure existence.
func registryPath() (string, error) {
	if stateHome := os.Getenv("XDG_STATE_HOME"); stateHome != "" {
		return filepath.Join(stateHome, "walt", "aliases.txt"), nil
	}
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	return filepath.Join(home, ".local", "state", "walt", "aliases.txt"), nil
}

// Ensures existence of registry file.
func ensureRegistryPath() (string, error) {
	path, err := registryPath()
	baseMessage := "Failed to create alias list file in local state directory: "
	if err != nil {
		return "", errors.New(baseMessage + err.Error())
	}
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return "", errors.New(baseMessage + err.Error())
	}
	return path, nil
}
