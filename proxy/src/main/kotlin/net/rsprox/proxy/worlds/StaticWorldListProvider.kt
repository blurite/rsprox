package net.rsprox.proxy.worlds

public class StaticWorldListProvider(
    private val worldList: WorldList,
) : WorldListProvider {
    override fun get(): WorldList {
        return worldList
    }
}
