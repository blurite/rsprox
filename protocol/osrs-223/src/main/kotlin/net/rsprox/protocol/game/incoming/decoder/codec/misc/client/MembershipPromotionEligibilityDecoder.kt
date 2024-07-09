package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.MembershipPromotionEligibility

@Consistent
public class MembershipPromotionEligibilityDecoder : MessageDecoder<MembershipPromotionEligibility> {
    override val prot: ClientProt = GameClientProt.MEMBERSHIP_PROMOTION_ELIGIBILITY

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MembershipPromotionEligibility {
        val eligibleForIntroductoryPrice = buffer.g1()
        val eligibleForTrialPurchase = buffer.g1()
        return MembershipPromotionEligibility(
            eligibleForIntroductoryPrice,
            eligibleForTrialPurchase,
        )
    }
}
