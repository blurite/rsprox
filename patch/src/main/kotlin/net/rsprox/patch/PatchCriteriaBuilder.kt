package net.rsprox.patch

public interface PatchCriteriaBuilder<T : PatchCriteria> {
    public fun port(port: Int): PatchCriteriaBuilder<T>
    public fun rsaModulus(hexString: String): PatchCriteriaBuilder<T>
    public fun acceptAllHosts(): PatchCriteriaBuilder<T>
    public fun javConfig(url: String): PatchCriteriaBuilder<T>
    public fun worldList(url: String): PatchCriteriaBuilder<T>
    public fun varpCount(expectedVarpCount: Int, replacementVarpCount: Int): PatchCriteriaBuilder<T>
    public fun siteUrl(replacement: String): PatchCriteriaBuilder<T>
    public fun name(replacement: String): PatchCriteriaBuilder<T>
    public fun build(): T
}
