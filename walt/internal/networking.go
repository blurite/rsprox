package internal

import (
	"os/exec"
	"runtime"
	"sort"
	"strconv"
	"strings"
)

func EnsureMac() {
	if runtime.GOOS != "darwin" {
		panic("walt is only intended to be use on macOS")
	}
}

func IPForWorld(world, group int) string {
	a := world / 256
	b := world % 256
	return strings.Join([]string{
		"127",
		strconv.Itoa(a),
		strconv.Itoa(b),
		strconv.Itoa(group),
	}, ".")
}

func Alias(ip string) error {
	return RunElevated(true, "ifconfig", "lo0", "alias", ip)
}

func Unalias(ip string) (bool, error) {
	if err := RunElevated(true, "ifconfig", "lo0", "-alias", ip); err != nil {
		// if the address isn't aliased, an error will be thrown with this message - non-fatal.
		if strings.Contains(err.Error(), "cannot assign requested address") {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

// Incrementally sorts and fetches all the currently aliased local addresses, not including the default 127.0.0.1
func GetLiveAliases() ([]string, error) {
	out, err := exec.Command("ifconfig", "lo0").CombinedOutput()
	if err != nil {
		return nil, err
	}
	var ips []string
	for ln := range strings.SplitSeq(string(out), "\n") {
		ln = strings.TrimSpace(ln)
		if strings.HasPrefix(ln, "inet") {
			fields := strings.Fields(ln)
			if len(fields) >= 2 {
				ip := fields[1]
				if strings.HasPrefix(ip, "127.") && ip != "127.0.0.1" {
					ips = append(ips, ip)
				}
			}
		}
	}
	sort.Strings(ips)
	return ips, nil
}
