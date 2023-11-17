package fr.bloctave.landmanager.proxy.iceandfire.handler

import com.github.alexthe666.iceandfire.api.event.DragonFireEvent
import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireConfig
import fr.bloctave.landmanager.util.areasCap
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireProxy
import net.minecraft.util.math.BlockPos
import net.minecraftforge.eventbus.api.SubscribeEvent

class DragonFireEventHandler : IEventHandler<DragonFireEvent> {

    @SubscribeEvent
    override fun handleEvent(event: DragonFireEvent) = event.run {
        val cap = event.entity.world.areasCap

        val pos = BlockPos(event.targetX, event.targetY, event.targetZ)

        if(cap.intersectingAreas(pos).let { areas ->
                if (areas.isEmpty())
                    !IceAndFireProxy.getConfig().dragonFire
                else
                    areas.any {!it.getProxyConfig(IceAndFireProxy).dragonFire }
            }) {
            isCanceled = true
        }

    }

}