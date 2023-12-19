package fr.bloctave.lmr.api.holefiller

import fr.bloctave.lmr.api.holefiller.handler.HoleFillerHandler
import fr.bloctave.lmr.api.holefiller.handler.HoleFillerImpactHandler
import fr.bloctave.lmr.api.proxy.SoftProxy

object HoleFillerProxy : SoftProxy<HoleFillerConfig>("hole_filler_mod", HoleFillerConfig::class) {

    init {
        this.addEventHandler(HoleFillerHandler::class)
        this.addEventHandler(HoleFillerImpactHandler::class)
    }




}