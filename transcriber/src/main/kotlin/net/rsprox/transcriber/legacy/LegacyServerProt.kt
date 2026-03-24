package net.rsprox.transcriber.legacy

public enum class LegacyServerProt(
    public val remappedName: String,
) {
    ;

    public companion object {
        @JvmStatic private val dictionary = entries.associate { it.name to it.remappedName }

        public operator fun get(name: String): String {
            return dictionary.getOrDefault(name, name)
        }
    }
}
