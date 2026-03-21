class GitFlowCli < Formula
  desc "CLI para crear ramas y commits con convención GitBN"
  homepage "https://github.com/manuelduarte077/git-flow-cli"
  url "https://github.com/manuelduarte077/git-flow-cli/releases/download/v2.0.1/git-flow-cli-2.0.1.tgz"
  sha256 "1b18fc3e8e132d45b4d0bb0b8894b56daa497953b3343af458ea962602e50d01"
  license "MIT"

  depends_on "openjdk@21"

  def install
    sub = "git-flow-cli-#{version}"
    if File.directory?(sub)
      libexec.install Dir["#{sub}/*"]
    else
      libexec.install Dir["*"]
    end
    (bin/"git-flow-cli").write_env_script libexec/"bin/git-flow-cli",
      Language::Java.overridable_java_home_env("21")
  end

  test do
    system "#{bin}/git-flow-cli", "--help"
  end
end
