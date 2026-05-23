package internal

import (
	"reflect"
	"runtime"
	"testing"
)

func TestIPForWorld(t *testing.T) {
	type tc struct {
		world int
		group int
		ip    string
	}

	cases := []tc{
		{0, 2, "127.0.0.2"},
		{1, 2, "127.0.1.2"},
		{256, 2, "127.1.0.2"},
		{300, 2, "127.1.44.2"},
	}
	for _, c := range cases {
		if got := IPForWorld(c.world, c.group); got != c.ip {
			t.Fatalf("IPForWorld(%d,%d) = %s want %s", c.world, c.group, got, c.ip)
		}
	}
}

func TestEnsureMac(t *testing.T) {
	// very likely unnecessary but...
	if runtime.GOOS != "darwin" {
		t.Skip("not macOS")
	}
	defer func() {
		if r := recover(); r != nil {
			t.Fatalf("panicked on darwin: %v", r)
		}
	}()
	ensureMac()
}

// parse test using captured ifconfig output from my local machine (with added inet)
func TestParseLo0Aliases(t *testing.T) {
	fixture := `
lo0: flags=8049<UP,LOOPBACK,RUNNING,MULTICAST> mtu 16384
        options=1203<RXCSUM,TXCSUM,TXSTATUS,SW_TIMESTAMP>
        inet 127.0.0.1 netmask 0xff000000
				inet 127.1.2.3 netmask 0xff000000
				inet 127.9.8.7 netmask 0xff000000
        inet6 ::1 prefixlen 128 
        inet6 fe80::1%lo0 prefixlen 64 scopeid 0x1 
        nd6 options=201<PERFORMNUD,DAD>
`
	got := parseAliases(fixture)
	want := []string{"127.1.2.3", "127.9.8.7"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("parseLo0Aliases got %v want %v", got, want)
	}
}

func ensureMac() {
	if runtime.GOOS != "darwin" {
		panic("walt is only intended to be use on macOS")
	}
}
