package net.rsprox.proxy.util

public fun interface ProgressCallback {
    public fun update(
        percentage: Double,
        actionText: String?,
        subActionText: String,
        progressText: String?,
    )

    public fun update(
        percentage: Double,
        actionText: String?,
        subActionText: String,
    ) {
        update(percentage, actionText, subActionText, null)
    }
}
