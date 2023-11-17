package fr.bloctave.landmanager.proxy.iceandfire.handler

import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent
import fr.bloctave.landmanager.util.areasCap
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireProxy
import net.minecraft.util.math.BlockPos
import net.minecraftforge.eventbus.api.SubscribeEvent

class GenericGriefEventHandler : IEventHandler<GenericGriefEvent> {

    @SubscribeEvent
    override fun handleEvent(event: GenericGriefEvent) = event.run {
        val cap = event.entity.world.areasCap

        val pos = BlockPos(event.targetX, event.targetY, event.targetZ)

        if(cap.intersectingAreas(pos).let { areas ->
                if (areas.isEmpty())
                    !IceAndFireProxy.getConfig().dragonGrief
                else
                    areas.any { !it.getProxyConfig(IceAndFireProxy).dragonGrief }
            }) {
            isCanceled = true
        }

    }
}