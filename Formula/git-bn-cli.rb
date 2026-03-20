# Homebrew formula (ubicación requerida por Homebrew 5: Formula/ en un tap).
# Tras publicar un release en GitHub, sustituye sha256 por el valor de:
#   shasum -a 256 git-bn-cli-1.0.1.tgz
#
# Instalación desde GitHub (recomendado, Homebrew 5+):
#   brew tap manuelduarte077/git-flow-cli https://github.com/manuelduarte077/git-flow-cli
#   brew install git-bn-cli

class GitBnCli < Formula
  desc "CLI para crear ramas y commits con convención GitBN"
  homepage "https://github.com/manuelduarte077/git-flow-cli"
  url "https://github.com/manuelduarte077/git-flow-cli/releases/download/v1.0.1/git-bn-cli-1.0.1.tgz"
  sha256 "62649315e805f235347bf35ec6359af11e7e76a1725b15d504ead527ff40d8d6"
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
