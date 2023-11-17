package fr.bloctave.landmanager.proxy.vanilla.handler

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.EventUtil
import fr.bloctave.landmanager.util.sendActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class BlockEntityPlaceHandler : IEventHandler<BlockEvent.EntityPlaceEvent> {

    @SubscribeEvent
    override fun handleEvent(event: BlockEvent.EntityPlaceEvent) = event.run {
        if (entity !is PlayerEntity || world !is World)
            return
        val player = event.entity as PlayerEntity
        val area = EventUtil.getArea(world as World, pos)
        if ((area != null && (area.isMember(player.uniqueID) || area.getProxyConfig(VanillaProxy).breakBlock)) ||
            EventUtil.ignoreProtection(player) ||
            (area == null && VanillaProxy.getConfig().breakBlock) //TODO: Change this to a config option per area
        ) {
            return
        }

        // Stop players from placing blocks
        player.sendActionBarMessage("message.landmanager.protection.place", TextFormatting.RED)
        isCanceled = true
    }
}