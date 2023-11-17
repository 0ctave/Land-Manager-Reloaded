package fr.bloctave.landmanager.proxy

import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireConfig
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.util.INBTSerializable
import org.apache.commons.lang3.tuple.Pair
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

abstract class IProxyConfig<T : IProxyConfig.ConfigBuilder>(configBuilder: KClass<T>) : INBTSerializable<CompoundNBT> {

    val COMMON: T
    val COMMON_SPEC: ForgeConfigSpec
    val fields : HashSet<String> = HashSet()
    init {
        ForgeConfigSpec.Builder().configure { configBuilder.primaryConstructor!!.call(it) }.apply {
            COMMON = left
            COMMON_SPEC = right
        }
        COMMON::class.memberProperties.forEach { fields.add(it.name) }
    }

    override fun serializeNBT(): CompoundNBT = CompoundNBT().apply {
        for (field in fields) {
            putBoolean(field, this::class.java.getField(field).getBoolean(this@IProxyConfig))
        }
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        for (field in fields) {
            this::class.java.getField(field).setBoolean(this@IProxyConfig, nbt.getBoolean(field))
        }
    }

    fun getProperty(propertyName: String): KMutableProperty1<*, *>? = this::class.members.firstOrNull { it.name == propertyName } as? KMutableProperty1<*, *>

    fun toggleProperty(propertyName: String) {
        getProperty(propertyName)?.let {
            if (it.returnType.classifier == Boolean::class) {
                it.setter.call(this, !(it.getter.call(this) as Boolean))
            } else {
                println("Property $propertyName is not a Boolean")
            }
        } ?: println("Property $propertyName not found")
    }

    abstract fun bake()
    abstract class ConfigBuilder

}
