package net.rsprox.proxy.downloader.cpp

public data class Remote(
    public val baseUrl: String,
    public val deltaFormat: String,
    public val flags: String,
    public val pieceFormat: String,
    public val type: String,
)
