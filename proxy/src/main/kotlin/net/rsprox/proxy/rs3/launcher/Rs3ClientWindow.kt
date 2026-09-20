package net.rsprox.proxy.rs3.launcher

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference

/** Readiness only. The official launcher and client own placement, DPI handling and persistence. */
internal object Rs3ClientWindow {
    fun find(pid: Long): HWND? {
        val windows = User32.INSTANCE
        var result: HWND? = null
        val owner = IntByReference()
        val name = CharArray(64)
        windows.EnumWindows({ window, _ ->
            windows.GetWindowThreadProcessId(window, owner)
            if (Integer.toUnsignedLong(owner.value) == pid && windows.IsWindowVisible(window)) {
                val length = windows.GetClassName(window, name, name.size)
                if (String(name, 0, length) == "JagWindow") {
                    result = window
                    return@EnumWindows false
                }
            }
            true
        }, null)
        return result
    }
}
