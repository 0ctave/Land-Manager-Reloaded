package fr.bloctave.lmr.api.iceandfire.handler

import com.github.alexthe666.iceandfire.api.event.DragonFireEvent
import fr.bloctave.lmr.api.iceandfire.IceAndFireConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.eventbus.api.SubscribeEvent

class DragonFireEventHandler : IEventHandler<DragonFireEvent> {

    @SubscribeEvent
    override fun handleEvent(event: DragonFireEvent) = event.run {
        val area = EventUtil.basicChecks(entity, entity.level as World, BlockPos(targetX, targetY, targetZ)) ?: return@run

        if (area.getConfig<IceAndFireConfig>().dragonFire()) {
            return@run
        }

        isCanceled = true

    }

}