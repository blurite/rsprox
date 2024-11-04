package net.rsprox.protocol.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * If button drag messages are sent whenever an obj is dragged from one subcomponent
 * to another.
 * @property selectedCombinedId the bitpacked combination of [selectedInterfaceId] and [selectedComponentId].
 * @property selectedInterfaceId the interface id from which the obj is dragged
 * @property selectedComponentId the component on that selected interface from which
 * the obj is dragged
 * @property selectedSub the subcomponent from which the obj is dragged,
 * or -1 if none exists
 * @property selectedObj the obj that is being dragged, or -1 if none exists
 * @property targetCombinedId the bitpacked combination of [targetInterfaceId] and [targetComponentId].
 * @property targetInterfaceId the interface id to which the obj is being dragged
 * @property targetComponentId the component of the target interface to which
 * the obj is being dragged
 * @property targetSub the subcomponent of the target to which the obj is being dragged,
 * or -1 if none exists
 * @property targetObj the obj in that subcomponent which is being dragged on,
 * or -1 if there is no obj in the target position
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class IfButtonD private constructor(
    private val _selectedCombinedId: CombinedId,
    private val _selectedSub: UShort,
    private val _selectedObj: UShort,
    private val _targetCombinedId: CombinedId,
    private val _targetSub: UShort,
    private val _targetObj: UShort,
) : IncomingGameMessage {
    public constructor(
        selectedCombinedId: CombinedId,
        selectedSub: Int,
        selectedObj: Int,
        targetCombinedId: CombinedId,
        targetSub: Int,
        targetObj: Int,
    ) : this(
        selectedCombinedId,
        selectedSub.toUShort(),
        selectedObj.toUShort(),
        targetCombinedId,
        targetSub.toUShort(),
        targetObj.toUShort(),
    )

    public val selectedCombinedId: Int
        get() = _selectedCombinedId.combinedId
    public val selectedInterfaceId: Int
        get() = _selectedCombinedId.interfaceId
    public val selectedComponentId: Int
        get() = _selectedCombinedId.componentId
    public val selectedSub: Int
        get() = _selectedSub.toIntOrMinusOne()
    public val selectedObj: Int
        get() = _selectedObj.toIntOrMinusOne()
    public val targetCombinedId: Int
        get() = _targetCombinedId.combinedId
    public val targetInterfaceId: Int
        get() = _targetCombinedId.interfaceId
    public val targetComponentId: Int
        get() = _targetCombinedId.componentId
    public val targetSub: Int
        get() = _targetSub.toIntOrMinusOne()
    public val targetObj: Int
        get() = _targetObj.toIntOrMinusOne()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfButtonD

        if (_selectedCombinedId != other._selectedCombinedId) return false
        if (_selectedSub != other._selectedSub) return false
        if (_selectedObj != other._selectedObj) return false
        if (_targetCombinedId != other._targetCombinedId) return false
        if (_targetSub != other._targetSub) return false
        if (_targetObj != other._targetObj) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _selectedCombinedId.hashCode()
        result = 31 * result + _selectedSub.hashCode()
        result = 31 * result + _selectedObj.hashCode()
        result = 31 * result + _targetCombinedId.hashCode()
        result = 31 * result + _targetSub.hashCode()
        result = 31 * result + _targetObj.hashCode()
        return result
    }

    override fun toString(): String =
        "IfButtonD(" +
            "selectedInterfaceId=$selectedInterfaceId, " +
            "selectedComponentId=$selectedComponentId, " +
            "selectedSub=$selectedSub, " +
            "selectedObj=$selectedObj, " +
            "targetInterfaceId=$targetInterfaceId, " +
            "targetComponentId=$targetComponentId, " +
            "targetSub=$targetSub, " +
            "targetObj=$targetObj" +
            ")"
}
