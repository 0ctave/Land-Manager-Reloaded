package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.item.BlockItem
import net.minecraft.util.Hand
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class PlayerInteractRightClickItemHandler : IEventHandler<PlayerInteractEvent.RightClickItem> {

    @SubscribeEvent
    override fun handleEvent(event: PlayerInteractEvent.RightClickItem) = event.run {

        val area = EventUtil.basicChecks(player, world, pos) ?: return@run

        if (area.getConfig<VanillaConfig>().interactItem() || (player.isCrouching && itemStack.item is BlockItem)) {
            return@run
        }

        // If player is holding shift with an itemblock, then allow it for block placing checks

        // Stop players from right clicking blocks
        if (world.isClientSide && hand == Hand.MAIN_HAND)
            player.sendAreaActionBarMessage("message.lmr.protection.interactItem", TextFormatting.RED, area.name)

        isCanceled = true
    }
}