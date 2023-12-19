package fr.bloctave.lmr.api.proxy

import fr.bloctave.lmr.config.util.IProxyAreaConfig
import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KClass

abstract class SoftProxy<T : IProxyAreaConfig>(val modid: String, private val configClass: KClass<T>) {

    private var configFactory: ProxyConfigFactory<T> = ProxyConfigFactory(configClass)
    private var commandFactory: CommandProxyFactory = CommandProxyFactory(this)

    private var handlers: MutableSet<KClass<out IEventHandler<*>>> = HashSet()

    fun addEventHandler(eventHandler: KClass<out IEventHandler<*>>) {
        handlers.add(eventHandler)
    }

    fun createConfig(): T = configFactory.newInstance()

    fun getConfigClass() = configClass
    fun getConfig(): T = configFactory.mainConfig().first

    fun getConfigSpec(): ForgeConfigSpec = configFactory.mainConfig().second

    val eventHandlers: Set<KClass<out IEventHandler<*>>>
        get() = handlers
}
