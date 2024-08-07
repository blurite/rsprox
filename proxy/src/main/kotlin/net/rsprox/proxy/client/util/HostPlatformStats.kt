package net.rsprox.proxy.client.util

@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public data class HostPlatformStats(
    public val version: Int,
    public val osType: Int,
    public val os64Bit: Int,
    public val osVersion: Int,
    public val javaVendor: Int,
    public val javaVersionMajor: Int,
    public val javaVersionMinor: Int,
    public val javaVersionPatch: Int,
    public val applet: Int,
    public val javaMaxMemoryMb: Int,
    public val javaAvailableProcessors: Int,
    public val systemMemory: Int,
    public val systemSpeed: Int,
    public val gpuDxName: String,
    public val gpuGlName: String,
    public val gpuDxVersion: String,
    public val gpuGlVersion: String,
    public val gpuDriverMonth: Int,
    public val gpuDriverYear: Int,
    public val cpuManufacturer: String,
    public val cpuBrand: String,
    public val cpuCount1: Int,
    public val cpuCount2: Int,
    public val cpuFeatures: IntArray,
    public val cpuSignature: Int,
    public val clientName: String,
    public val deviceName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HostPlatformStats

        if (version != other.version) return false
        if (osType != other.osType) return false
        if (os64Bit != other.os64Bit) return false
        if (osVersion != other.osVersion) return false
        if (javaVendor != other.javaVendor) return false
        if (javaVersionMajor != other.javaVersionMajor) return false
        if (javaVersionMinor != other.javaVersionMinor) return false
        if (javaVersionPatch != other.javaVersionPatch) return false
        if (applet != other.applet) return false
        if (javaMaxMemoryMb != other.javaMaxMemoryMb) return false
        if (javaAvailableProcessors != other.javaAvailableProcessors) return false
        if (systemMemory != other.systemMemory) return false
        if (systemSpeed != other.systemSpeed) return false
        if (gpuDxName != other.gpuDxName) return false
        if (gpuGlName != other.gpuGlName) return false
        if (gpuDxVersion != other.gpuDxVersion) return false
        if (gpuGlVersion != other.gpuGlVersion) return false
        if (gpuDriverMonth != other.gpuDriverMonth) return false
        if (gpuDriverYear != other.gpuDriverYear) return false
        if (cpuManufacturer != other.cpuManufacturer) return false
        if (cpuBrand != other.cpuBrand) return false
        if (cpuCount1 != other.cpuCount1) return false
        if (cpuCount2 != other.cpuCount2) return false
        if (!cpuFeatures.contentEquals(other.cpuFeatures)) return false
        if (cpuSignature != other.cpuSignature) return false
        if (clientName != other.clientName) return false
        if (deviceName != other.deviceName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + osType
        result = 31 * result + os64Bit.hashCode()
        result = 31 * result + osVersion
        result = 31 * result + javaVendor
        result = 31 * result + javaVersionMajor
        result = 31 * result + javaVersionMinor
        result = 31 * result + javaVersionPatch
        result = 31 * result + applet.hashCode()
        result = 31 * result + javaMaxMemoryMb.hashCode()
        result = 31 * result + javaAvailableProcessors
        result = 31 * result + systemMemory
        result = 31 * result + systemSpeed
        result = 31 * result + gpuDxName.hashCode()
        result = 31 * result + gpuGlName.hashCode()
        result = 31 * result + gpuDxVersion.hashCode()
        result = 31 * result + gpuGlVersion.hashCode()
        result = 31 * result + gpuDriverMonth
        result = 31 * result + gpuDriverYear
        result = 31 * result + cpuManufacturer.hashCode()
        result = 31 * result + cpuBrand.hashCode()
        result = 31 * result + cpuCount1
        result = 31 * result + cpuCount2
        result = 31 * result + cpuFeatures.contentHashCode()
        result = 31 * result + cpuSignature
        result = 31 * result + clientName.hashCode()
        result = 31 * result + deviceName.hashCode()
        return result
    }

    override fun toString(): String {
        return "HostPlatformStats(" +
            "version=$version, " +
            "osType=$osType, " +
            "os64Bit=$os64Bit, " +
            "osVersion=$osVersion, " +
            "javaVendor=$javaVendor, " +
            "javaVersionMajor=$javaVersionMajor, " +
            "javaVersionMinor=$javaVersionMinor, " +
            "javaVersionPatch=$javaVersionPatch, " +
            "applet=$applet, " +
            "javaMaxMemoryMb=$javaMaxMemoryMb, " +
            "javaAvailableProcessors=$javaAvailableProcessors, " +
            "systemMemory=$systemMemory, " +
            "systemSpeed=$systemSpeed, " +
            "gpuDxName='$gpuDxName', " +
            "gpuGlName='$gpuGlName', " +
            "gpuDxVersion='$gpuDxVersion', " +
            "gpuGlVersion='$gpuGlVersion', " +
            "gpuDriverMonth=$gpuDriverMonth, " +
            "gpuDriverYear=$gpuDriverYear, " +
            "cpuManufacturer='$cpuManufacturer', " +
            "cpuBrand='$cpuBrand', " +
            "cpuCount1=$cpuCount1, " +
            "cpuCount2=$cpuCount2, " +
            "cpuFeatures=${cpuFeatures.contentToString()}, " +
            "cpuSignature=$cpuSignature, " +
            "clientName='$clientName', " +
            "deviceName='$deviceName'" +
            ")"
    }
}
