package net.rsprox.proxy.rs3.window

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.MONITORINFO
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/** Window queries only. Restoration is performed by the client before its first ShowWindow. */
internal class Rs3WindowApi {
    private val windows = Native.load("user32", DpiAwareUser32::class.java, W32APIOptions.DEFAULT_OPTIONS)

    fun <T> withDpiAwareness(action: () -> T): T {
        val previous = windows.SetThreadDpiAwarenessContext(Pointer.createConstant(-4L))
        check(previous != null) { "Unable to set window-placement thread DPI awareness" }
        try {
            return action()
        } finally {
            windows.SetThreadDpiAwarenessContext(previous)
        }
    }

    fun find(pid: Long): HWND? {
        var result: HWND? = null
        val owner = IntByReference()
        val name = CharArray(64)
        windows.EnumWindows({ window, _ ->
            windows.GetWindowThreadProcessId(window, owner)
            if (Integer.toUnsignedLong(owner.value) == pid && windows.IsWindowVisible(window)) {
                val length = windows.GetClassName(window, name, name.size)
                if (String(name, 0, length) == "JagWindow" && isWindowed(window)) {
                    result = window
                    return@EnumWindows false
                }
            }
            true
        }, null)
        return result
    }

    fun read(window: HWND): Rs3WindowPlacement? {
        if (!isWindowed(window)) return null
        val placement = WINDOWPLACEMENT()
        if (!windows.GetWindowPlacement(window, placement).booleanValue()) return null
        if (placement.showCmd != WinUser.SW_SHOWNORMAL && placement.showCmd != WinUser.SW_SHOWMAXIMIZED) return null
        val rect = placement.rcNormalPosition
        if (rect.right <= rect.left || rect.bottom <= rect.top) return null
        return Rs3WindowPlacement(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            placement.showCmd == WinUser.SW_SHOWMAXIMIZED,
        )
    }

    fun startupPlacement(saved: Rs3WindowPlacement?): ByteArray {
        val rect = RECT()
        if (saved != null) {
            rect.left = saved.left
            rect.top = saved.top
            rect.right = saved.right
            rect.bottom = saved.bottom
        }
        val monitor = windows.MonitorFromRect(rect, WinUser.MONITOR_DEFAULTTONEAREST)
        val info = MONITORINFO()
        check(windows.GetMonitorInfo(monitor, info).booleanValue()) { "Unable to query RS3 startup monitor" }
        val work = info.rcWork
        val width = (saved?.let { it.right - it.left } ?: 1040).coerceAtMost(work.right - work.left)
        val height = (saved?.let { it.bottom - it.top } ?: 807).coerceAtMost(work.bottom - work.top)
        // The store uses GetWindowPlacement workspace coordinates; the native launcher path uses MoveWindow.
        val left =
            saved?.let { it.left + work.left - info.rcMonitor.left }
                ?: (work.left + (work.right - work.left - width) / 2)
        val top =
            saved?.let { it.top + work.top - info.rcMonitor.top }
                ?: (work.top + (work.bottom - work.top - height) / 2)
        val x = left.coerceIn(work.left, work.right - width)
        val y = top.coerceIn(work.top, work.bottom - height)
        return ByteArrayOutputStream(34)
            .also { bytes ->
                DataOutputStream(bytes).use { output ->
                    output.writeByte(0)
                    output.writeByte(if (saved?.maximized == true) WinUser.SW_SHOWMAXIMIZED else WinUser.SW_SHOWNORMAL)
                    repeat(4) { output.writeInt(-1) }
                    output.writeInt(x)
                    output.writeInt(y)
                    output.writeInt(x + width)
                    output.writeInt(y + height)
                }
            }.toByteArray()
    }

    private fun isWindowed(window: HWND): Boolean =
        windows.GetWindowLong(window, WinUser.GWL_STYLE) and WinUser.WS_CAPTION == WinUser.WS_CAPTION

    internal interface DpiAwareUser32 : User32 {
        @Suppress("FunctionName")
        fun SetThreadDpiAwarenessContext(context: Pointer): Pointer?
    }
}
