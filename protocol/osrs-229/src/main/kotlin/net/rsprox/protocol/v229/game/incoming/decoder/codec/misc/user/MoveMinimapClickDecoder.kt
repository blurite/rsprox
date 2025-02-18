package net.rsprox.protocol.v229.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.incoming.decoder.prot.GameClientProt

public class MoveMinimapClickDecoder : ProxyMessageDecoder<MoveMinimapClick> {
    override val prot: ClientProt = GameClientProt.MOVE_MINIMAPCLICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MoveMinimapClick {
        // The x, z and keyCombination get scrambled between revisions
        val keyCombination = buffer.g1Alt3()
        val z = buffer.g2()
        val x = buffer.g2Alt1()

        // The arguments below are consistent across revisions
        val minimapWidth = buffer.g1()
        val minimapHeight = buffer.g1()
        val cameraAngleY = buffer.g2()
        val checkpoint1 = buffer.g1()
        check(checkpoint1 == 57) {
            "Invalid checkpoint 1: $checkpoint1"
        }
        val checkpoint2 = buffer.g1()
        check(checkpoint2 == 0) {
            "Invalid checkpoint 2: $checkpoint2"
        }
        val checkpoint3 = buffer.g1()
        check(checkpoint3 == 0) {
            "Invalid checkpoint 3: $checkpoint3"
        }
        val checkpoint4 = buffer.g1()
        check(checkpoint4 == 89) {
            "Invalid checkpoint 4: $checkpoint4"
        }
        val fineX = buffer.g2()
        val fineZ = buffer.g2()
        val checkpoint5 = buffer.g1()
        check(checkpoint5 == 63) {
            "Invalid checkpoint 5: $checkpoint5"
        }
        return MoveMinimapClick(
            x,
            z,
            keyCombination,
            minimapWidth,
            minimapHeight,
            cameraAngleY,
            fineX,
            fineZ,
        )
    }
}
