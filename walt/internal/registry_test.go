package internal

import (
	"os"
	"path/filepath"
	"reflect"
	"testing"
)

func testRegistry(t *testing.T) string {
	t.Helper()
	dir := t.TempDir()
	t.Setenv("XDG_STATE_HOME", dir)
	return filepath.Join(dir, "walt", "aliases.txt")
}

func TestSaveLoad(t *testing.T) {
	testRegistry(t)
	in := []string{"127.1.0.2", "127.1.0.2", "127.1.0.1"}
	if err := Save(in); err != nil {
		t.Fatalf("Save: %v", err)
	}

	got, err := Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}
	want := []string{"127.1.0.1", "127.1.0.2"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("got %v want %v", got, want)
	}
}

func TestAppend(t *testing.T) {
	path := testRegistry(t)

	if err := Append("127.1.2.3"); err != nil {
		t.Fatalf("Append: %v", err)
	}
	// second time no-op
	if err := Append("127.1.2.3"); err != nil {
		t.Fatalf("Append: %v", err)
	}

	b, err := os.ReadFile(path)
	if err != nil {
		t.Fatalf("ReadFile: %v", err)
	}
	// file should contain the IP once (with trailing newline)
	if string(b) != "127.1.2.3\n" {
		t.Fatalf("file content = %q", string(b))
	}
}

func TestClear(t *testing.T) {
	path := testRegistry(t)

	if err := Save([]string{"127.1.2.3", "127.1.2.4"}); err != nil {
		t.Fatalf("Save: %v", err)
	}
	if err := Remove("127.1.2.3"); err != nil {
		t.Fatalf("Remove: %v", err)
	}
	got, _ := Load()
	want := []string{"127.1.2.4"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("after remove got %v want %v", got, want)
	}

	// remove last, file should be deleted
	if err := Remove("127.1.2.4"); err != nil {
		t.Fatalf("Remove last: %v", err)
	}
	if _, err := os.Stat(path); !os.IsNotExist(err) {
		t.Fatalf("registry file should be gone, err=%v", err)
	}

	// clear on non-existent file should be no-op
	if err := Clear(); err != nil {
		t.Fatalf("Clear on missing file: %v", err)
	}
}
