package fr.bloctave.lmr.api.supplementaries

import fr.bloctave.lmr.api.proxy.SoftProxy
import fr.bloctave.lmr.proxy.supplementaries.RopeArrowImpactHandler
import fr.bloctave.lmr.proxy.supplementaries.SlingShotImpactHandler

object SupplementariesProxy : SoftProxy<SupplementariesConfig>("supplementaries", SupplementariesConfig::class) {

    init {
        this.addEventHandler(RopeArrowImpactHandler::class)
        this.addEventHandler(SlingShotImpactHandler::class)
    }




}