package net.rsprox.protocol.game.outgoing.decoder.prot

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprot.protocol.ServerProt

public enum class GameServerProt(
    override val opcode: Int,
    override val size: Int,
) : ServerProt,
    ClientProt {
    // Interface related packets
    IF_RESYNC(GameServerProtId.IF_RESYNC, Prot.VAR_SHORT),
    IF_OPENTOP(GameServerProtId.IF_OPENTOP, 2),
    IF_OPENSUB(GameServerProtId.IF_OPENSUB, 7),
    IF_CLOSESUB(GameServerProtId.IF_CLOSESUB, 4),
    IF_MOVESUB(GameServerProtId.IF_MOVESUB, 8),
    IF_CLEARINV(GameServerProtId.IF_CLEARINV, 4),
    IF_SETEVENTS(GameServerProtId.IF_SETEVENTS, 12),
    IF_SETPOSITION(GameServerProtId.IF_SETPOSITION, 8),
    IF_SETSCROLLPOS(GameServerProtId.IF_SETSCROLLPOS, 6),
    IF_SETROTATESPEED(GameServerProtId.IF_SETROTATESPEED, 8),
    IF_SETTEXT(GameServerProtId.IF_SETTEXT, Prot.VAR_SHORT),
    IF_SETHIDE(GameServerProtId.IF_SETHIDE, 5),
    IF_SETANGLE(GameServerProtId.IF_SETANGLE, 10),
    IF_SETOBJECT(GameServerProtId.IF_SETOBJECT, 10),
    IF_SETCOLOUR(GameServerProtId.IF_SETCOLOUR, 6),
    IF_SETANIM(GameServerProtId.IF_SETANIM, 6),
    IF_SETNPCHEAD(GameServerProtId.IF_SETNPCHEAD, 6),
    IF_SETNPCHEAD_ACTIVE(GameServerProtId.IF_SETNPCHEAD_ACTIVE, 6),
    IF_SETPLAYERHEAD(GameServerProtId.IF_SETPLAYERHEAD, 4),
    IF_SETMODEL(GameServerProtId.IF_SETMODEL, 6),
    IF_SETPLAYERMODEL_BASECOLOUR(GameServerProtId.IF_SETPLAYERMODEL_BASECOLOUR, 6),
    IF_SETPLAYERMODEL_BODYTYPE(GameServerProtId.IF_SETPLAYERMODEL_BODYTYPE, 5),
    IF_SETPLAYERMODEL_OBJ(GameServerProtId.IF_SETPLAYERMODEL_OBJ, 8),
    IF_SETPLAYERMODEL_SELF(GameServerProtId.IF_SETPLAYERMODEL_SELF, 5),

    // Music-system related packets (excl. zone ones)
    MIDI_SONG_V2(GameServerProtId.MIDI_SONG_V2, 10),
    MIDI_SONG_WITHSECONDARY(GameServerProtId.MIDI_SONG_WITHSECONDARY, 12),
    MIDI_SWAP(GameServerProtId.MIDI_SWAP, 8),
    MIDI_SONG_STOP(GameServerProtId.MIDI_SONG_STOP, 4),
    MIDI_SONG_V1(GameServerProtId.MIDI_SONG_V1, 2),
    MIDI_JINGLE(GameServerProtId.MIDI_JINGLE, 5),
    SYNTH_SOUND(GameServerProtId.SYNTH_SOUND, 5),

    // Zone header packets
    UPDATE_ZONE_FULL_FOLLOWS(GameServerProtId.UPDATE_ZONE_FULL_FOLLOWS, 3),
    UPDATE_ZONE_PARTIAL_FOLLOWS(GameServerProtId.UPDATE_ZONE_PARTIAL_FOLLOWS, 3),
    UPDATE_ZONE_PARTIAL_ENCLOSED(GameServerProtId.UPDATE_ZONE_PARTIAL_ENCLOSED, Prot.VAR_SHORT),

    // Zone payload packets
    LOC_ADD_CHANGE(GameServerProtId.LOC_ADD_CHANGE, 5),
    LOC_DEL(GameServerProtId.LOC_DEL, 2),
    LOC_ANIM(GameServerProtId.LOC_ANIM, 4),
    LOC_MERGE(GameServerProtId.LOC_MERGE, 14),
    OBJ_ADD(GameServerProtId.OBJ_ADD, 14),
    OBJ_DEL(GameServerProtId.OBJ_DEL, 7),
    OBJ_COUNT(GameServerProtId.OBJ_COUNT, 11),
    OBJ_ENABLED_OPS(GameServerProtId.OBJ_ENABLED_OPS, 4),
    OBJ_CUSTOMISE(GameServerProtId.OBJ_CUSTOMISE, 17),
    OBJ_UNCUSTOMISE(GameServerProtId.OBJ_UNCUSTOMISE, 7),
    MAP_ANIM(GameServerProtId.MAP_ANIM, 6),
    MAP_PROJANIM(GameServerProtId.MAP_PROJANIM, 20),
    SOUND_AREA(GameServerProtId.SOUND_AREA, 7),

    // Specific packets
    PROJANIM_SPECIFIC_V3(GameServerProtId.PROJANIM_SPECIFIC_V3, 22),

    @Deprecated(
        "Deprecated as a new variant that supports source index was introduced.",
        replaceWith = ReplaceWith("PROJANIM_SPECIFIC_V3"),
    )
    PROJANIM_SPECIFIC_V2(GameServerProtId.PROJANIM_SPECIFIC_V2, 19),

    @Deprecated(
        "Deprecated as it is bugged(size: 17; payload: 18) and " +
            "a newer variant with greater property ranges is introduced",
        replaceWith = ReplaceWith("PROJANIM_SPECIFIC_V2"),
    )
    PROJANIM_SPECIFIC_V1(GameServerProtId.PROJANIM_SPECIFIC_V1, 17),
    MAP_ANIM_SPECIFIC(GameServerProtId.MAP_ANIM_SPECIFIC, 8),
    LOC_ANIM_SPECIFIC(GameServerProtId.LOC_ANIM_SPECIFIC, 6),
    NPC_HEADICON_SPECIFIC(GameServerProtId.NPC_HEADICON_SPECIFIC, 9),
    NPC_SPOTANIM_SPECIFIC(GameServerProtId.NPC_SPOTANIM_SPECIFIC, 9),
    NPC_ANIM_SPECIFIC(GameServerProtId.NPC_ANIM_SPECIFIC, 5),
    PLAYER_ANIM_SPECIFIC(GameServerProtId.PLAYER_ANIM_SPECIFIC, 3),
    PLAYER_SPOTANIM_SPECIFIC(GameServerProtId.PLAYER_SPOTANIM_SPECIFIC, 9),

    // Info packets
    PLAYER_INFO(GameServerProtId.PLAYER_INFO, Prot.VAR_SHORT),
    NPC_INFO_SMALL_V5(GameServerProtId.NPC_INFO_SMALL_V5, Prot.VAR_SHORT),
    NPC_INFO_LARGE_V5(GameServerProtId.NPC_INFO_LARGE_V5, Prot.VAR_SHORT),

    @Deprecated(
        "Deprecated as a new variant was introduced.",
        replaceWith = ReplaceWith("NPC_INFO_SMALL_V5"),
    )
    NPC_INFO_SMALL_V4(GameServerProtId.NPC_INFO_SMALL_V4, Prot.VAR_SHORT),

    @Deprecated(
        "Deprecated as a new variant was introduced.",
        replaceWith = ReplaceWith("NPC_INFO_LARGE_V5"),
    )
    NPC_INFO_LARGE_V4(GameServerProtId.NPC_INFO_LARGE_V4, Prot.VAR_SHORT),
    SET_NPC_UPDATE_ORIGIN(GameServerProtId.SET_NPC_UPDATE_ORIGIN, 2),

    // World entity packets
    CLEAR_ENTITIES(GameServerProtId.CLEAR_ENTITIES, 0),
    SET_ACTIVE_WORLD(GameServerProtId.SET_ACTIVE_WORLD, 4),
    WORLDENTITY_INFO_V3(GameServerProtId.WORLDENTITY_INFO_V3, Prot.VAR_SHORT),

    @Deprecated(
        "Deprecated as a new variant that supports fine coord was introduced.",
        replaceWith = ReplaceWith("WORLDENTITY_INFO_V3"),
    )
    WORLDENTITY_INFO_V2(GameServerProtId.WORLDENTITY_INFO_V2, Prot.VAR_SHORT),

    @Deprecated(
        "Deprecated as a new variant that supports fine height was introduced.",
        replaceWith = ReplaceWith("WORLDENTITY_INFO_V2"),
    )
    WORLDENTITY_INFO_V1(GameServerProtId.WORLDENTITY_INFO_V1, Prot.VAR_SHORT),

    // Map packets
    REBUILD_NORMAL(GameServerProtId.REBUILD_NORMAL, Prot.VAR_SHORT),
    REBUILD_REGION(GameServerProtId.REBUILD_REGION, Prot.VAR_SHORT),
    REBUILD_WORLDENTITY(GameServerProtId.REBUILD_WORLDENTITY, Prot.VAR_SHORT),

    // Varp packets
    VARP_SMALL(GameServerProtId.VARP_SMALL, 3),
    VARP_LARGE(GameServerProtId.VARP_LARGE, 6),
    VARP_RESET(GameServerProtId.VARP_RESET, 0),
    VARP_SYNC(GameServerProtId.VARP_SYNC, 0),

    // Camera packets
    CAM_SHAKE(GameServerProtId.CAM_SHAKE, 4),
    CAM_RESET(GameServerProtId.CAM_RESET, 0),
    CAM_SMOOTHRESET(GameServerProtId.CAM_SMOOTHRESET, 4),
    CAM_MOVETO(GameServerProtId.CAM_MOVETO, 6),
    CAM_MOVETO_CYCLES(GameServerProtId.CAM_MOVETO_CYCLES, 8),
    CAM_MOVETO_ARC(GameServerProtId.CAM_MOVETO_ARC, 10),
    CAM_LOOKAT(GameServerProtId.CAM_LOOKAT, 6),
    CAM_LOOKAT_EASED_COORD(GameServerProtId.CAM_LOOKAT_EASED_COORD, 7),
    CAM_ROTATEBY(GameServerProtId.CAM_ROTATEBY, 7),
    CAM_ROTATETO(GameServerProtId.CAM_ROTATETO, 7),
    CAM_MODE(GameServerProtId.CAM_MODE, 1),
    CAM_TARGET_V2(GameServerProtId.CAM_TARGET_V2, 5),
    CAM_TARGET_V1(GameServerProtId.CAM_TARGET_V1, 3),
    OCULUS_SYNC(GameServerProtId.OCULUS_SYNC, 4),

    // Inventory packets
    UPDATE_INV_FULL(GameServerProtId.UPDATE_INV_FULL, Prot.VAR_SHORT),
    UPDATE_INV_PARTIAL(GameServerProtId.UPDATE_INV_PARTIAL, Prot.VAR_SHORT),
    UPDATE_INV_STOPTRANSMIT(GameServerProtId.UPDATE_INV_STOPTRANSMIT, 2),

    // Social packets
    MESSAGE_PRIVATE(GameServerProtId.MESSAGE_PRIVATE, Prot.VAR_SHORT),
    MESSAGE_PRIVATE_ECHO(GameServerProtId.MESSAGE_PRIVATE_ECHO, Prot.VAR_SHORT),
    FRIENDLIST_LOADED(GameServerProtId.FRIENDLIST_LOADED, 0),
    UPDATE_FRIENDLIST(GameServerProtId.UPDATE_FRIENDLIST, Prot.VAR_SHORT),
    UPDATE_IGNORELIST(GameServerProtId.UPDATE_IGNORELIST, Prot.VAR_SHORT),

    // Friend chat (old "clans") packets
    UPDATE_FRIENDCHAT_CHANNEL_FULL_V1(GameServerProtId.UPDATE_FRIENDCHAT_CHANNEL_FULL_V1, Prot.VAR_SHORT),
    UPDATE_FRIENDCHAT_CHANNEL_FULL_V2(GameServerProtId.UPDATE_FRIENDCHAT_CHANNEL_FULL_V2, Prot.VAR_SHORT),
    UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER(GameServerProtId.UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER, Prot.VAR_BYTE),
    MESSAGE_FRIENDCHANNEL(GameServerProtId.MESSAGE_FRIENDCHANNEL, Prot.VAR_BYTE),

    // Clan chat packets
    VARCLAN(GameServerProtId.VARCLAN, Prot.VAR_BYTE),
    VARCLAN_ENABLE(GameServerProtId.VARCLAN_ENABLE, 0),
    VARCLAN_DISABLE(GameServerProtId.VARCLAN_DISABLE, 0),
    CLANCHANNEL_FULL(GameServerProtId.CLANCHANNEL_FULL, Prot.VAR_SHORT),
    CLANCHANNEL_DELTA(GameServerProtId.CLANCHANNEL_DELTA, Prot.VAR_SHORT),
    CLANSETTINGS_FULL(GameServerProtId.CLANSETTINGS_FULL, Prot.VAR_SHORT),
    CLANSETTINGS_DELTA(GameServerProtId.CLANSETTINGS_DELTA, Prot.VAR_SHORT),
    MESSAGE_CLANCHANNEL(GameServerProtId.MESSAGE_CLANCHANNEL, Prot.VAR_BYTE),
    MESSAGE_CLANCHANNEL_SYSTEM(GameServerProtId.MESSAGE_CLANCHANNEL_SYSTEM, Prot.VAR_BYTE),

    // Log out packets
    LOGOUT(GameServerProtId.LOGOUT, 0),
    LOGOUT_WITHREASON(GameServerProtId.LOGOUT_WITHREASON, 1),
    LOGOUT_TRANSFER(GameServerProtId.LOGOUT_TRANSFER, Prot.VAR_BYTE),

    // Misc. player state packets
    UPDATE_RUNWEIGHT(GameServerProtId.UPDATE_RUNWEIGHT, 2),
    UPDATE_RUNENERGY(GameServerProtId.UPDATE_RUNENERGY, 2),
    SET_MAP_FLAG(GameServerProtId.SET_MAP_FLAG, 2),
    SET_PLAYER_OP(GameServerProtId.SET_PLAYER_OP, Prot.VAR_BYTE),
    UPDATE_STAT_V2(GameServerProtId.UPDATE_STAT_V2, 7),
    UPDATE_STAT_V1(GameServerProtId.UPDATE_STAT_V1, 6),

    // Misc. player packets
    RUNCLIENTSCRIPT(GameServerProtId.RUNCLIENTSCRIPT, Prot.VAR_SHORT),
    TRIGGER_ONDIALOGABORT(GameServerProtId.TRIGGER_ONDIALOGABORT, 0),
    MESSAGE_GAME(GameServerProtId.MESSAGE_GAME, Prot.VAR_BYTE),
    CHAT_FILTER_SETTINGS(GameServerProtId.CHAT_FILTER_SETTINGS, 2),
    CHAT_FILTER_SETTINGS_PRIVATECHAT(GameServerProtId.CHAT_FILTER_SETTINGS_PRIVATECHAT, 1),
    UPDATE_TRADINGPOST(GameServerProtId.UPDATE_TRADINGPOST, Prot.VAR_SHORT),
    UPDATE_STOCKMARKET_SLOT(GameServerProtId.UPDATE_STOCKMARKET_SLOT, 20),

    // Misc. client state packets
    HINT_ARROW(GameServerProtId.HINT_ARROW, 6),
    RESET_ANIMS(GameServerProtId.RESET_ANIMS, 0),
    UPDATE_REBOOT_TIMER(GameServerProtId.UPDATE_REBOOT_TIMER, 2),
    SET_HEATMAP_ENABLED(GameServerProtId.SET_HEATMAP_ENABLED, 1),
    MINIMAP_TOGGLE(GameServerProtId.MINIMAP_TOGGLE, 1),
    SERVER_TICK_END(GameServerProtId.SERVER_TICK_END, 0),
    HIDENPCOPS(GameServerProtId.HIDENPCOPS, 1),
    HIDEOBJOPS(GameServerProtId.HIDEOBJOPS, 1),
    HIDELOCOPS(GameServerProtId.HIDELOCOPS, 1),
    SET_INTERACTION_MODE(GameServerProtId.SET_INTERACTION_MODE, 4),
    RESET_INTERACTION_MODE(GameServerProtId.RESET_INTERACTION_MODE, 2),

    // Misc. client packets
    URL_OPEN(GameServerProtId.URL_OPEN, Prot.VAR_SHORT),
    SITE_SETTINGS(GameServerProtId.SITE_SETTINGS, Prot.VAR_BYTE),
    UPDATE_UID192(GameServerProtId.UPDATE_UID192, 28),
    REFLECTION_CHECKER(GameServerProtId.REFLECTION_CHECKER, Prot.VAR_SHORT),
    SEND_PING(GameServerProtId.SEND_PING, 8),
    HISCORE_REPLY(GameServerProtId.HISCORE_REPLY, Prot.VAR_SHORT),

    // Unknown packets
    UNKNOWN_STRING(GameServerProtId.UNKNOWN_STRING, Prot.VAR_BYTE),

    RECONNECT(0xFF, Prot.VAR_SHORT),
}
