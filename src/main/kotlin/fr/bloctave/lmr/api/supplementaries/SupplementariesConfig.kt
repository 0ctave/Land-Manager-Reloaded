package fr.bloctave.lmr.api.supplementaries

import fr.bloctave.lmr.config.util.AreaConfigImpl
import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseValue

class SupplementariesConfig : AreaConfigImpl() {

    @ConfigValue
    var slingShot: PreciseValue<Boolean> =
        this.configure("slingShot", false, "If true the sling shot can throw block in this area by default")

    @ConfigValue
    var ropeArrow: PreciseValue<Boolean> =
        this.configure("ropeArrow", false, "If true the rope arrow can create ropes in this area by default")



}