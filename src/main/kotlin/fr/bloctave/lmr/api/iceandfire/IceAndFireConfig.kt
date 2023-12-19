package fr.bloctave.lmr.api.iceandfire

import fr.bloctave.lmr.config.util.AreaConfigImpl
import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseValue

class IceAndFireConfig : AreaConfigImpl() {

    @ConfigValue
    var dragonGrief: PreciseValue<Boolean> =
        this.configure("dragonGrief", false, "If true then dragons can grief in the area by default")

    @ConfigValue
    var dragonFire: PreciseValue<Boolean> =
        this.configure("dragonFire", false, "If true then dragons can throw projectiles in the area by default")

    @ConfigValue
    var dragonFireDamage: PreciseValue<Boolean> =
        this.configure("dragonFireDamage", false, "If true then dragons can grief the area with their breath by default")

}

