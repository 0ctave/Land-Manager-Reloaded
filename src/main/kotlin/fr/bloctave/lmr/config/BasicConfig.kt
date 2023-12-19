package fr.bloctave.lmr.config

import fr.bloctave.lmr.config.util.IProxyAreaConfig
import fr.bloctave.lmr.config.util.PreciseValue
import net.minecraftforge.common.ForgeConfigSpec

abstract class BasicConfig : IProxyAreaConfig {

    final override val builder = ForgeConfigSpec.Builder()
    override var fields: HashMap<String, PreciseValue<Any>> = HashMap()

    //lateinit var config: ForgeConfigSpec

    fun bakeConfig(): Pair<BasicConfig, ForgeConfigSpec> {
        bake()
        return this to builder.build()
    }
}