class Walt < Formula
  desc "Manage loopback address aliases for RSProx on macOS"
  homepage "https://github.com/blurite/rsprox"
  # update these once tag present
  url "https://github.com/blurite/rsprox/archive/refs/tags/v0.1.0.tar.gz"
  sha256 "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
  license "MIT"

  # dev-only: point head to fork
  head "https://github.com/doneill612/rsprox.git", branch: "feature/mac-os-world-aliasing-hb"

  depends_on "go" => :build

  def install
    # The main package lives in ./walt
    system "go", "build", *std_go_args(ldflags: "-s -w"), "./walt"
  end

  test do
    out = shell_output("#{bin}/walt --help")
    assert_match "Manage loopback", out
  end
end
