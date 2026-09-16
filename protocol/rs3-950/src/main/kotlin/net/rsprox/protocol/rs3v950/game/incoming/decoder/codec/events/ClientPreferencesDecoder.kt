package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.ClientPreferences
import net.rsprox.protocol.session.Session

internal class ClientPreferencesDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ClientPreferences> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClientPreferences {
        val version = buffer.g1()
        require(version == 38) { "Unsupported RS3 preference version $version" }
        val compatibility1 = buffer.g1()
        val compatibility2 = buffer.g1()
        val setting0 = buffer.g1()
        val setting1 = buffer.g1()
        val setting2 = buffer.g1()
        val compatibility6 = buffer.g1()
        val setting3 = buffer.g1()
        val setting4 = buffer.g1()
        val compatibility9 = buffer.g1()
        val setting5 = buffer.g1()
        val setting6 = buffer.g1()
        val compatibility12 = buffer.g1()
        val compatibility13 = buffer.g1()
        val compatibility14 = buffer.g1()
        val setting7 = buffer.g1()
        val compatibility16 = buffer.g1()
        val compatibility17 = buffer.g1()
        val compatibility18 = buffer.g1()
        val setting8 = buffer.g1()
        val compatibility20 = buffer.g1()
        val compatibility21 = buffer.g1()
        val compatibility22 = buffer.g1()
        val setting9 = buffer.g1()
        val compatibility24 = buffer.g1()
        val compatibility25 = buffer.g1()
        val compatibility26 = buffer.g1()
        val setting10 = buffer.g1()
        val setting11 = buffer.g1()
        val setting12 = buffer.g1()
        val setting13 = buffer.g1()
        val setting14 = buffer.g1()
        val setting15 = buffer.g1()
        val setting16 = buffer.g1()
        val setting17 = buffer.g1()
        val setting18 = buffer.g1()
        val setting19 = buffer.g2()
        val setting20 = buffer.g2()
        val setting21 = buffer.g2()
        val setting22 = buffer.g2()
        val setting23 = buffer.g1()
        val setting24 = buffer.g1()
        val compatibility46 = buffer.g1()
        val compatibility47 = buffer.g1()
        val compatibility48 = buffer.g1()
        val compatibility49 = buffer.g1()
        val compatibility50 = buffer.g1()
        val compatibility51 = buffer.g1()
        val setting25 = buffer.g1()
        val setting26 = buffer.g1()
        val setting27 = buffer.g1()
        val setting28 = buffer.g1()
        val setting29 = buffer.g1()
        val compatibility57 = buffer.g1()
        return ClientPreferences(
            version,
            compatibility1,
            compatibility2,
            setting0,
            setting1,
            setting2,
            compatibility6,
            setting3,
            setting4,
            compatibility9,
            setting5,
            setting6,
            compatibility12,
            compatibility13,
            compatibility14,
            setting7,
            compatibility16,
            compatibility17,
            compatibility18,
            setting8,
            compatibility20,
            compatibility21,
            compatibility22,
            setting9,
            compatibility24,
            compatibility25,
            compatibility26,
            setting10,
            setting11,
            setting12,
            setting13,
            setting14,
            setting15,
            setting16,
            setting17,
            setting18,
            setting19,
            setting20,
            setting21,
            setting22,
            setting23,
            setting24,
            compatibility46,
            compatibility47,
            compatibility48,
            compatibility49,
            compatibility50,
            compatibility51,
            setting25,
            setting26,
            setting27,
            setting28,
            setting29,
            compatibility57,
        )
    }
}
