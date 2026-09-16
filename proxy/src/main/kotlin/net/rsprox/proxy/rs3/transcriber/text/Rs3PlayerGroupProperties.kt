package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupDelta
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupFull
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupMember
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.string

internal fun Property.appendPlayerGroup(value: PlayerGroupFull.Group) {
    long("key", value.groupKey)
    string("name", value.groupName)
    int("revision", value.revision)
    int("flags", value.flags)
    int("groupWord22", value.groupWord22)
    int("groupInt98", value.groupInt98)
    long("groupLongA0", value.groupLongA0)
    group("members") {
        value.members.forEachIndexed { index, member ->
            group {
                int("index", index)
                appendPlayerGroupMember(member)
            }
        }
    }
    group("banned") { value.bannedNames.forEach { name -> group { string("name", name) } } }
    appendVariables(value.variables, Rs3VariableDomain.PLAYER_GROUP)
}

internal fun Property.appendPlayerGroupMember(member: PlayerGroupMember) {
    member.name?.let { string("name", it) }
    int("flags", member.flags)
    int("world", member.world)
    int("state", member.memberState)
    int("memberByte35", member.memberByte35)
    int("memberByte3c", member.memberByte3c)
    group("skills") {
        member.experience.forEachIndexed { index, value ->
            group {
                int("skill", index)
                int("xp", value)
            }
        }
    }
    appendVariables(member.variables, Rs3VariableDomain.PLAYER)
}

internal fun Property.appendPlayerGroupRecords(records: List<PlayerGroupDelta.Record>) {
    group("records") {
        for (record in records) {
            when (record) {
                is PlayerGroupDelta.AddMember -> group("ADD_MEMBER") { appendPlayerGroupMember(record.member) }
                is PlayerGroupDelta.RemoveMember -> group("REMOVE_MEMBER") { int("index", record.memberIndex) }
                is PlayerGroupDelta.AddBanned -> group("ADD_BANNED") { string("name", record.name) }
                is PlayerGroupDelta.RemoveBanned -> group("REMOVE_BANNED") { int("index", record.bannedIndex) }
                is PlayerGroupDelta.MemberByte35 ->
                    group("MEMBER_BYTE35") {
                        int("index", record.memberIndex)
                        int("value", record.value)
                    }
                is PlayerGroupDelta.MemberWorld ->
                    group("MEMBER_WORLD") {
                        int("index", record.memberIndex)
                        int("world", record.world)
                    }
                is PlayerGroupDelta.MemberOffline -> group("MEMBER_OFFLINE") { int("index", record.memberIndex) }
                is PlayerGroupDelta.MemberState ->
                    group("MEMBER_STATE") {
                        int("index", record.memberIndex)
                        int("value", record.value)
                    }
                PlayerGroupDelta.AllMembersState2 -> group("ALL_MEMBERS_STATE") { int("value", 2) }
                PlayerGroupDelta.AllMembersState3 -> group("ALL_MEMBERS_STATE") { int("value", 3) }
                is PlayerGroupDelta.UpdateMember ->
                    group("UPDATE_MEMBER") {
                        int("index", record.memberIndex)
                        appendPlayerGroupMember(record.member)
                    }
                is PlayerGroupDelta.Variable ->
                    group("VARIABLE") {
                        appendVariable(record.variable, Rs3VariableDomain.PLAYER_GROUP)
                    }
                PlayerGroupDelta.NoVariable -> group("NO_VARIABLE") {}
                is PlayerGroupDelta.Varbit ->
                    group("VARBIT") {
                        int("id", record.id)
                        record.value?.let { int("value", it) }
                    }
                is PlayerGroupDelta.MemberByte3c ->
                    group("MEMBER_BYTE3C") {
                        int("index", record.memberIndex)
                        int("value", record.value)
                    }
                is PlayerGroupDelta.Rejected ->
                    group("REJECTED") {
                        int("type", record.type)
                        int("sentinel", record.sentinel)
                    }
            }
        }
    }
}
