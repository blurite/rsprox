package net.rsprox.gui.sessions

import net.rsprox.gui.AppIcons
import net.rsprox.gui.util.resizeTo
import javax.swing.Icon

public enum class SessionType(
    public val displayName: String,
) {
    Java("Java"),
    Native("Old School (Native)"),
    RuneLite("Old School (RuneLite)"),
    RS3("RuneScape 3 (OpenGL)"),
    RS3_VULKAN("RuneScape 3 (Vulkan)"),
    ;

    public val isRs3: Boolean
        get() = this == RS3 || this == RS3_VULKAN

    public val icon: Icon by lazy {
        when (this) {
            Java -> AppIcons.Java.resizeTo(16, 16)
            Native -> AppIcons.Native.resizeTo(16, 16)
            RuneLite -> AppIcons.RuneLite.resizeTo(16, 16)
            RS3, RS3_VULKAN -> AppIcons.RuneScape3.resizeTo(16, 16)
        }
    }
}
