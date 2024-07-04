package net.rsprox.proxy.config

public class JavConfigBuilder(
    base: JavConfig,
) {
    private val builder: StringBuilder = StringBuilder(base.text)

    public fun replaceParam(
        id: Int,
        value: Any,
    ): JavConfigBuilder {
        val prefix = "param=$id="
        val index = builder.indexOf(prefix)
        if (index == -1) {
            throw IllegalArgumentException("Jav config does not support param $id")
        }
        val lineBreakIndex = builder.indexOf('\n', index)
        val endIndex = if (lineBreakIndex == -1) builder.lastIndex else lineBreakIndex
        builder.setRange(index, endIndex, prefix + value)
        return this
    }

    public fun replaceProperty(
        name: String,
        value: Any,
    ): JavConfigBuilder {
        val prefix = "$name="
        val index = builder.indexOf(prefix)
        if (index == -1) {
            throw IllegalArgumentException("Jav config does not support property $name")
        }
        val lineBreakIndex = builder.indexOf('\n', index)
        val endIndex = if (lineBreakIndex == -1) builder.lastIndex else lineBreakIndex
        builder.setRange(index, endIndex, prefix + value)
        return this
    }

    public fun build(): JavConfig {
        return JavConfig(builder.toString())
    }
}
