package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class PlayerBreakSpeedHandler : IEventHandler<PlayerEvent.BreakSpeed> {

    private var lastTimeHitProtectedBlock: Long = 0


    @SubscribeEvent
    override fun handleEvent(event: PlayerEvent.BreakSpeed) = event.run  {
        val world = player.level

        val area = EventUtil.basicChecks(player, world, pos)?: return@run

        if (area.getConfig<VanillaConfig>().breakBlock())
            return@run


        // Stop players from breaking blocks
        if (world.isClientSide && world.gameTime - lastTimeHitProtectedBlock > 10)
            player.sendAreaActionBarMessage("message.lmr.protection.break", TextFormatting.RED, area.name)

        newSpeed = 0F
        isCanceled = true
    }
}