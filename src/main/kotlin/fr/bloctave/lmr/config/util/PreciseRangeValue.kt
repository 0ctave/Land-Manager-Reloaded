package fr.bloctave.lmr.config.util

import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KClass

class PreciseRangeValue<T : Comparable<T>>(private val configValue: ForgeConfigSpec.ConfigValue<T>, val min: T, val max: T, type: KClass<T>) : PreciseValue<T>(configValue, type)