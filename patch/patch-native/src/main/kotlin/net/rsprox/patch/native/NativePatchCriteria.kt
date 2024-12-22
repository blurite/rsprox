package net.rsprox.patch.native

import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchCriteria
import net.rsprox.patch.PatchCriteriaBuilder
import net.rsprox.patch.native.processors.utils.HexBytePattern
import net.rsprox.patch.native.processors.utils.hexPattern

public data class NativePatchCriteria(
    public val type: NativeClientType,
    public val rsaModulus: String? = null,
    public val constStrings: List<ConstStringSlice>,
    public val patternStrings: List<PatternStringSlice>,
    public val bytePatternSlices: List<WildcardHexByteSequenceSlice>,
): PatchCriteria {
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder(
        private val type: NativeClientType,
    ): PatchCriteriaBuilder<NativePatchCriteria>
    {
        private var rsaModulus: String? = null
        private val constStrings: MutableList<ConstStringSlice> = mutableListOf()
        private val patternStrings: MutableList<PatternStringSlice> = mutableListOf()
        private val bytePatternSlices: MutableList<WildcardHexByteSequenceSlice> = mutableListOf()

        public override fun port(port: Int): Builder {
            val originalPort = intToHexStringLE(DEFAULT_PORT)
            val replacementPort = intToHexStringLE(port)
            when (type) {
                NativeClientType.WIN -> {
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

                else -> throw UnsupportedOperationException("$type does not support port replacement.")
            }
            return this
        }

        public fun acceptAllLoopbackAddresses(): Builder {
            when (type) {
                NativeClientType.WIN -> {
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

                else -> throw UnsupportedOperationException("$type does not support loopback address replacement.")
            }
            return this
        }

        public override fun acceptAllHosts(): Builder {
            when (type) {
                NativeClientType.WIN -> {
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

                else -> throw UnsupportedOperationException("$type does not support host replacement.")
            }
            return this
        }

        public override fun javConfig(url: String): Builder {
            constString(
                DEFAULT_JAVCONFIG_URL,
                url,
                FailureBehaviour.ERROR,
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                HIGH_PRIORITY,
            )
            return this
        }

        public override fun worldList(url: String): Builder {
            constString(
                DEFAULT_WORLDLIST_URL,
                url,
                FailureBehaviour.ERROR,
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES,
                HIGH_PRIORITY,
            )
            return this
        }

        public override fun varpCount(
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
        public override fun siteUrl(replacement: String): Builder {
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
        public override fun name(replacement: String): Builder {
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

        public override fun rsaModulus(hexString: String): Builder {
            require(hexString.length <= OSRS_MODULUS_LEN) {
                "RSA Modulus length cannot be greater than the existing one: ${hexString.length}, $OSRS_MODULUS_LEN"
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

        public override fun build(): NativePatchCriteria = NativePatchCriteria(
            type,
            rsaModulus,
            constStrings,
            patternStrings,
            bytePatternSlices,
        )
    }

    public companion object {
        public const val HIGH_PRIORITY: Int = 100
        public const val DEFAULT_PRIORITY: Int = 50
        public const val LOW_PRIORITY: Int = 0
        private const val DEFAULT_PORT: Int = 43594
        private const val OSRS_MODULUS_LEN: Int = 256
        private const val DEFAULT_JAVCONFIG_URL: String = "http://oldschool.runescape.com/jav_config.ws?m=0"
        private const val DEFAULT_WORLDLIST_URL: String = "https://oldschool.runescape.com/slr.ws?order=LPWM"

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
