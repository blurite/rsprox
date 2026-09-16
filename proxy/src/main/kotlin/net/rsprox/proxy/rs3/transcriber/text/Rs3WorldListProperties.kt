package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.string

internal fun Property.appendWorldList(reply: WorldlistFetchReply.Reply) {
    int("version", reply.version)
    int("definitionsflag", reply.definitionsFlag)
    val definitions = reply.definitions
    if (definitions != null) {
        int("minimumworldid", definitions.minimumWorldId)
        int("maximumworldid", definitions.maximumWorldId)
        int("checksum", definitions.checksum)
        group("countries") {
            for ((index, country) in definitions.countries.withIndex()) {
                group("country") {
                    int("index", index)
                    int("id", country.id)
                    markedString("name", country.name)
                }
            }
        }
    }
    val worlds = definitions?.worlds?.associateBy { it.index }.orEmpty()
    group("worlds") {
        for (population in reply.populations) {
            group("world") {
                if (definitions != null) int("id", definitions.minimumWorldId + population.index)
                int("index", population.index)
                int("population", population.population)
                worlds[population.index]?.let { worldDetails(it) }
            }
        }
        // Retain definitions even if a future reply omits a population entry.
        val populated = reply.populations.mapTo(mutableSetOf()) { it.index }
        for (world in definitions?.worlds.orEmpty()) {
            if (world.index in populated) continue
            group("world") {
                int("id", checkNotNull(definitions).minimumWorldId + world.index)
                int("index", world.index)
                worldDetails(world)
            }
        }
    }
}

private fun Property.worldDetails(world: WorldlistFetchReply.World) {
    int("countryindex", world.countryIndex)
    int("properties", world.properties)
    int("extraid", world.extraId)
    world.extraName?.let { markedString("extraname", it) }
    markedString("activity", world.activity)
    markedString("hostname", world.hostname)
}

private fun Property.markedString(
    name: String,
    value: WorldlistFetchReply.MarkedString,
) {
    if (value.marker == 0) {
        string(name, value.text)
    } else {
        any<String>(name, null)
        int("${name}marker", value.marker)
    }
}
