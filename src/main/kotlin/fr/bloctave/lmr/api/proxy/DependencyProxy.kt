package fr.bloctave.lmr.api.proxy


import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.config.util.IProxyAreaConfig
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.FileUtils
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.registerConfig
import kotlin.reflect.full.createInstance

class DependencyProxy(private val MOD_ID: String) {
    private val dependencies: HashMap<String, SoftProxy<out IProxyAreaConfig>> = HashMap()
    private val loadedDependencies: HashMap<String, SoftProxy<out IProxyAreaConfig>> = HashMap()
    private val LOGGER = LogManager.getLogger(DependencyProxy::class.java)


    init {
        LOGGER.info("[{}] Ready to listen for dependencies", MOD_ID)
        FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(MOD_ID), MOD_ID)

    }

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
                            proxy.modid
                        )
                        FORGE_BUS.apply { register(eventHandlerClass.createInstance()) }
                    }
                    registerConfig(ModConfig.Type.COMMON, proxy.getConfigSpec(), "%s/%s.toml".format(MOD_ID, modid))
                    loadedDependencies[modid] = proxy
                }
            }
        }
    }

    fun addDependency(proxy: SoftProxy<out IProxyAreaConfig>) {
        dependencies[proxy.modid] = proxy
    }

    fun getLoadedDependencies(): HashMap<String, SoftProxy<out IProxyAreaConfig>> = loadedDependencies
    fun getProxy(proxy: String): SoftProxy<*>? {
        return dependencies[proxy]
    }

}
