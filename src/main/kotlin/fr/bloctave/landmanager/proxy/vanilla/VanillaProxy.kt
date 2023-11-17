package fr.bloctave.landmanager.proxy.vanilla

import fr.bloctave.landmanager.proxy.SoftProxy
import fr.bloctave.landmanager.proxy.vanilla.handler.*

object VanillaProxy : SoftProxy<VanillaConfig>("minecraft", VanillaConfig::class) {

    init {
        this.addEventHandler(BlockEntityPlaceHandler::class)
        this.addEventHandler(ExplosionDetonateHandler::class)
        this.addEventHandler(LivingSpawnCheckSpawnHandler::class)
        this.addEventHandler(PlayerBreakSpeedHandler::class)
        this.addEventHandler(PlayerInteractRightClickBlockHandler::class)

    }




}