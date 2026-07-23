package cmd

import (
	"reflect"
	"testing"
)

func TestBuildPlan(t *testing.T) {
	// 127.1.1.2 present in registry file but not on lo0
	rml := []string{"127.1.1.2"}
	// 127.1.3.2 present on lo0 but not on the registry file
	lmr := []string{"127.1.3.2"}
	plan := NewSyncPlan(rml, lmr)
	want := SyncPlan{
		Actions: []Action{
			{Type: Add, Ip: "127.1.1.2"},
			{Type: Adopt, Ip: "127.1.3.2"},
		},
	}
	assertEqualPlan(t, want, plan)
}

func assertEqualPlan(t *testing.T, want, got SyncPlan) {
	t.Helper()
	if !reflect.DeepEqual(want, got) {
		t.Fatalf("plan mismatch\nwant: %#v\ngot:  %#v", want, got)
	}
}
