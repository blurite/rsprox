package net.rsprox.proxy.downloader.cpp

public data class Metafile(
    public val id: String,
    public val files: List<File>,
    public val pad: List<Pad>,
    public val pieces: Pieces,
    public val version: String,
    public val scanTime: Int,
    public val algorithm: String,
) {
    public data class File(
        public val attr: Int,
        public val name: String,
        public val size: Long,
    )

    public data class Pad(
        public val offset: Long,
        public val size: Long,
    )

    public data class Pieces(
        public val algorithm: String,
        public val digests: List<String>,
        public val hashPadding: Boolean,
    )
}
