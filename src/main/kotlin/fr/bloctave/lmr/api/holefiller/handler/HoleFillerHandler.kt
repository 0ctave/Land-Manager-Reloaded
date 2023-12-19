package fr.bloctave.lmr.api.holefiller.handler

import com.dannyboythomas.hole_filler_mod.entities.EntityThrowableHoleFillerBase
import com.dannyboythomas.hole_filler_mod.tiles.TileHoleFillerBase
import fr.bloctave.lmr.api.holefiller.HoleFillerConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.entity.EntityLeaveWorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class HoleFillerHandler : IEventHandler<EntityLeaveWorldEvent> {

    @SubscribeEvent
    override fun handleEvent(event: EntityLeaveWorldEvent) = event.run {
        if (entity !is EntityThrowableHoleFillerBase) {
            return@run
        }

        val shooter = (entity as EntityThrowableHoleFillerBase).owner

        EventUtil.basicChecks(shooter, entity.level, entity.blockPosition()) ?: return

        for (tileEntity in entity.level.tickableBlockEntities) {
            if (tileEntity is TileHoleFillerBase && tileEntity.blockPos.closerThan(entity.blockPosition(), 5.0)) {
                tileEntity.holedata?.preVolume?.forEach { pos ->
                    val area = EventUtil.basicChecks(shooter, entity.level, BlockPos(pos)) ?: return@forEach

                    tileEntity.running = area.getConfig<HoleFillerConfig>().holeFilling()
                    return@run
                }
            }
        }
    }
}