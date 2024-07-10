package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import java.awt.Color

/**
 * If set-colour is used to set the colour of a text component.
 * @property interfaceId the id of the interface on which the text resides
 * @property componentId the id of the component on which the text resides
 * @property red the value of the red colour, ranging from 0 to 31 (inclusive)
 * @property green the value of the green colour, ranging from 0 to 31 (inclusive)
 * @property blue the value of the blue colour, ranging from 0 to 31 (inclusive)
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfSetColour private constructor(
    public val combinedId: CombinedId,
    private val colour: Rs15BitColour,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        red: Int,
        green: Int,
        blue: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        Rs15BitColour(
            red,
            green,
            blue,
        ),
    )

    /**
     * A secondary constructor to build a colour from [Color].
     * This can be useful to avoid manual colour conversions,
     * as 8-bit colours are typically used.
     * This function will strip away the 3 least significant
     * bits from the colours, as Jagex's colour format only expects
     * 5 bits per colour, so small changes in tone may occur.
     */
    public constructor(
        interfaceId: Int,
        componentId: Int,
        color: Color,
    ) : this(
        CombinedId(interfaceId, componentId),
        Rs15BitColour(
            color.red ushr 3,
            color.green ushr 3,
            color.blue ushr 3,
        ),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val red: Int
        get() = colour.red
    public val green: Int
        get() = colour.green
    public val blue: Int
        get() = colour.blue
    public val colour15BitPacked: Int
        get() = colour.packed.toInt()

    /**
     * Turns the 15-bit RS RGB colour into a 24-bit normalized RGB colour.
     */
    public fun toAwtColor(): Color {
        return Color(
            (red shl 19)
                .or(green shl 11)
                .or(blue shl 3),
        )
    }

    @JvmInline
    private value class Rs15BitColour(
        val packed: UShort,
    ) {
        constructor(
            red: Int,
            green: Int,
            blue: Int,
        ) : this(
            (red and 0x1F shl 10)
                .or(green and 0x1F shl 5)
                .or(blue and 0x1F)
                .toUShort(),
        )

        val red: Int
            get() = packed.toInt() ushr 10 and 0x1F
        val green: Int
            get() = packed.toInt() ushr 5 and 0x1F
        val blue: Int
            get() = packed.toInt() and 0x1F

        override fun toString(): String {
            return "Rs15BitColour(" +
                "red=$red, " +
                "green=$green, " +
                "blue=$blue" +
                ")"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetColour

        if (combinedId != other.combinedId) return false
        if (colour != other.colour) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + colour.hashCode()
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        val packed =
            (red shl 19)
                .or(green shl 11)
                .or(blue shl 3)
        return "IfSetColour(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "red=$red/31, " +
            "green=$green/31, " +
            "blue=$blue/31, " +
            "24-bit RGB colour=${packed.toHexString(HexFormat.UpperCase)}" +
            ")"
    }
}
