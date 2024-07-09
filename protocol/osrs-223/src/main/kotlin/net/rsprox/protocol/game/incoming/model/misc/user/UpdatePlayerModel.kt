package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory
import kotlin.jvm.Throws

/**
 * Update player model packet is sent for the old make-over interface,
 * when the player finishes designing their character. It should be noted,
 * that this is no longer in use in OldSchool RuneScape, as a newer interface
 * uses traditional buttons to manage it. However, as this is still a valid
 * packet that can be sent by the server, we've implemented it.
 * @property bodyType the body type of the player
 * @property identKits the ident kits the player can customize
 * @property colours the colours the player can customize
 */
@Suppress("MemberVisibilityCanBePrivate")
public class UpdatePlayerModel private constructor(
    private val _bodyType: UByte,
    private val identKits: ByteArray,
    private val colours: ByteArray,
) : IncomingGameMessage {
    public constructor(
        bodyType: Int,
        identKits: ByteArray,
        colours: ByteArray,
    ) : this(
        bodyType.toUByte(),
        identKits,
        colours,
    )

    public val bodyType: Int
        get() = _bodyType.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    /**
     * Gets the backing ident kits byte array.
     * Changes done to this byte array reflect on this packet.
     */
    public fun getIdentKitsByteArray(): ByteArray {
        return identKits
    }

    /**
     * Gets the backing colours byte array.
     * Changes done to this byte array reflect on the packet.
     */
    public fun getColoursByteArray(): ByteArray {
        return colours
    }

    /**
     * Gets the ident kit at index [index], or -1 if it doesn't exist.
     * @param index the index of the body part
     * @return ident kit at that body part, or -1 if it doesn't exist
     * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= 7
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public fun getIdentKit(index: Int): Int {
        val value = identKits[index].toInt()
        return if (value == 0xFF) {
            -1
        } else {
            value
        }
    }

    /**
     * Gets the colour at index [index], or -1 if it doesn't exist.
     * @param index the index of the colour
     * @return colour at that index, or -1 if it doesn't exist
     * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= 5
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public fun getColour(index: Int): Int {
        return colours[index].toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdatePlayerModel

        if (_bodyType != other._bodyType) return false
        if (!identKits.contentEquals(other.identKits)) return false
        if (!colours.contentEquals(other.colours)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _bodyType.hashCode()
        result = 31 * result + identKits.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "UpdatePlayerModel(" +
            "bodyType=$bodyType, " +
            "identKits=${identKits.contentToString()}, " +
            "colours=${colours.contentToString()}" +
            ")"
    }
}
