package net.rsprox.cache.api.type

public interface GameValType {
    public val gameVal: GameVal
    public val id: Int

    public fun getParent(): String {
        return checkNotNull(getParentOrNull()) {
            "Parent is null for $gameVal:$id"
        }
    }

    public fun getChild(childId: Int): String? {
        return checkNotNull(getChildOrNull(childId)) {
            "Child is null for $gameVal:$id:$childId"
        }
    }

    public fun getParentOrNull(): String?

    public fun getChildOrNull(childId: Int): String?
}
