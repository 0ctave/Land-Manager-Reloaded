package fr.bloctave.landmanager.proxy

import kotlin.reflect.KClass

class CommandProxyFactory(private val configFactory : ProxyConfigFactory<*>) {

    init {


        configFactory.mainConfig().fields.forEach {
            //createCommand()
            println("Test on ${it}")
            println(configFactory.mainConfig().getProperty(it)!!.getter.call(configFactory.mainConfig()))
            println(configFactory.mainConfig().toggleProperty(it))
            println(configFactory.mainConfig().getProperty(it)!!.getter.call(configFactory.mainConfig()))

        }
    }

    /*private fun createCommand(): ProxyConfigCommand {

    }*/

}