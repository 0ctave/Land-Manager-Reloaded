package fr.bloctave.lmr.api.iceandfire.handler

import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent
import fr.bloctave.lmr.api.iceandfire.IceAndFireConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.eventbus.api.SubscribeEvent

class GenericGriefEventHandler : IEventHandler<GenericGriefEvent> {

    @SubscribeEvent
    override fun handleEvent(event: GenericGriefEvent) = event.run {
        val area = EventUtil.basicChecks(entity, entity.level as World, BlockPos(targetX, targetY, targetZ)) ?: return@run

        if (area.getConfig<IceAndFireConfig>().dragonGrief()) {
            return@run
        }

        isCanceled = true

    }
}