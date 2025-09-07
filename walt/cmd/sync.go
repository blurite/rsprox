package cmd

import (
	"github.com/spf13/cobra"
)

var syncCmd = &cobra.Command{
	Use:   "sync",
	Short: "Ensure all registered loopback aliases exist on lo0",
	Run: func(cmd *cobra.Command, args []string) {

	},
}
