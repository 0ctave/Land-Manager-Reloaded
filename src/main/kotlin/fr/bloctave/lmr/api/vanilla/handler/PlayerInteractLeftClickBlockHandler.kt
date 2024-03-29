package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class PlayerInteractLeftClickBlockHandler : IEventHandler<PlayerInteractEvent.LeftClickBlock> {

    @SubscribeEvent
    override fun handleEvent(event: PlayerInteractEvent.LeftClickBlock) = event.run {
        val area = EventUtil.basicChecks(player, world, pos) ?: return@run

        if (area.getConfig<VanillaConfig>().interactBlock()) {
            return@run
        }

        if (world.isClientSide && hand == Hand.MAIN_HAND)
            player.sendAreaActionBarMessage("message.lmr.protection.interactBlock", TextFormatting.RED, area.name)

        cancellationResult = ActionResultType.FAIL
        isCanceled = true
    }
}