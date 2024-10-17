package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * An enhanced-client-only packet to inform the server of the status of
 * membership eligibility.
 * @property eligibleForIntroductoryPrice whether the player is eligible for
 * an introductory price, kept in an integer form in case there are more values
 * than just yes/no.
 * @property eligibleForTrialPurchase whether the player is eligible
 * for a trial purchase, kept int an integer form in case there are more values
 * than just yes/no.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class MembershipPromotionEligibility private constructor(
    private val _eligibleForIntroductoryPrice: UByte,
    private val _eligibleForTrialPurchase: UByte,
) : IncomingGameMessage {
    public constructor(
        eligibleForIntroductoryPrice: Int,
        eligibleForTrialPurchase: Int,
    ) : this(
        eligibleForIntroductoryPrice.toUByte(),
        eligibleForTrialPurchase.toUByte(),
    )

    public val eligibleForIntroductoryPrice: Int
        get() = _eligibleForIntroductoryPrice.toInt()
    public val eligibleForTrialPurchase: Int
        get() = _eligibleForTrialPurchase.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MembershipPromotionEligibility

        if (_eligibleForIntroductoryPrice != other._eligibleForIntroductoryPrice) return false
        if (_eligibleForTrialPurchase != other._eligibleForTrialPurchase) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _eligibleForIntroductoryPrice.hashCode()
        result = 31 * result + _eligibleForTrialPurchase.hashCode()
        return result
    }

    override fun toString(): String =
        "MembershipPromotionEligibility(" +
            "eligibleForIntroductoryPrice=$eligibleForIntroductoryPrice, " +
            "eligibleForTrialPurchase=$eligibleForTrialPurchase" +
            ")"
}
