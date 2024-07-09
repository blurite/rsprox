package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Send snapshot message is sent when a player reports another player.
 *
 * Rules table:
 * ```
 * | Id |                     Rule                    |
 * |----|:-------------------------------------------:|
 * | 3  |               Exploiting a bug              |
 * | 4  |             Staff impersonation             |
 * | 5  |     Buying/selling accounts and services    |
 * | 6  |           Macroing or use of bots           |
 * | 7  |       Boxing in the Deadman Tournament      |
 * | 8  |          Encouraging rule breaking          |
 * | 10 |             Advertising websites            |
 * | 11 |       Muling in the Deadman Tournament      |
 * | 12 | Asking for or providing contact information |
 * | 14 |                   Scamming                  |
 * | 15 |         Seriously offensive language        |
 * | 16 |                 Solicitation                |
 * | 17 |             Disruptive behaviour            |
 * | 18 |            Offensive account name           |
 * | 19 |              Real-life threats              |
 * | 20 |           Breaking real-world laws          |
 * | 21 |          Player-run Games of Chance         |
 * ```
 *
 * @property name the name of the player that is being reported
 * @property ruleId the rule that the player broke (see table above).
 * Note that the rule ids are internal and not what one sees on the interface,
 * as the rule ids must be persistent across years of usage. Additionally,
 * the "Boxing in Deadman Tournament" and "Muling in the Deadman Tournament"
 * rules can only be selected if the player is logged into a Deadman world.
 * Additionally worth noting that the rule ids are 1 less than what is shown
 * in clientscripts, as the clientscript command behind sending the snapshot
 * decrements 1 from the value prior to submitting it to the server.
 * @property mute whether to mute the player. This option is only possible
 * by Player and Jagex moderators.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class SendSnapshot private constructor(
    public val name: String,
    private val _ruleId: UByte,
    public val mute: Boolean,
) : IncomingGameMessage {
    public constructor(
        name: String,
        ruleId: Int,
        mute: Boolean,
    ) : this(
        name,
        ruleId.toUByte(),
        mute,
    )

    public val ruleId: Int
        get() = _ruleId.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendSnapshot

        if (name != other.name) return false
        if (_ruleId != other._ruleId) return false
        if (mute != other.mute) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + _ruleId.hashCode()
        result = 31 * result + mute.hashCode()
        return result
    }

    override fun toString(): String {
        return "SendSnapshot(" +
            "name='$name', " +
            "ruleId=$ruleId, " +
            "mute=$mute" +
            ")"
    }
}
