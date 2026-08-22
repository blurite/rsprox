package net.rsprox.proxy.rs3.transcriber

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3v949.game.incoming.model.buttons.If3Button
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.regular.AnyProperty

public object Rs3PropertyTreeBuilder {
    public fun build(message: IncomingMessage): RootProperty? =
        when (message) {
            is If3Button -> buildIfButton(message)
            else -> buildUnhandled(message)
        }

    private fun buildIfButton(message: If3Button): RootProperty? {

        val root =
            object : RootProperty {
                override val prot: String = "IF_BUTTON${message.op}"
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }

        val componentName = Rs3GamevalLookup.component(message.combinedId)
        root.children += AnyProperty("component", componentName, String::class.java)

        if (message.obj != -1) {
            root.children += AnyProperty("obj", Rs3GamevalLookup.obj(message.obj), String::class.java)
        }
        if (message.slot != -1) {
            root.children += AnyProperty("slot", message.slot, Int::class.java)
        }

        return root
    }

    private fun buildUnhandled(message: IncomingMessage): RootProperty {
        val root =
            object : RootProperty {
                override val prot: String = message.javaClass.simpleName
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }
        root.children += AnyProperty("value", message.toString(), String::class.java)
        return root
    }
}
