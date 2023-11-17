package fr.bloctave.landmanager.proxy.vanilla.handler

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.handler.CommonEventHandler
import fr.bloctave.landmanager.proxy.IEventHandler
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.EventUtil
import fr.bloctave.landmanager.util.sendActionBarMessage
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerEvent

class PlayerBreakSpeedHandler : IEventHandler<PlayerEvent.BreakSpeed> {

    private var lastTimeHitProtectedBlock: Long = 0

    override fun handleEvent(event: PlayerEvent.BreakSpeed) = event.run  {
        val world = player.world
        val area = EventUtil.getArea(world, pos)
        if ((area != null && (area.isMember(player.uniqueID) || area.getProxyConfig(VanillaProxy).breakBlock)) ||
            EventUtil.ignoreProtection(player) ||
            (area == null && VanillaProxy.getConfig().breakBlock)
        ) {
            return
        }

        // Stop players from breaking blocks
        if (world.isRemote && world.gameTime - lastTimeHitProtectedBlock > 10)
            player.sendActionBarMessage("message.landmanager.protection.break", TextFormatting.RED)
        newSpeed = 0F
        isCanceled = true
    }
}