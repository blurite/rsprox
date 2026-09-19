package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateRunEnergy
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateRunEnergyDecoder : ProxyMessageDecoder<UpdateRunEnergy> {
    override val prot: ClientProt = GameServerProt.UPDATE_RUNENERGY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRunEnergy {
        val energy = buffer.g1()
        return UpdateRunEnergy(energy)
    }
}
