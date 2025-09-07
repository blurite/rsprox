package internal

import (
	"runtime"
	"strconv"
	"strings"
)

func EnsureMac() {
	if runtime.GOOS != "Darwin" {
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
	return RunElevated("ifconfig", true, "lo0", "alias", ip)
}
