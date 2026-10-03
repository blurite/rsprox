package net.rsprox.mcpbridge;

/** How rsprox wants this client to draw its frames. */
enum Rendering {
    /** Frames drawn by the GPU plugin, when the client's profile enables it. */
    GPU,

    /** Frames drawn without the GPU plugin, which the bridge stops. */
    SOFTWARE;

    /** Get the rendering that the {@code softwareRendering} flag of a welcome asks for. */
    static Rendering fromWire(boolean softwareRendering) {
        return softwareRendering ? SOFTWARE : GPU;
    }
}
