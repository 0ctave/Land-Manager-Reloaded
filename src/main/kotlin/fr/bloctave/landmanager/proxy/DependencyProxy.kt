package fr.bloctave.landmanager.proxy


import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.LandManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.FileUtils
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.registerConfig

class DependencyProxy(private val MOD_ID: String) {
    private val dependencies: HashMap<String, SoftProxy<out IProxyConfig<out IProxyConfig.ConfigBuilder>>> = HashMap()
    private val LOGGER = LogManager.getLogger(DependencyProxy::class.java)


    init {
        LOGGER.info("[{}] Ready to listen for dependencies", MOD_ID)
        FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(MOD_ID), MOD_ID)

    }

    @Throws(Exception::class)
    fun registerDependencies() {
        if (dependencies.isNotEmpty()) {
            FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(LandManager.MOD_ID), LandManager.MOD_ID)

            for ((modid, proxy) in dependencies) {
                if (ModList.get().isLoaded(modid) || modid == "minecraft") {
                    LOGGER.info("[{}] {} is present starting registration", MOD_ID, modid)
                    for (eventHandlerClass in proxy.eventHandlers) {
                        LOGGER.info(
                            "[{}] Registering event handler {} for proxy {}",
                            MOD_ID,
                            eventHandlerClass.simpleName,
                            proxy.javaClass.name
                        )
                        MinecraftForge.EVENT_BUS.register(eventHandlerClass)
                    }
                    registerConfig(ModConfig.Type.COMMON, proxy.getConfig().COMMON_SPEC, "%s/%s.toml".format(MOD_ID, modid))
                }
            }
        }
    }

    fun addDependency(proxy: SoftProxy<out IProxyConfig<out IProxyConfig.ConfigBuilder>>) {
        dependencies[proxy.modid] = proxy
    }

    fun getDependencies(): HashMap<String, SoftProxy<out IProxyConfig<out IProxyConfig.ConfigBuilder>>> = dependencies

}
