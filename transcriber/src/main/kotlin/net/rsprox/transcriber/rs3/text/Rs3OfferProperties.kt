package net.rsprox.transcriber.rs3.text

import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateStockmarketSlotV2
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.int

private val offerStages = listOf("empty", "adding", "added", "verifying", "aborting", "finished")

internal fun Property.offerState(message: UpdateStockmarketSlotV2) {
    val packed = message.offer?.updatedState ?: message.state
    val stage = packed and 7
    val label = offerStages.getOrNull(stage)
    if (label != null) any("stage", label) else int("stage", stage)
    if (stage != 0) any("side", if (packed and 8 == 0) "buy" else "sell")
    filteredInt("unknownflags", packed and 0xF0, 0)
    if (message.offer?.updatedState != null && message.state != 7) int("headerstate", message.state)
}
