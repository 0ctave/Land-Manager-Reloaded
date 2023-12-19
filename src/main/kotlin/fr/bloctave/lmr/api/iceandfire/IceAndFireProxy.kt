package fr.bloctave.lmr.api.iceandfire

import fr.bloctave.lmr.api.iceandfire.handler.DragonFireDamageWorldEventHandler
import fr.bloctave.lmr.api.iceandfire.handler.DragonFireEventHandler
import fr.bloctave.lmr.api.iceandfire.handler.GenericGriefEventHandler
import fr.bloctave.lmr.api.proxy.SoftProxy

object IceAndFireProxy : SoftProxy<IceAndFireConfig>("iceandfire", IceAndFireConfig::class) {

    init {
        this.addEventHandler(DragonFireEventHandler::class)
        this.addEventHandler(DragonFireDamageWorldEventHandler::class)
        this.addEventHandler(GenericGriefEventHandler::class)
    }




}