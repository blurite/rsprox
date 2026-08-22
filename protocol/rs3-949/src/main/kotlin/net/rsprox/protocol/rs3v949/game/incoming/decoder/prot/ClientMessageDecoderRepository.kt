package net.rsprox.protocol.rs3v949.game.incoming.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.buttons.IfButtonXDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.locs.OpLocDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.npcs.OpNpcDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.objs.OpObjDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.players.OpPlayerDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.events.EventAppletFocusDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.events.EventNativeMouseClickDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.misc.user.MoveGameClickDecoder

internal object ClientMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(huffmanCodec: HuffmanCodec): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON1, 1))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON2, 2))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON3, 3))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON4, 4))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON5, 5))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON6, 6))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON7, 7))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON8, 8))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON9, 9))
                bind(IfButtonXDecoder(GameClientProt.IF_BUTTON10, 10))
                for (decoder in OpNpcDecoder.all()) {
                    bind(decoder)
                }
                for (decoder in OpLocDecoder.all()) {
                    bind(decoder)
                }
                for (decoder in OpObjDecoder.all()) {
                    bind(decoder)
                }
                for (decoder in OpPlayerDecoder.all()) {
                    bind(decoder)
                }
                bind(EventAppletFocusDecoder(GameClientProt.EVENT_APPLET_FOCUS_135))
                bind(EventNativeMouseClickDecoder(GameClientProt.SEND_NATIVE_MOUSE_CLICK))
                bind(MoveGameClickDecoder(GameClientProt.MOVE_GAMECLICK))
            }
        return builder.build()
    }
}
