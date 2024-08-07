package net.rsprox.gui.sidebar

import com.formdev.flatlaf.ui.FlatTabbedPaneUI
import net.rsprox.gui.App
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

public class SideBarUI : FlatTabbedPaneUI() {
    override fun createLayoutManager(): LayoutManager {
        return object : FlatTabbedPaneLayout() {
            override fun calculateSize(minimum: Boolean): Dimension {
                val width = preferredTabAreaWidth(RIGHT, tabPane.height)
                val height = preferredTabAreaHeight(RIGHT, tabPane.width)
                val xOff =
                    tabAreaInsets.left + tabAreaInsets.right + getContentBorderInsets(RIGHT).right +
                        getContentBorderInsets(
                            RIGHT,
                        ).left
                if (tabPane.selectedComponent == null) {
                    return Dimension(width + xOff, height)
                }
                return super.calculateSize(minimum)
            }
        }
    }

    override fun createMouseListener(): MouseListener {
        val listener = super.createMouseListener()
        return object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                listener.mouseClicked(e)
            }

            override fun mousePressed(e: MouseEvent) {
                listener.mouseEntered(e)
                if (e.button != MouseEvent.BUTTON1) return
                val tabIndex = tabForCoordinate(tabPane, e.x, e.y)
                if (tabIndex !in 0..<tabPane.tabCount) {
                    return
                }
                tabPane.selectedIndex = if (tabIndex == tabPane.selectedIndex) -1 else tabIndex
                App.service.setFiltersStatus(tabPane.selectedIndex)
            }

            override fun mouseEntered(e: MouseEvent?) {
                listener.mouseEntered(e)
            }

            override fun mouseExited(e: MouseEvent?) {
                listener.mouseExited(e)
            }
        }
    }
}
