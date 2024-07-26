package net.rsprox.gui

import com.formdev.flatlaf.extras.FlatSVGIcon
import javax.imageio.ImageIO
import javax.swing.ImageIcon

public object AppIcons {

    public val Add: FlatSVGIcon = loadSvgIcon("add")
    public val Filter: FlatSVGIcon = loadSvgIcon("filter")
    public val Java: ImageIcon = loadIcon("java")
    public val Native: ImageIcon = loadIcon("native")
    public val Push: FlatSVGIcon = loadSvgIcon("push")
    public val RuneLite: ImageIcon = loadIcon("runelite")
    public val Settings: FlatSVGIcon = loadSvgIcon("settings")
    public val Update: FlatSVGIcon = loadSvgIcon("update")
    public val User: FlatSVGIcon = loadSvgIcon("user")
    public val Web: FlatSVGIcon = loadSvgIcon("web")

    private fun loadIcon(name: String): ImageIcon {
        val image = ImageIO.read(javaClass.getResource("/icons/$name.png"))
        return ImageIcon(image)
    }

    private fun loadSvgIcon(name: String): FlatSVGIcon {
        return FlatSVGIcon(javaClass.getResource("/icons/$name.svg"))
    }
}
