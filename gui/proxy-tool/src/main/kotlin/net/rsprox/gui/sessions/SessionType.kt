package net.rsprox.gui.sessions

import net.rsprox.gui.AppIcons
import net.rsprox.gui.util.resizeTo
import javax.swing.Icon

public enum class SessionType {
    Java,
    Native,
    RuneLite,
    ;

    public val icon: Icon by lazy {
        when (this) {
            Java -> AppIcons.Java.resizeTo(16, 16)
            Native -> AppIcons.Native.resizeTo(16, 16)
            RuneLite -> AppIcons.RuneLite.resizeTo(16, 16)
        }
    }
}
