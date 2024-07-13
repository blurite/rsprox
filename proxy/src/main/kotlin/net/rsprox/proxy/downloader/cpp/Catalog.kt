package net.rsprox.proxy.downloader.cpp

public data class Catalog(
    public val config: CatalogConfig,
    public val id: String,
    public val metafile: String,
) {
    public fun getMetafile(): Metafile {
        return RepositoryDownloader.getConfig(metafile)
    }
}
