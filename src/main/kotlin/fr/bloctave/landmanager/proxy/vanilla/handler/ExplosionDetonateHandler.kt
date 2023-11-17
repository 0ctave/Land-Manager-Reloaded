package fr.bloctave.landmanager.proxy.vanilla.handler

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.areasCap
import net.minecraftforge.event.world.ExplosionEvent

class ExplosionDetonateHandler : IEventHandler<ExplosionEvent.Detonate> {
    override fun handleEvent(event: ExplosionEvent.Detonate) = event.run {
        val cap = world.areasCap
        affectedBlocks.removeIf { pos ->
            cap.intersectingAreas(pos).let { areas ->
                if (areas.isEmpty())
                    VanillaProxy.getConfig().explosion
                else
                    areas.any { !it.getProxyConfig(VanillaProxy).explosion }
            }
        }

        return@run
    }
}