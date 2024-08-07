package net.rsprox.gui.util

import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

private fun Image.resizeTo(
    width: Int,
    height: Int,
): Image {
    val tmp: Image = getScaledInstance(width, height, Image.SCALE_SMOOTH)
    val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = resized.createGraphics()
    g.drawImage(tmp, 0, 0, null)
    g.dispose()
    return resized
}

public fun ImageIcon.resizeTo(
    width: Int,
    height: Int,
): ImageIcon {
    check(image is BufferedImage)
    val resized = image.resizeTo(width, height)
    return ImageIcon(resized)
}
