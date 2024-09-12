package net.rsprox.protocol.game.incoming.model.npcs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpNpcT messages are fired whenever an interface component is targeted
 * on a NPC, which, as of revision 204, includes using items from
 * the player's inventory on NPCs - the OpNpcU message was deprecated.
 * @property index the index of the npc the component was used on
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property selectedInterfaceId the interface id of the selected component
 * @property selectedComponentId the component id being used on the npc
 * @property selectedSub the subcomponent of the selected component, or -1 of none exists
 * @property selectedObj the obj on the selected subcomponent, or -1 if none exists
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class OpNpcT private constructor(
    private val _index: UShort,
    public val controlKey: Boolean,
    private val selectedCombinedId: CombinedId,
    private val _selectedSub: UShort,
    private val _selectedObj: UShort,
) : IncomingGameMessage {
    public constructor(
        index: Int,
        controlKey: Boolean,
        selectedCombinedId: CombinedId,
        selectedSub: Int,
        selectedObj: Int,
    ) : this(
        index.toUShort(),
        controlKey,
        selectedCombinedId,
        selectedSub.toUShort(),
        selectedObj.toUShort(),
    )

    public val index: Int
        get() = _index.toInt()
    public val selectedInterfaceId: Int
        get() = selectedCombinedId.interfaceId
    public val selectedComponentId: Int
        get() = selectedCombinedId.componentId
    public val selectedSub: Int
        get() = _selectedSub.toIntOrMinusOne()
    public val selectedObj: Int
        get() = _selectedObj.toIntOrMinusOne()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpNpcT

        if (_index != other._index) return false
        if (controlKey != other.controlKey) return false
        if (selectedCombinedId != other.selectedCombinedId) return false
        if (_selectedSub != other._selectedSub) return false
        if (_selectedObj != other._selectedObj) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + controlKey.hashCode()
        result = 31 * result + selectedCombinedId.hashCode()
        result = 31 * result + _selectedSub.hashCode()
        result = 31 * result + _selectedObj.hashCode()
        return result
    }

    override fun toString(): String {
        return "OpNpcT(" +
            "index=$index, " +
            "controlKey=$controlKey, " +
            "selectedInterfaceId=$selectedInterfaceId, " +
            "selectedComponentId=$selectedComponentId, " +
            "selectedSub=$selectedSub, " +
            "selectedObj=$selectedObj" +
            ")"
    }
}
