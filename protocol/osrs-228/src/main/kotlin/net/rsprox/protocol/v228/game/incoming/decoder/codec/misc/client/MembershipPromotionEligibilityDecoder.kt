package net.rsprox.protocol.v228.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.client.MembershipPromotionEligibility
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class MembershipPromotionEligibilityDecoder : ProxyMessageDecoder<MembershipPromotionEligibility> {
    override val prot: ClientProt = GameClientProt.MEMBERSHIP_PROMOTION_ELIGIBILITY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MembershipPromotionEligibility {
        val eligibleForIntroductoryPrice = buffer.g1()
        val eligibleForTrialPurchase = buffer.g1()
        return MembershipPromotionEligibility(
            eligibleForIntroductoryPrice,
            eligibleForTrialPurchase,
        )
    }
}
