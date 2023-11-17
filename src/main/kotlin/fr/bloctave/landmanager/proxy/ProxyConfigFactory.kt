package fr.bloctave.landmanager.proxy

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ProxyConfigFactory<T : IProxyConfig<out IProxyConfig.ConfigBuilder>>(private val configClass : KClass<T>) {


    private val mainConfig: T = newInstance()

    init {
        val config = newInstance()
        config.bake()
    }

    fun newInstance(): T = configClass.primaryConstructor!!.call()
    fun mainConfig(): T = mainConfig
}