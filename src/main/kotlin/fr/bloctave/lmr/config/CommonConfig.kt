package fr.bloctave.lmr.config

import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseValue

object CommonConfig : BasicConfig() {


    @ConfigValue
    val creativeIgnoresProtection: PreciseValue<Boolean> = this.configure("creativeIgnoresProtection", false, "Whether non-op players in creative can break/place blocks in any area")

     @ConfigValue
    val tool: PreciseValue<Boolean> = this.configure("tool", false, "If non-op players can use '/lmr tool' to get the admin tool for creating areas")
    @ConfigValue
    val rename: PreciseValue<Boolean> = this.configure("rename", false, "If area owners can rename their areas")

    @ConfigValue
    val nestedAreas: PreciseValue<Boolean> = this.configure("nestedAreas", true, "Enable nested areas", "If false then areas cannot be created inside other areas")

}