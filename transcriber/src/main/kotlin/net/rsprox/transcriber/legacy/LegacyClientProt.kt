package net.rsprox.transcriber.legacy

public enum class LegacyClientProt(
    public val remappedName: String,
) {
    OPLOC1("OPLOC1_V1"),
    OPLOC2("OPLOC2_V1"),
    OPLOC3("OPLOC3_V1"),
    OPLOC4("OPLOC4_V1"),
    OPLOC5("OPLOC5_V1"),
    OPNPC1("OPNPC1_V1"),
    OPNPC2("OPNPC2_V1"),
    OPNPC3("OPNPC3_V1"),
    OPNPC4("OPNPC4_V1"),
    OPNPC5("OPNPC5_V1"),
    OPOBJ1("OPOBJ1_V1"),
    OPOBJ2("OPOBJ2_V1"),
    OPOBJ3("OPOBJ3_V1"),
    OPOBJ4("OPOBJ4_V1"),
    OPOBJ5("OPOBJ5_V1"),
    ;

    public companion object {
        @JvmStatic private val dictionary = entries.associate { it.name to it.remappedName }

        public operator fun get(name: String): String {
            return dictionary.getOrDefault(name, name)
        }
    }
}
