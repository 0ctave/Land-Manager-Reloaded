package fr.bloctave.landmanager.proxy.vanilla.handler

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.areasCap
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.eventbus.api.Event

class LivingSpawnCheckSpawnHandler : IEventHandler<LivingSpawnEvent.CheckSpawn> {
    override fun handleEvent(event: LivingSpawnEvent.CheckSpawn) = event.run {
        if (world !is World)
            return
        val cap = (world as World).areasCap
        val areas = cap.intersectingAreas(Vector3d(x, y, z))
        val hostile = !entityLiving.type.classification.peacefulCreature

        // Stop entity spawning if within an area that prevents it
        if (areas.isEmpty() && !(if (hostile) VanillaProxy.getConfig().hostileSpawning else VanillaProxy.getConfig().passiveSpawning)) {
            result = Event.Result.DENY
            return
        }
        if (areas.any {
                !(if (hostile) it.getProxyConfig(VanillaProxy).hostileSpawning else it.getProxyConfig(
                    VanillaProxy
                ).passiveSpawning)
            })
            result = Event.Result.DENY
    }
}