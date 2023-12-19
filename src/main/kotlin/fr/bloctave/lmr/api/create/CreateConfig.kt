package fr.bloctave.lmr.api.create

import fr.bloctave.lmr.config.util.AreaConfigImpl
import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseValue

class CreateConfig : AreaConfigImpl() {

    @ConfigValue
    var kinetics: PreciseValue<Boolean> =
        this.configure("kinetics", false, "If true kinetic contraptions can move in an area by default")

}