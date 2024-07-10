package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Npc head-icon specific packets are used to render a head icon over
 * a given NPC to one user alone, and not the rest of the world.
 * It is worth noting, however, that the head icon will only be set
 * if the given NPC was already registered by the NPC INFO packet.
 * If a given NPC is removed from the local view through NPC INFO,
 * the head icon goes alongside, and will not be automatically
 * restored should that NPC re-enter the local view.
 * @property index the index of the npc in the world
 * @property headIconSlot the slot of the head icon, a value of 0 to 7 (inclusive)
 * @property spriteGroup the cache group id of the sprite.
 * While the client reads a 32-bit integer for this value, the client
 * does not allow for a value greater than 65535 to be used due to cache limitations,
 * thus, in order to compress the packet even further, we also limit the id to a maximum
 * of 65535.
 * @property spriteIndex the index of the sprite within the sprite file in the cache.
 * Note that this is not the id of the file in the cache group, as for sprites, this is always
 * zero. Each sprite file itself defines a number of sprites - this is the index in that list
 * of sprites.
 * @throws IllegalArgumentException if the [headIconSlot] is not in range of 0 to 7 (inclusive)
 */
public class NpcHeadIconSpecific private constructor(
    private val _index: UShort,
    private val _headIconSlot: UByte,
    private val _spriteGroup: UShort,
    private val _spriteIndex: UShort,
) : OutgoingGameMessage {
    public constructor(
        index: Int,
        headIconSlot: Int,
        spriteGroup: Int,
        spriteIndex: Int,
    ) : this(
        index.toUShort(),
        headIconSlot.toUByte(),
        spriteGroup.toUShort(),
        spriteIndex.toUShort(),
    ) {
        require(headIconSlot in 0..<8) {
            "Head icon slot must be in range of 0 to 7 (inclusive)"
        }
    }

    public val index: Int
        get() = _index.toInt()
    public val headIconSlot: Int
        get() = _headIconSlot.toInt()
    public val spriteGroup: Int
        get() = _spriteGroup.toInt()
    public val spriteIndex: Int
        get() = _spriteIndex.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NpcHeadIconSpecific

        if (_index != other._index) return false
        if (_headIconSlot != other._headIconSlot) return false
        if (_spriteGroup != other._spriteGroup) return false
        if (_spriteIndex != other._spriteIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + _headIconSlot.hashCode()
        result = 31 * result + _spriteGroup.hashCode()
        result = 31 * result + _spriteIndex.hashCode()
        return result
    }

    override fun toString(): String {
        return "NpcHeadIconSpecific(" +
            "index=$index, " +
            "headIconSlot=$headIconSlot, " +
            "spriteGroup=$spriteGroup, " +
            "spriteIndex=$spriteIndex" +
            ")"
    }
}
