package net.rsprox.shared.account

public data class JagexAccount(
    public val code: String,
    public val idToken: String,
    public var sessionId: String? = null,
) {
    val characters: List<JagexCharacter> = arrayListOf<JagexCharacter>()

    public fun updateCharacters(characters: List<JagexCharacter>) {
        val stored = this.characters as ArrayList<JagexCharacter>
        stored.clear()
        stored.addAll(characters)
    }
}
