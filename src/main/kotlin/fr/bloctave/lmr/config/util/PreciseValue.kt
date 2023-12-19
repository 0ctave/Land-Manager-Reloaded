package fr.bloctave.lmr.config.util

import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KClass

open class PreciseValue<T: Any>(private val configValue: ForgeConfigSpec.ConfigValue<T>, val type: KClass<T>) : Any() {

    private var value: T? = null
    fun set(value: T) { this.value = value }
    operator fun invoke(): T = value ?: run {
        value = configValue.get()
        value!!
    }
}