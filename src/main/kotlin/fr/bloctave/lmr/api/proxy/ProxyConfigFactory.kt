package fr.bloctave.lmr.api.proxy

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.config.util.IProxyAreaConfig
import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ProxyConfigFactory<T : IProxyAreaConfig>(
    private val configClass: KClass<T>
) {

    private val mainConfig: Pair<T,ForgeConfigSpec>

    init {
        mainConfig = bake(configClass.primaryConstructor!!.call())

        LandManager.LOGGER.info("Baking config for ${configClass.simpleName}")
    }

    fun newInstance(): T = bake(configClass.primaryConstructor!!.call()).first.copyFields(mainConfig.first)
    fun mainConfig(): Pair<T,ForgeConfigSpec> = mainConfig

    fun bake(config: T): Pair<T, ForgeConfigSpec> {
        config.bake()
        return config to config.builder.build()
    }


}