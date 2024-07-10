package net.rsprox.protocol.game.outgoing.decoder.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamLookAtEasedAngleAbsoluteDecoder

public object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(CamLookAtEasedAngleAbsoluteDecoder())
            }
        return builder.build()
    }
}
