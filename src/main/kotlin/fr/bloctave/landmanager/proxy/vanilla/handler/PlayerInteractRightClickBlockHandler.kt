package fr.bloctave.landmanager.proxy.vanilla.handler

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.EventUtil
import fr.bloctave.landmanager.util.sendActionBarMessage
import net.minecraft.item.BlockItem
import net.minecraft.util.Hand
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.Event

class PlayerInteractRightClickBlockHandler : IEventHandler<PlayerInteractEvent.RightClickBlock> {
    override fun handleEvent(event: PlayerInteractEvent.RightClickBlock) = event.run {
        val area = EventUtil.getArea(world, pos)
        if ((area != null && (area.getProxyConfig(VanillaProxy).interaction || area.isMember(player.uniqueID))) ||
            EventUtil.ignoreProtection(player) ||
            (area == null && VanillaProxy.getConfig().interaction) ||
            // If player is holding shift with an itemblock, then allow it for block placing checks
            (player.isSneaking && itemStack.item is BlockItem)
        ) {
            return
        }
        // If player is holding shift with an itemblock, then allow it for block placing checks

        // Stop players from right clicking blocks
        if (world.isRemote && hand == Hand.MAIN_HAND)
            player.sendActionBarMessage("message.landmanager.protection.interact", TextFormatting.RED)
        useBlock = Event.Result.DENY    }
}