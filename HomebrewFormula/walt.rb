class Walt < Formula
  desc "Manage loopback address aliases for RSProx on macOS"
  homepage "https://github.com/blurite/rsprox"
  
  # update these on next release - can be automated via GH action
  url "https://github.com/blurite/rsprox/archive/refs/tags/v1.0.tar.gz"
  sha256 "be0a466572daa88ee6308da5bafe7d2072948097536bfc35a5b1eff5e5f1550a"

  license "MIT"

  head "https://github.com/blurite/rsprox.git", branch: "main"

  depends_on "go" => :build

  def install
    # The main package lives in ./walt
    cd "walt" do
      system "go", "build", *std_go_args(ldflags: "-s -w")
    end
  end

  test do
    out = shell_output("#{bin}/walt --help")
    assert_match "Manage loopback", out
  end
end
