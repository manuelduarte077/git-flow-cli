class GitFlowCli < Formula
  desc "CLI para crear ramas y commits con convención GitBN"
  homepage "https://github.com/manuelduarte077/git-flow-cli"
  url "https://github.com/manuelduarte077/git-flow-cli/releases/download/v2.0.1/git-flow-cli-2.0.1.tgz"
  sha256 "3fd4d421a634f2207209570bfdfdaf487642393298ac429700aa1e36f4dd9df6"
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
