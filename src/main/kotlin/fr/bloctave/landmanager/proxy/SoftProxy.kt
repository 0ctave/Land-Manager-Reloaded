package fr.bloctave.landmanager.proxy

import kotlin.reflect.KClass

abstract class SoftProxy<T: IProxyConfig<out IProxyConfig.ConfigBuilder>>(val modid: String, private val configClass: KClass<T>) {

    private var configFactory: ProxyConfigFactory<T> = ProxyConfigFactory(configClass)
    private var commandFactory: CommandProxyFactory = CommandProxyFactory(configFactory)
    private val config: T = configFactory.mainConfig()


    private var handlers: MutableSet<KClass<out IEventHandler<*>>> = HashSet()

    fun addEventHandler(eventHandler: KClass<out IEventHandler<*>>) {
        handlers.add(eventHandler)
    }

    fun createConfig() = configFactory.newInstance()

    fun getConfig(): T = config

    fun getConfigClass(): KClass<T> = configClass

    fun getPermission(fieldName: String): Boolean {
        val field = config.COMMON.javaClass.getDeclaredField(fieldName)
        //field.isAccessible = true
        return field.getBoolean(config.COMMON)
    }

    val eventHandlers: Set<KClass<out IEventHandler<*>>>
        get() = handlers
}
