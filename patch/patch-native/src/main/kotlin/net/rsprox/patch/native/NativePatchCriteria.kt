package net.rsprox.patch.native

import net.rsprox.patch.NativeClientType
import net.rsprox.patch.native.processors.utils.HexBytePattern
import net.rsprox.patch.native.processors.utils.hexPattern

public data class NativePatchCriteria(
    public val type: NativeClientType,
    public val rsaModulus: String? = null,
    public val constStrings: List<ConstStringSlice>,
    public val patternStrings: List<PatternStringSlice>,
    public val bytePatternSlices: List<WildcardHexByteSequenceSlice>,
) {
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder(
        private val type: NativeClientType,
    ) {
        private var rsaModulus: String? = null
        private val constStrings: MutableList<ConstStringSlice> = mutableListOf()
        private val patternStrings: MutableList<PatternStringSlice> = mutableListOf()
        private val bytePatternSlices: MutableList<WildcardHexByteSequenceSlice> = mutableListOf()

        public fun port(port: Int): Builder {
            val originalPort = intToHexStringLE(DEFAULT_PORT)
            val replacementPort = intToHexStringLE(port)
            when (type) {
                NativeClientType.WIN, NativeClientType.RS3_WIN -> {
                    wildcardByteSequence(
                        hexPattern(originalPort),
                        hexPattern(replacementPort),
                        FailureBehaviour.ERROR,
                        DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
                    )
                }

                NativeClientType.MAC -> {
                    wildcardByteSequence(
                        hexPattern("BE$originalPort"),
                        hexPattern("BE$replacementPort"),
                        FailureBehaviour.ERROR,
                        DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                    )
                }
            }
            return this
        }

        /** Revision-950 Windows live lobby/world endpoint construction; never patch arbitrary HTTPS constants. */
        public fun rs3LoginPorts(
            primary: Int,
            alternate: Int,
        ): Builder {
            require(type == NativeClientType.RS3_WIN)
            require(primary in 1024..65535 && alternate in 1024..65535 && primary != alternate)
            require(primary != DEFAULT_PORT && alternate != DEFAULT_PORT)
            val first = intToHexStringLE(primary)
            val second = intToHexStringLE(alternate)
            // Each guarded instruction window must occur exactly once. Only the two mov immediates change.
            // Later lobby selection, world selection, and the second world selection entry point respectively.
            val sites =
                listOf(
                    "483986709A0100750E41BE4AAA000041BCBB010000EB1AB8C0630000" to
                        "483986709A0100750E41BE${first}41BC${second}EB1AB8C0630000",
                    "493980709A0100750CBF4AAA0000BEBB010000EB18B8C0630000" to
                        "493980709A0100750CBF${first}BE${second}EB18B8C0630000",
                    "483982709A0100750E41BE4AAA000041BFBB010000EB1BB8C0630000" to
                        "483982709A0100750E41BE${first}41BF${second}EB1BB8C0630000",
                )
            for ((old, new) in sites) {
                wildcardByteSequence(
                    hexPattern(old),
                    hexPattern(new),
                    FailureBehaviour.ERROR,
                    DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                )
            }
            return this
        }

        /** Direct launches do not receive the window title through the official launcher's pipe. */
        public fun rs3WindowTitle(): Builder {
            require(type == NativeClientType.RS3_WIN)
            return constString(
                "RuneTekApp",
                "RuneScape",
                FailureBehaviour.ERROR,
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
            )
        }

        public fun acceptAllLoopbackAddresses(): Builder {
            when (type) {
                NativeClientType.WIN, NativeClientType.RS3_WIN -> {
                    constString(
                        "127.0.0.1",
                        "127.",
                        FailureBehaviour.ERROR,
                        DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                    )
                }
                NativeClientType.MAC -> {
                    wildcardByteSequence(
                        hexPattern("0F85????????E9????????4C3B????0F84????????49"),
                        hexPattern("0F84????????E9????????4C3B????0F84????????49"),
                    )
                }
            }
            return this
        }

        public fun acceptAllHosts(): Builder {
            when (type) {
                NativeClientType.WIN, NativeClientType.RS3_WIN -> {
                    constString(
                        "127.0.0.1",
                        "",
                        FailureBehaviour.ERROR,
                        DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                    )
                }
                NativeClientType.MAC -> {
                    wildcardByteSequence(
                        hexPattern("0F85????????E9????????4C3B????0F84????????49"),
                        hexPattern("0F84????????E9????????4C3B????0F84????????49"),
                        priority = HIGH_PRIORITY,
                    )
                }
            }
            return this
        }

        public fun javConfig(url: String): Builder {
            val defaults =
                when (type) {
                    NativeClientType.RS3_WIN -> listOf(DEFAULT_RS3_JAVCONFIG_URL)
                    else ->
                        listOf(
                            DEFAULT_JAVCONFIG_URL_PRE_231,
                            DEFAULT_JAVCONFIG_URL,
                        )
                }
            constString(
                defaults,
                url,
                FailureBehaviour.ERROR,
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                HIGH_PRIORITY,
            )
            return this
        }

        public fun worldList(url: String): Builder {
            constString(
                listOf(
                    DEFAULT_WORLDLIST_URL_PRE_231,
                    DEFAULT_WORLDLIST_URL,
                ),
                url,
                FailureBehaviour.ERROR,
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                HIGH_PRIORITY,
            )
            return this
        }

        public fun varpCount(
            expectedVarpCount: Int,
            replacementVarpCount: Int,
        ): Builder {
            val input = intToHexStringLE(expectedVarpCount)
            val replacement = intToHexStringLE(replacementVarpCount)
            wildcardByteSequence(
                hexPattern("BA${input}E8"),
                hexPattern("BA${replacement}E8"),
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            return this
        }

        /**
         * Replaces any 'runescape.com' with the input [replacement] url.
         */
        public fun siteUrl(replacement: String): Builder {
            patternString(
                createStringCapturedPattern("runescape.com"),
                "$1$replacement$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            return this
        }

        /**
         * Replaces anything to do with ´oldschool', "runescape" or "oldschool runescape" in various formats.
         * This function will additionally update the cache path, and URL if URL hasn't been previously updated.
         * @param replacement the string to replace by, should be capitalized as a name would be.
         */
        public fun name(replacement: String): Builder {
            patternString(
                createStringCapturedPattern("RuneScape or Old School RuneScape"),
                "$1$replacement$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            patternString(
                createStringCapturedPattern("runescape"),
                "$1${replacement.lowercase()}$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            patternString(
                createStringCapturedPattern("Old School Rune[sS]cape"),
                "$1$replacement$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            patternString(
                createStringCapturedPattern("OldSchool RuneScape"),
                "$1$replacement$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            patternString(
                createStringCapturedPattern("Old School password"),
                "$1$replacement Password$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            patternString(
                createStringCapturedPattern("Rune[sS]cape"),
                "$1$replacement$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            patternString(
                createStringCapturedPattern("oldschool"),
                "$1${replacement.lowercase()}$2",
                duplicateBehaviour = DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES,
            )
            return this
        }

        public fun rsaModulus(hexString: String): Builder {
            require(hexString.length <= MAX_MODULUS_LEN) {
                "RSA Modulus length cannot be greater than $MAX_MODULUS_LEN: ${hexString.length}"
            }
            this.rsaModulus = hexString
            return this
        }

        public fun constString(
            old: String,
            new: String,
            failureBehaviour: FailureBehaviour = FailureBehaviour.WARN,
            duplicateBehaviour: DuplicateReplacementBehaviour = DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
            priority: Int = DEFAULT_PRIORITY,
        ): Builder {
            require(new.length <= old.length) {
                "New string length cannot be longer than the old: '$old' > '$new'"
            }
            this.constStrings +=
                ConstStringSlice(
                    listOf(old),
                    new,
                    failureBehaviour,
                    duplicateBehaviour,
                    priority,
                )
            return this
        }

        public fun constString(
            old: List<String>,
            new: String,
            failureBehaviour: FailureBehaviour = FailureBehaviour.WARN,
            duplicateBehaviour: DuplicateReplacementBehaviour = DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
            priority: Int = DEFAULT_PRIORITY,
        ): Builder {
            for (element in old) {
                require(new.length <= element.length) {
                    "New string length cannot be longer than the old: '$element' > '$new'"
                }
            }
            this.constStrings +=
                ConstStringSlice(
                    old,
                    new,
                    failureBehaviour,
                    duplicateBehaviour,
                    priority,
                )
            return this
        }

        public fun patternString(
            pattern: Regex,
            replacement: String,
            failureBehaviour: FailureBehaviour = FailureBehaviour.WARN,
            duplicateBehaviour: DuplicateReplacementBehaviour = DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
            priority: Int = DEFAULT_PRIORITY,
        ): Builder {
            this.patternStrings +=
                PatternStringSlice(
                    pattern,
                    replacement,
                    failureBehaviour,
                    duplicateBehaviour,
                    priority,
                )
            return this
        }

        public fun wildcardByteSequence(
            old: HexBytePattern,
            new: HexBytePattern,
            failureBehaviour: FailureBehaviour = FailureBehaviour.ERROR,
            duplicateBehaviour: DuplicateReplacementBehaviour = DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
            priority: Int = DEFAULT_PRIORITY,
        ): Builder {
            bytePatternSlices +=
                WildcardHexByteSequenceSlice(
                    old,
                    new,
                    failureBehaviour,
                    duplicateBehaviour,
                    priority,
                )
            return this
        }

        public fun build(): NativePatchCriteria {
            return NativePatchCriteria(
                type,
                rsaModulus,
                constStrings,
                patternStrings,
                bytePatternSlices,
            )
        }
    }

    public companion object {
        public const val HIGH_PRIORITY: Int = 100
        public const val DEFAULT_PRIORITY: Int = 50
        public const val LOW_PRIORITY: Int = 0
        private const val DEFAULT_PORT: Int = 43594
        public const val OSRS_MODULUS_LEN: Int = 256
        public const val MAX_MODULUS_LEN: Int = 1024
        public const val DEFAULT_RS3_JAVCONFIG_URL: String = "https://world5.runescape.com/jav_config.ws?binaryType=2"
        private const val DEFAULT_JAVCONFIG_URL_PRE_231: String = "http://oldschool.runescape.com/jav_config.ws?m=0"
        private const val DEFAULT_JAVCONFIG_URL: String = "https://oldschool.config.runescape.com/jav_config.ws?m=0"
        private const val DEFAULT_WORLDLIST_URL_PRE_231: String = "https://oldschool.runescape.com/slr.ws?order=LPWM"
        private const val DEFAULT_WORLDLIST_URL: String = "https://oldschool.config.runescape.com/slr.ws?order=LPWM"

        private fun createStringCapturedPattern(string: String): Regex {
            return Regex("""(\00[a-zA-Z0-9./\-_!?:;* ]*)$string([a-zA-Z0-9./\-_!?:;* ]*\00)""")
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun intToHexStringLE(num: Int): String {
            // Converts a 32-bit integer to a little-endian hex string
            val first = (num and 0xFF).toByte().toHexString(HexFormat.UpperCase)
            val second = (num ushr 8).toByte().toHexString(HexFormat.UpperCase)
            val third = (num ushr 16).toByte().toHexString(HexFormat.UpperCase)
            val fourth = (num ushr 24).toByte().toHexString(HexFormat.UpperCase)
            return "$first$second$third$fourth"
        }
    }
}
