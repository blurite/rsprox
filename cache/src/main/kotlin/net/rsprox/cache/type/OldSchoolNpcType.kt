package net.rsprox.cache.type

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.type.NpcType

@Suppress("DuplicatedCode")
public class OldSchoolNpcType(
    override val id: Int,
) : NpcType {
    override var name: String = "null"
    override var size: Int = 1
    override var category: Int = -1
    override var model: List<Int> = emptyList()
    override var headmodel: List<Int> = emptyList()
    override var readyanim: Int = -1
    override var turnleftanim: Int = -1
    override var turnrightanim: Int = -1
    override var walkanim: Int = -1
    override var walkanimback: Int = -1
    override var walkanimleft: Int = -1
    override var walkanimright: Int = -1
    override var runanim: Int = -1
    override var runanimback: Int = -1
    override var runanimleft: Int = -1
    override var runanimright: Int = -1
    override var crawlanim: Int = -1
    override var crawlanimback: Int = -1
    override var crawlanimleft: Int = -1
    override var crawlanimright: Int = -1
    override var recolsource: List<Int> = emptyList()
    override var recoldest: List<Int> = emptyList()
    override var retexsource: List<Int> = emptyList()
    override var retexdest: List<Int> = emptyList()
    override var op: MutableList<String?> = mutableListOf(null, null, null, null, null)
    override var minimap: Boolean = true
    override var vislevel: Int = -1
    override var resizeh: Int = 128
    override var resizev: Int = 128
    override var renderPriority: Int = 0
    override var ambient: Int = 0
    override var contrast: Int = 0
    override var turnspeed: Int = 32
    override var multinpc: List<Int> = emptyList()
    override var multivarbit: Int = -1
    override var multivar: Int = -1
    override var active: Boolean = true
    override var walksmoothing: Boolean = true
    override var follower: Boolean = false
    override var lowpriorityops: Boolean = false
    override var headicongroups: List<Int> = emptyList()
    override var headiconindices: List<Int> = emptyList()
    override var overlayheight: Int = -1
    override var stat: MutableList<Int> = mutableListOf(1, 1, 1, 1, 1, 1)
    override var params: MutableMap<Int, Any> = mutableMapOf()
    override var footprintSize: Int = -1
    override var worldOverlapTint: Boolean = false
    override var worldOverlapTintColour: Int = 39188

    public fun decode(
        revision: Int,
        buffer: JagByteBuf,
    ) {
        while (true) {
            val opcode = buffer.g1()
            if (opcode == 0) {
                break
            }
            decode(revision, opcode, buffer)
        }
        postDecode()
    }

    private fun postDecode() {
        if (this.footprintSize == -1) {
            this.footprintSize = (0.4f * (this.size * 128).toFloat()).toInt()
        }
    }

    private fun decode(
        @Suppress("UNUSED_PARAMETER") revision: Int,
        opcode: Int,
        buffer: JagByteBuf,
    ) {
        when (opcode) {
            1 -> {
                val count = buffer.g1()
                this.model =
                    buildList(count) {
                        for (i in 0..<count) {
                            add(buffer.g2())
                        }
                    }
            }
            2 -> this.name = buffer.gjstr()
            12 -> this.size = buffer.g1()
            13 -> this.readyanim = buffer.g2()
            14 -> this.walkanim = buffer.g2()
            15 -> this.turnleftanim = buffer.g2()
            16 -> this.turnrightanim = buffer.g2()
            17 -> {
                this.walkanim = buffer.g2()
                this.walkanimback = buffer.g2()
                this.walkanimleft = buffer.g2()
                this.walkanimright = buffer.g2()
            }
            18 -> this.category = buffer.g2()
            30 -> this.op[0] = buffer.gjstr()
            31 -> this.op[1] = buffer.gjstr()
            32 -> this.op[2] = buffer.gjstr()
            33 -> this.op[3] = buffer.gjstr()
            34 -> this.op[4] = buffer.gjstr()
            40 -> {
                val count = buffer.g1()
                val source = IntArray(count)
                val dest = IntArray(count)
                for (i in 0..<count) {
                    source[i] = buffer.g2()
                    dest[i] = buffer.g2()
                }
                this.recolsource = source.toList()
                this.recoldest = dest.toList()
            }
            41 -> {
                val count = buffer.g1()
                val source = IntArray(count)
                val dest = IntArray(count)
                for (i in 0..<count) {
                    source[i] = buffer.g2()
                    dest[i] = buffer.g2()
                }
                this.retexsource = source.toList()
                this.retexdest = dest.toList()
            }
            60 -> {
                val count = buffer.g1()
                this.headmodel =
                    buildList(count) {
                        for (i in 0..<count) {
                            add(buffer.g2())
                        }
                    }
            }
            74 -> this.stat[0] = buffer.g2()
            75 -> this.stat[1] = buffer.g2()
            76 -> this.stat[2] = buffer.g2()
            77 -> this.stat[3] = buffer.g2()
            78 -> this.stat[4] = buffer.g2()
            79 -> this.stat[5] = buffer.g2()
            93 -> this.minimap = false
            95 -> this.vislevel = buffer.g2()
            97 -> this.resizeh = buffer.g2()
            98 -> this.resizev = buffer.g2()
            99 -> this.renderPriority = 1
            100 -> this.ambient = buffer.g1s()
            101 -> this.contrast = buffer.g1s() * 5
            102 -> {
                val flags = buffer.g1()
                var count = 0
                var temp = flags
                while (temp != 0) {
                    temp = temp shr 1
                    count++
                }
                val groups = IntArray(count)
                val files = IntArray(count)
                for (i in 0 until count) {
                    if ((flags and (1 shl i)) == 0) {
                        groups[i] = -1
                        files[i] = -1
                    } else {
                        groups[i] = buffer.gSmart2or4null()
                        files[i] = buffer.gSmart1or2null()
                    }
                }
                this.headicongroups = groups.toList()
                this.headiconindices = files.toList()
            }
            103 -> this.turnspeed = buffer.g2()
            106 -> decodeMulti(buffer, false)
            107 -> this.active = false
            109 -> this.walksmoothing = false
            111 -> this.renderPriority = 2
            114 -> this.runanim = buffer.g2()
            115 -> {
                this.runanim = buffer.g2()
                this.runanimback = buffer.g2()
                this.runanimleft = buffer.g2()
                this.runanimright = buffer.g2()
            }
            116 -> this.crawlanim = buffer.g2()
            117 -> {
                this.crawlanim = buffer.g2()
                this.crawlanimback = buffer.g2()
                this.crawlanimleft = buffer.g2()
                this.crawlanimright = buffer.g2()
            }
            118 -> decodeMulti(buffer, true)
            122 -> this.follower = true
            123 -> this.lowpriorityops = true
            124 -> this.overlayheight = buffer.g2()
            126 -> this.footprintSize = buffer.g2()
            145 -> this.worldOverlapTint = true
            146 -> this.worldOverlapTintColour = buffer.g2()
            249 -> ParamTypeHelper.unpackClientParams(buffer, params)
            else -> error("Invalid opcode: $opcode")
        }
    }

    private fun decodeMulti(
        buffer: JagByteBuf,
        hasDefault: Boolean,
    ) {
        this.multivarbit = buffer.g2()
        this.multivar = buffer.g2()
        var default = -1
        if (hasDefault) {
            default = buffer.g2()
            if (default == 0xFFFF) {
                default = -1
            }
        }
        val count = buffer.g1()
        val multinpcs = IntArray(count + 2)
        for (i in 0..count) {
            multinpcs[i] = buffer.g2()
            if (multinpcs[i] == 0xFFFF) {
                multinpcs[i] = -1
            }
        }
        multinpcs[count + 1] = default
        this.multinpc = multinpcs.toList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OldSchoolNpcType

        if (id != other.id) return false
        if (name != other.name) return false
        if (size != other.size) return false
        if (category != other.category) return false
        if (model != other.model) return false
        if (headmodel != other.headmodel) return false
        if (readyanim != other.readyanim) return false
        if (turnleftanim != other.turnleftanim) return false
        if (turnrightanim != other.turnrightanim) return false
        if (walkanim != other.walkanim) return false
        if (walkanimback != other.walkanimback) return false
        if (walkanimleft != other.walkanimleft) return false
        if (walkanimright != other.walkanimright) return false
        if (runanim != other.runanim) return false
        if (runanimback != other.runanimback) return false
        if (runanimleft != other.runanimleft) return false
        if (runanimright != other.runanimright) return false
        if (crawlanim != other.crawlanim) return false
        if (crawlanimback != other.crawlanimback) return false
        if (crawlanimleft != other.crawlanimleft) return false
        if (crawlanimright != other.crawlanimright) return false
        if (recolsource != other.recolsource) return false
        if (recoldest != other.recoldest) return false
        if (retexsource != other.retexsource) return false
        if (retexdest != other.retexdest) return false
        if (op != other.op) return false
        if (minimap != other.minimap) return false
        if (vislevel != other.vislevel) return false
        if (resizeh != other.resizeh) return false
        if (resizev != other.resizev) return false
        if (renderPriority != other.renderPriority) return false
        if (ambient != other.ambient) return false
        if (contrast != other.contrast) return false
        if (turnspeed != other.turnspeed) return false
        if (multinpc != other.multinpc) return false
        if (multivarbit != other.multivarbit) return false
        if (multivar != other.multivar) return false
        if (active != other.active) return false
        if (walksmoothing != other.walksmoothing) return false
        if (follower != other.follower) return false
        if (lowpriorityops != other.lowpriorityops) return false
        if (headicongroups != other.headicongroups) return false
        if (headiconindices != other.headiconindices) return false
        if (overlayheight != other.overlayheight) return false
        if (stat != other.stat) return false
        if (params != other.params) return false
        if (footprintSize != other.footprintSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + size
        result = 31 * result + category
        result = 31 * result + model.hashCode()
        result = 31 * result + headmodel.hashCode()
        result = 31 * result + readyanim
        result = 31 * result + turnleftanim
        result = 31 * result + turnrightanim
        result = 31 * result + walkanim
        result = 31 * result + walkanimback
        result = 31 * result + walkanimleft
        result = 31 * result + walkanimright
        result = 31 * result + runanim
        result = 31 * result + runanimback
        result = 31 * result + runanimleft
        result = 31 * result + runanimright
        result = 31 * result + crawlanim
        result = 31 * result + crawlanimback
        result = 31 * result + crawlanimleft
        result = 31 * result + crawlanimright
        result = 31 * result + recolsource.hashCode()
        result = 31 * result + recoldest.hashCode()
        result = 31 * result + retexsource.hashCode()
        result = 31 * result + retexdest.hashCode()
        result = 31 * result + op.hashCode()
        result = 31 * result + minimap.hashCode()
        result = 31 * result + vislevel
        result = 31 * result + resizeh
        result = 31 * result + resizev
        result = 31 * result + renderPriority
        result = 31 * result + ambient
        result = 31 * result + contrast
        result = 31 * result + turnspeed
        result = 31 * result + multinpc.hashCode()
        result = 31 * result + multivarbit
        result = 31 * result + multivar
        result = 31 * result + active.hashCode()
        result = 31 * result + walksmoothing.hashCode()
        result = 31 * result + follower.hashCode()
        result = 31 * result + lowpriorityops.hashCode()
        result = 31 * result + headicongroups.hashCode()
        result = 31 * result + headiconindices.hashCode()
        result = 31 * result + overlayheight
        result = 31 * result + stat.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + footprintSize
        return result
    }

    override fun toString(): String {
        return "OldSchoolNpcType(" +
            "id=$id, " +
            "name='$name', " +
            "size=$size, " +
            "category=$category, " +
            "model=$model, " +
            "headmodel=$headmodel, " +
            "readyanim=$readyanim, " +
            "turnleftanim=$turnleftanim, " +
            "turnrightanim=$turnrightanim, " +
            "walkanim=$walkanim, " +
            "walkanimback=$walkanimback, " +
            "walkanimleft=$walkanimleft, " +
            "walkanimright=$walkanimright, " +
            "runanim=$runanim, " +
            "runanimback=$runanimback, " +
            "runanimleft=$runanimleft, " +
            "runanimright=$runanimright, " +
            "crawlanim=$crawlanim, " +
            "crawlanimback=$crawlanimback, " +
            "crawlanimleft=$crawlanimleft, " +
            "crawlanimright=$crawlanimright, " +
            "recolsource=$recolsource, " +
            "recoldest=$recoldest, " +
            "retexsource=$retexsource, " +
            "retexdest=$retexdest, " +
            "op=$op, " +
            "minimap=$minimap, " +
            "vislevel=$vislevel, " +
            "resizeh=$resizeh, " +
            "resizev=$resizev, " +
            "renderPriority=$renderPriority, " +
            "ambient=$ambient, " +
            "contrast=$contrast, " +
            "turnspeed=$turnspeed, " +
            "multinpc=$multinpc, " +
            "multivarbit=$multivarbit, " +
            "multivar=$multivar, " +
            "active=$active, " +
            "walksmoothing=$walksmoothing, " +
            "follower=$follower, " +
            "lowpriorityops=$lowpriorityops, " +
            "headicongroups=$headicongroups, " +
            "headiconindices=$headiconindices, " +
            "overlayheight=$overlayheight, " +
            "stat=$stat, " +
            "params=$params, " +
            "footprintSize=$footprintSize" +
            ")"
    }

    public companion object {
        public fun get(
            revision: Int,
            id: Int,
            buffer: JagByteBuf,
        ): NpcType {
            val type = OldSchoolNpcType(id)
            type.decode(revision, buffer)
            return type
        }
    }
}
