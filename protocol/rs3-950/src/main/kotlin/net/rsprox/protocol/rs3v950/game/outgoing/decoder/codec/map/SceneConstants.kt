package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map

// Native 950 constructor: Ghidra 0x182d76 initializes Client+0x19440's
// scene window size (+0x610) to 256. Both rebuild handlers use size >> 4
// to convert their centre chunks to the scene's south-west tile origin.
// This is the scene window size, not the variable instance-template grid size.
internal const val SCENE_SIZE: Int = 256
