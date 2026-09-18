package net.rsprox.proxy.rs3.login

public class Rs3ServerLoginResponseFramer : Rs3LoginSuccessFramer(world = false) {
    public val displayName: String?
        get() {
            val data = loginData ?: return null
            // Revision 950, FUN_002db7a0: auth flag/token, 47 account bytes, then a gjstr2 name.
            val marker = 1 + initialCipherDraws + 47
            if (data.getOrNull(marker) != 0.toByte()) return null
            val start = marker + 1
            val end = (start until data.size).firstOrNull { data[it] == 0.toByte() } ?: return null
            return String(data, start, end - start, java.nio.charset.Charset.forName("windows-1252"))
                .replace("\uFFFD", "")
        }
}
