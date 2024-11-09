package net.rsprox.shared.account

public interface JagexAccountStore {
    public val accounts: List<JagexAccount>
    public var selectedCharacterId: Int?

    public fun add(account: JagexAccount)

    public fun delete(account: JagexAccount)
}
