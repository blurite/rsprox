package net.rsprox.proxy.downloader.cpp

public data class Repository(
    public val repository: String,
    public val version: String,
    public val catalog: String,
    public val alias: String,
) {
    public fun getName(): String {
        return this.repository
    }

    public fun getVersionData(): VersionData {
        return RepositoryDownloader.getConfig(version)
    }

    public fun getAliases(): Map<String, String> {
        return RepositoryDownloader.getConfig(alias)
    }

    public fun getCatalog(id: String): Catalog {
        return RepositoryDownloader.getConfig("$catalog$id/catalog.json")
    }
}
