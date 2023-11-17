package fr.bloctave.landmanager.proxy.vanilla

import fr.bloctave.landmanager.proxy.IProxyConfig
import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireConfig
import net.minecraftforge.common.ForgeConfigSpec

class VanillaConfig() : IProxyConfig<VanillaConfig.VanillaConfigBuilder>(VanillaConfigBuilder::class) {

    var passiveSpawning: Boolean
    var hostileSpawning: Boolean
    var explosion: Boolean
    var interaction: Boolean
    var placeBlock: Boolean
    var breakBlock: Boolean

    init {
        passiveSpawning = false
        hostileSpawning = false
        explosion = false
        interaction = false
        placeBlock = false
        breakBlock = false
    }

    override fun bake() {
        passiveSpawning = COMMON.passiveSpawning.get()
        hostileSpawning = COMMON.hostileSpawning.get()
        explosion = COMMON.explosion.get()
        interaction = COMMON.interaction.get()
        placeBlock = COMMON.placeBlock.get()
        breakBlock = COMMON.breakBlock.get()
    }


    class VanillaConfigBuilder(builder: ForgeConfigSpec.Builder): ConfigBuilder() {
        val passiveSpawning: ForgeConfigSpec.BooleanValue = builder
            .comment("If true area owners can toggle whether passive entities can spawn in the area")
            .define("passiveSpawning", false)
        val hostileSpawning: ForgeConfigSpec.BooleanValue = builder
            .comment("If true area owners can toggle whether hostile entities can spawn in the area")
            .define("hostileSpawning", false)
        val explosion: ForgeConfigSpec.BooleanValue = builder
            .comment("If true area owners can toggle whether explosions can destroy blocks in the area")
            .define("explosion", false)
        val interaction: ForgeConfigSpec.BooleanValue = builder
            .comment("If true area owners can toggle whether other players can interact (right click) with blocks in the area")
            .define("interaction", false)
        val placeBlock: ForgeConfigSpec.BooleanValue = builder
            .comment("If true area owners can toggle whether other players can place blocks in the area")
            .define("placeBlock", false)
        val breakBlock: ForgeConfigSpec.BooleanValue = builder
            .comment("If true area owners can toggle whether other players can break blocks in the area")
            .define("breakBlock", false)



    }

}