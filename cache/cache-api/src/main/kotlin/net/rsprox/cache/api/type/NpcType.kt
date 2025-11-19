package net.rsprox.cache.api.type

@Suppress("SpellCheckingInspection")
public interface NpcType {
    public val id: Int
    public val name: String
    public val size: Int
    public val category: Int
    public val model: List<Int>
    public val headmodel: List<Int>
    public val readyanim: Int
    public val turnleftanim: Int
    public val turnrightanim: Int
    public val walkanim: Int
    public val walkanimback: Int
    public val walkanimleft: Int
    public val walkanimright: Int
    public val runanim: Int
    public val runanimback: Int
    public val runanimleft: Int
    public val runanimright: Int
    public val crawlanim: Int
    public val crawlanimback: Int
    public val crawlanimleft: Int
    public val crawlanimright: Int
    public val recolsource: List<Int>
    public val recoldest: List<Int>
    public val retexsource: List<Int>
    public val retexdest: List<Int>
    public val op: List<String?>
    public val minimap: Boolean
    public val vislevel: Int
    public val resizeh: Int
    public val resizev: Int
    public val renderPriority: Int
    public val ambient: Int
    public val contrast: Int
    public val turnspeed: Int
    public val multinpc: List<Int>
    public val multivarbit: Int
    public val multivar: Int
    public val active: Boolean
    public val walksmoothing: Boolean
    public val follower: Boolean
    public val lowpriorityops: Boolean
    public val headicongroups: List<Int>
    public val headiconindices: List<Int>
    public val overlayheight: Int
    public val stat: List<Int>
    public val params: Map<Int, Any>
    public val footprintSize: Int
    public val worldOverlapTint: Boolean
    public val worldOverlapTintColour: Int
}
