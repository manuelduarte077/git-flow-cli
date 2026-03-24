class GitFlowCli < Formula
  desc "CLI para crear ramas y commits con convención GitBN"
  homepage "https://github.com/manuelduarte077/git-flow-cli"
  url "https://github.com/manuelduarte077/git-flow-cli/releases/download/v2.0.2/git-flow-cli-2.0.2.tgz"
  sha256 "8e9ecca4cee1a720c6daa9ff0c595c04492b86da2f6345f11c06c5be3d55529d"
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
