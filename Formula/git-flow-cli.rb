class GitFlowCli < Formula
  desc "CLI para crear ramas y commits con convención GitBN"
  homepage "https://github.com/manuelduarte077/git-flow-cli"
  url "https://github.com/manuelduarte077/git-flow-cli/releases/download/v2.0.0/git-flow-cli-2.0.0.tgz"
  sha256 "15cc8c2eb5c0f7755b5535f0e72f2fcec7c6d185d7c4ab4628255df9e3f97a4a"
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
