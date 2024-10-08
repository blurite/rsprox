package net.rsprox.shared.filters

public enum class ProtCategory(
    public val label: String,
) {
    INTERFACES("Interfaces"),
    NPCS("Npcs"),
    LOCS("Locs"),
    OBJS("Objs"),
    PLAYERS("Players"),
    EVENTS("Events"),
    PROT_RESUME("Protected access resume"),
    FRIENDCHAT("Friend chat"),
    CLAN("Clan"),
    SOCIAL("Social"),
    MESSAGING("Messaging"),
    INFO("Info"),
    MIDI("Midi"),
    ZONES("Zones"),
    SPECIFIC("Specifics"),
    MAP("Map"),
    VARP("Varp"),
    CAMERA("Camera"),
    INVENTORIES("Inventories"),
    PLAYER_EXTENDED_INFO("Player extended info"),
    NPC_EXTENDED_INFO("Npc extended info"),
    OTHER("Other"),
    DEPRECATED("Deprecated packets"),
}
