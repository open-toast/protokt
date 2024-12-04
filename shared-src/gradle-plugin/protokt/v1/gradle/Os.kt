package protokt.v1.gradle

class Os private constructor(
    val kind: Kind,
    val arch: Arch
) {
    companion object {
        val current: Os by lazy {
            val os = System.getProperty("os.name").lowercase()
            val arch = System.getProperty("os.arch").lowercase()

            Os(
                kind = if (os.contains("mac")) {
                    Kind.MACOS
                } else if (os.contains("linux")) {
                    Kind.LINUX
                } else if (os.contains("windows")) {
                    Kind.WINDOWS
                } else {
                    Kind.UNKNOWN
                },

                arch = if (arch.contains("x64") || arch.contains("amd64")) {
                    Arch.X64
                } else if (arch.contains("aarch64")) {
                    Arch.ARM64
                } else {
                    Arch.UNKNOWN
                }
            )
        }
    }

    enum class Kind {
        MACOS, LINUX, WINDOWS, UNKNOWN
    }

    enum class Arch {
        X64, ARM64, UNKNOWN
    }

    val conformanceClassifier by lazy {
        when (kind) {
            Kind.LINUX -> "Linux"
            Kind.MACOS -> "macOS"
            else -> error("Unsupported OS $kind")
        } + "-" + when (arch) {
            Arch.X64 -> "X64"
            Arch.ARM64 -> "ARM64"
            else -> error("Unsupported CPU architecture $arch")
        }
    }
}