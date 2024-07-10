package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Message game packet is used to send a normal game message in
 * the player's chatbox.
 *
 * Game message types (note: names without asterisk are official from a leak):
 * ```
 * | Id  |                Type                |
 * |-----|:----------------------------------:|
 * | 0   |        chattype_gamemessage        |
 * | 1   |          chattype_modchat          |
 * | 2   |         chattype_publicchat        |
 * | 3   |        chattype_privatechat        |
 * | 4   |           chattype_engine          |
 * | 5   |  chattype_loginlogoutnotification  |
 * | 6   |       chattype_privatechatout      |
 * | 7   |       chattype_modprivatechat      |
 * | 9   |        chattype_friendschat        |
 * | 11  |  chattype_friendschatnotification  |
 * | 14  |         chattype_broadcast         |
 * | 26  |      chattype_snapshotfeedback     |
 * | 27  |        chattype_obj_examine        |
 * | 28  |        chattype_npc_examine        |
 * | 29  |        chattype_loc_examine        |
 * | 30  |     chattype_friendnotification    |
 * | 31  |     chattype_ignorenotification    |
 * | 41  |           chattype_clan*           |
 * | 43  |        chattype_clan_system*       |
 * | 44  |        chattype_clan_guest*        |
 * | 46  |     chattype_clan_guest_system*    |
 * | 90  |         chattype_autotyper         |
 * | 91  |        chattype_modautotyper       |
 * | 99  |          chattype_console          |
 * | 101 |          chattype_tradereq         |
 * | 102 |           chattype_trade           |
 * | 103 |       chattype_chalreq_trade       |
 * | 104 |    chattype_chalreq_friendschat    |
 * | 105 |            chattype_spam           |
 * | 106 |       chattype_playerrelated       |
 * | 107 |        chattype_10sectimeout       |
 * | 108 |          chattype_welcome*         |
 * | 109 | chattype_clan_creation_invitation* |
 * | 110 |    chattype_clan_wars_challenge*   |
 * | 111 |      chattype_gim_form_group*      |
 * | 112 |      chattype_gim_group_with*      |
 * ```
 *
 * @property type the type of the message to send (see table above)
 * @property name the name of the target player who is making a request.
 * This property is only for messages such as "X wishes to trade with you.",
 * where there is a player at the other end that is making some sort of request.
 * Upon interacting with these chat messages, the client will invoke the respective
 * op-player packet if it can find that player in local player's high resolution
 * list of players.
 * It is important to note, however, that only opplayer 1, 4, 6 and 7 will ever
 * be fired in this manner.
 * @property message the message itself to render in the chatbox
 */
public class MessageGame private constructor(
    private val _type: UShort,
    public val name: String?,
    public val message: String,
) : OutgoingGameMessage {
    public constructor(
        type: Int,
        name: String,
        message: String,
    ) : this(
        type.toUShort(),
        name,
        message,
    )

    public constructor(
        type: Int,
        message: String,
    ) : this(
        type.toUShort(),
        null,
        message,
    )

    public val type: Int
        get() = _type.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageGame

        if (_type != other._type) return false
        if (name != other.name) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _type.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageGame(" +
            "type=$type, " +
            "name=$name, " +
            "message='$message'" +
            ")"
    }
}
