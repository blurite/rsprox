package net.rsprox.protocol.game.incoming.model.objs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpObjT messages are fired whenever an interface component is targeted
 * on an obj on the ground, which, as of revision 204, includes using items from
 * the player's inventory on objs - the OpObjU message was deprecated.
 * @property id the id of the obj the component was used on
 * @property x the absolute x coordinate of the obj
 * @property z the absolute z coordinate of the obj
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property selectedInterfaceId the interface id of the selected component
 * @property selectedComponentId the component id being used on the obj
 * @property selectedSub the subcomponent of the selected component, or -1 of none exists
 * @property selectedObj the obj on the selected subcomponent, or -1 if none exists
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class OpObjT private constructor(
    private val _id: UShort,
    private val _x: UShort,
    private val _z: UShort,
    public val controlKey: Boolean,
    private val selectedCombinedId: CombinedId,
    private val _selectedSub: UShort,
    private val _selectedObj: UShort,
) : IncomingGameMessage {
    public constructor(
        id: Int,
        x: Int,
        z: Int,
        controlKey: Boolean,
        selectedCombinedId: CombinedId,
        selectedSub: Int,
        selectedObj: Int,
    ) : this(
        id.toUShort(),
        x.toUShort(),
        z.toUShort(),
        controlKey,
        selectedCombinedId,
        selectedSub.toUShort(),
        selectedObj.toUShort(),
    )

    public val id: Int
        get() = _id.toInt()
    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
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

        other as OpObjT

        if (_id != other._id) return false
        if (_x != other._x) return false
        if (_z != other._z) return false
        if (controlKey != other.controlKey) return false
        if (selectedCombinedId != other.selectedCombinedId) return false
        if (_selectedSub != other._selectedSub) return false
        if (_selectedObj != other._selectedObj) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + controlKey.hashCode()
        result = 31 * result + selectedCombinedId.hashCode()
        result = 31 * result + _selectedSub.hashCode()
        result = 31 * result + _selectedObj.hashCode()
        return result
    }

    override fun toString(): String {
        return "OpObjT(" +
            "id=$id, " +
            "x=$x, " +
            "z=$z, " +
            "controlKey=$controlKey, " +
            "selectedInterfaceId=$selectedInterfaceId, " +
            "selectedComponentId=$selectedComponentId, " +
            "selectedSub=$selectedSub, " +
            "selectedObj=$selectedObj" +
            ")"
    }
}
