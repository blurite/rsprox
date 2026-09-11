package net.rsprox.protocol.rs3v950.game.incoming.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfButtonXDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventAppletFocusDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventNativeMouseClickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.locs.OpLocDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.MoveGameClickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.npcs.OpNpcDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.objs.OpObjDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.players.OpPlayerDecoder

internal object ClientMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(@Suppress("UNUSED_PARAMETER") huffmanCodec: HuffmanCodec): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder = MessageDecoderRepositoryBuilder(protRepository).apply {
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON1_V2, 1))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON2_V2, 2))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON3_V2, 3))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON4_V2, 4))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON5_V2, 5))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON6_V2, 6))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON7_V2, 7))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON8_V2, 8))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON9_V2, 9))
            bind(IfButtonXDecoder(GameClientProt.IF_BUTTON10_V2, 10))
            for (decoder in OpNpcDecoder.all()) bind(decoder)
            for (decoder in OpLocDecoder.all()) bind(decoder)
            for (decoder in OpObjDecoder.all()) bind(decoder)
            for (decoder in OpPlayerDecoder.all()) bind(decoder)
            bind(EventAppletFocusDecoder(GameClientProt.EVENT_APPLET_FOCUS))
            bind(EventNativeMouseClickDecoder(GameClientProt.EVENT_NATIVE_MOUSE_CLICK))
            bind(MoveGameClickDecoder(GameClientProt.MOVE_GAMECLICK))
        }
        return builder.build()
    }
}
