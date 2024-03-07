package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class BlockBreakHandler : IEventHandler<BlockEvent.BreakEvent> {

    @SubscribeEvent
    override fun handleEvent(event: BlockEvent.BreakEvent) = event.run {

        val area = EventUtil.basicChecks(player, world as World, pos) ?: return@run

        if (area.getConfig<VanillaConfig>().breakBlock())
            return@run


        // Stop players from placing blocks
        player?.sendAreaActionBarMessage("message.lmr.protection.break", TextFormatting.RED, area.name)

        isCanceled = true
    }
}