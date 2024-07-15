package net.rsprox.proxy.progressbar

public class ProgressBarNotifier(
    private val callback: (Int, String) -> Unit,
) {
    public fun update(
        percentage: Int,
        text: String,
    ) {
        callback(percentage, text)
    }
}
