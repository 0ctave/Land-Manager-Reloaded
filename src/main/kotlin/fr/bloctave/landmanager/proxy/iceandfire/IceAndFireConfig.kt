package fr.bloctave.landmanager.proxy.iceandfire

import fr.bloctave.landmanager.proxy.IProxyConfig
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

class IceAndFireConfig() : IProxyConfig<IceAndFireConfig.IceAndFireConfigBuilder>(IceAndFireConfigBuilder::class) {

    var dragonGrief: Boolean
    var dragonFire: Boolean
    var dragonFireDamage: Boolean

    init {
        dragonGrief = false
        dragonFire = false
        dragonFireDamage = false
    }

    override fun bake() {
        dragonGrief = COMMON.dragonGrief.get()
        dragonFire = COMMON.dragonFire.get()
        dragonFireDamage = COMMON.dragonFireDamage.get()
    }


    class IceAndFireConfigBuilder(builder: Builder): ConfigBuilder() {
        val dragonGrief: ForgeConfigSpec.BooleanValue = builder
            .comment("If true then the area owners can toggle whether dragons can grief in the area")
            .define("dragonGrief", false)
        val dragonFire: ForgeConfigSpec.BooleanValue = builder
            .comment("If true then the area owners can toggle whether dragons can grief throw projectiles in the area")
            .define("dragonFire", false)
        val dragonFireDamage: ForgeConfigSpec.BooleanValue = builder
            .comment("If true then the area owners can toggle whether dragons can grief the area with their breath")
            .define("dragonFireDamage", false)
    }

}

