package fr.bloctave.lmr.config.util

import net.minecraftforge.common.ForgeConfigSpec

abstract class AreaConfigImpl(override val builder: ForgeConfigSpec.Builder = ForgeConfigSpec.Builder(), override var fields: HashMap<String, PreciseValue<Any>> = HashMap()) :
    IProxyAreaConfig