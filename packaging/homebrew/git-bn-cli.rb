# Homebrew formula (tap local o repo).
# Tras publicar un release en GitHub, sustituye sha256 por el valor de:
#   shasum -a 256 git-bn-cli-1.0.1.tgz
#
# Instalación desde este repo:
#   brew install --formula ./packaging/homebrew/git-bn-cli.rb
#
# Tap (reemplaza OWNER/REPO):
#   brew tap OWNER/repo https://github.com/OWNER/homebrew-repo
#   brew install git-bn-cli

class GitBnCli < Formula
  desc "CLI para crear ramas y commits con convención GitBN"
  homepage "https://github.com/manuelduarte077/git-flow-cli"
  url "https://github.com/manuelduarte077/git-flow-cli/releases/download/v1.0.1/git-bn-cli-1.0.1.tgz"
  sha256 "REPLACE_WITH_SHASUM_OF_TGZ"
  license "MIT"

  depends_on "openjdk@21"

  def install
    libexec.install Dir["git-bn-cli-#{version}/*"]
    (bin/"git-bn-cli").write_env_script libexec/"bin/git-bn-cli",
      Language::Java.overridable_java_home_env("21")
  end

  test do
    system "#{bin}/git-bn-cli", "--help"
  end
end
