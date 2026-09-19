package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.StoreServerPermVarcs
import net.rsprox.protocol.rs3v950.cache.readTypedVariable
import net.rsprox.protocol.session.Session

internal class StoreServerPermVarcsDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<StoreServerPermVarcs> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): StoreServerPermVarcs {
        val complete = buffer.g1()
        val variables =
            buildList {
                while (buffer.isReadable) {
                    add(buffer.readTypedVariable(session, Rs3VariableDomain.CLIENT, fromClient = true))
                }
            }
        return StoreServerPermVarcs(complete, variables)
    }
}
