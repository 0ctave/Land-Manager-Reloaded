package fr.bloctave.lmr.api.vanilla

import fr.bloctave.lmr.api.proxy.SoftProxy
import fr.bloctave.lmr.api.vanilla.handler.*

object VanillaProxy : SoftProxy<VanillaConfig>("minecraft", VanillaConfig::class) {

    init {
        this.addEventHandler(BlockEntityPlaceHandler::class)
        this.addEventHandler(ExplosionDetonateHandler::class)
        this.addEventHandler(LivingSpawnCheckSpawnHandler::class)
        this.addEventHandler(LivingSpawnCheckSpecialSpawnHandler::class)
        this.addEventHandler(BlockBreakHandler::class)
        this.addEventHandler(PlayerBreakSpeedHandler::class)

        this.addEventHandler(PlayerAttackHangingEntityHandler::class)

        this.addEventHandler(PlayerInteractLeftClickBlockHandler::class)
        this.addEventHandler(PlayerInteractRightClickBlockHandler::class)
        this.addEventHandler(PlayerInteractRightClickItemHandler::class)

        this.addEventHandler(LivingEntityUseItemHandler::class)

        this.addEventHandler(PistonHandler::class)
        this.addEventHandler(WorldTickHandler::class)

    }




}