package fr.bloctave.lmr.api.holefiller

import fr.bloctave.lmr.config.util.AreaConfigImpl
import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseValue

class HoleFillerConfig : AreaConfigImpl() {

    @ConfigValue
    var holeFilling: PreciseValue<Boolean> = this.configure("holeFilling", false, "If true the hole filler will fill holes in the area by default")

}