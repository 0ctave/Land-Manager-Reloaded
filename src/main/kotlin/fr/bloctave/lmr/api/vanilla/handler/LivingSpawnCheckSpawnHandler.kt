package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent

class LivingSpawnCheckSpawnHandler : IEventHandler<LivingSpawnEvent.CheckSpawn> {

    @SubscribeEvent
    override fun handleEvent(event: LivingSpawnEvent.CheckSpawn) = event.run {

        val area = EventUtil.basicChecks(entity, world as? World, entity.blockPosition()) ?: return@run
        val hostile = !entityLiving.type.category.isFriendly

        if ((hostile && area.getConfig<VanillaConfig>().hostileSpawning()) || (!hostile && area.getConfig<VanillaConfig>().passiveSpawning()))
            return@run

        result = Event.Result.DENY
    }
}