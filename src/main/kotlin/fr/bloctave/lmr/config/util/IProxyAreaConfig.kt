package fr.bloctave.lmr.config.util

import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.util.INBTSerializable
import kotlin.reflect.KClass
import kotlin.reflect.full.*

interface IProxyAreaConfig : INBTSerializable<CompoundNBT> {

    val builder: ForgeConfigSpec.Builder
    val fields: HashMap<String, PreciseValue<Any>>

    override fun serializeNBT(): CompoundNBT = CompoundNBT().apply {
        fields.forEach {
            when (it.value.type) {
                Boolean::class -> putBoolean(it.key, it.value() as Boolean)
                Int::class -> putInt(it.key, it.value() as Int)
                Double::class -> putDouble(it.key, it.value() as Double)
                else -> throw IllegalArgumentException("Unknown type ${it.value}")
            }
        }
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        fields.forEach {
            when (it.value.type) {
                Boolean::class -> it.value.set(if (nbt.contains(it.key)) nbt.getBoolean(it.key) else it.value())
                Int::class -> it.value.set(if (nbt.contains(it.key)) nbt.getInt(it.key) else it.value())
                Double::class -> it.value.set(if (nbt.contains(it.key)) nbt.getDouble(it.key) else it.value())
                else -> throw IllegalArgumentException("Unknown type ${it.value}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(propertyName: String): T = fields[propertyName]?.invoke() as? T ?: throw IllegalArgumentException("Unknown type")

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(propertyName: String, type: KClass<T>): T = fields[propertyName]?.invoke() as? T ?: throw IllegalArgumentException("Unknown type")

    fun <T : Any> setValue(propertyName: String, value: T) {
        fields[propertyName]?.set(value)
    }


    fun configure(path: String, value: Boolean, vararg comment: String): PreciseValue<Boolean> {
        return PreciseValue(builder.comment(*comment).define(path, value), Boolean::class)
    }

    fun configureInRange(path: String, value: Int, min: Int, max: Int, vararg comment: String): PreciseRangeValue<Int> {
        return PreciseRangeValue(builder.comment(*comment).defineInRange(path, value, min, max), min, max, Int::class)
    }

    fun configureInRange(path: String, value: Double, min: Double, max: Double, vararg comment: String): PreciseRangeValue<Double> {
        return PreciseRangeValue(builder.comment(*comment).defineInRange(path, value, min, max), min, max, Double::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun bake() : ForgeConfigSpec.Builder {
        this::class.memberProperties.filter { it.findAnnotation<ConfigValue>() != null }.forEach {
            val property = it.getter.call(this)
            if (property is PreciseValue<*>)
                when (property.type) {
                    Boolean::class, Int::class,Double::class  -> fields[it.name] = property as PreciseValue<Any>
                    else -> return@forEach

                }
        }

        return builder
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IProxyAreaConfig>copyFields(instance: T): T {
        if (instance::class != this::class) throw IllegalArgumentException("Instance is not of the same type")
        fields.forEach {
            when (it.value.type) {
                Boolean::class, Int::class, Double::class -> it.value.set(instance.getValue(it.key, it.value.type))
                else -> throw IllegalArgumentException("Unknown type ${it.value}")
            }
        }
        return this as T
    }

}

inline fun <reified V : Enum<V>> IProxyAreaConfig.configureEnum(path: String, value: V, acceptableValues: Collection<V>, vararg comment: String): PreciseValue<V> {
    return PreciseValue(builder.comment(*comment).defineEnum(path, value, acceptableValues), V::class)
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@SinceKotlin("1.1")
annotation class ConfigValue