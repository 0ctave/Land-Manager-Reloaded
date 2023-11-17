package fr.bloctave.landmanager.proxy.iceandfire


import fr.bloctave.landmanager.proxy.iceandfire.handler.DragonFireDamageWorldEventHandler
import fr.bloctave.landmanager.proxy.iceandfire.handler.DragonFireEventHandler
import fr.bloctave.landmanager.proxy.iceandfire.handler.GenericGriefEventHandler
import fr.bloctave.landmanager.proxy.SoftProxy

object IceAndFireProxy : SoftProxy<IceAndFireConfig>("iceandfire", IceAndFireConfig::class) {

    init {
        this.addEventHandler(DragonFireEventHandler::class)
        this.addEventHandler(DragonFireDamageWorldEventHandler::class)
        this.addEventHandler(GenericGriefEventHandler::class)
    }




}