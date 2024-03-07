package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.entity.item.HangingEntity
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class PlayerAttackHangingEntityHandler : IEventHandler<AttackEntityEvent> {

    @SubscribeEvent
    override fun handleEvent(event: AttackEntityEvent) = event.run {
        val area = EventUtil.basicChecks(player, target.level, target.blockPosition()) ?: return@run

        if (area.getConfig<VanillaConfig>().interactBlock() || (target !is HangingEntity)) {
            return@run
        }

        if (target.level.isClientSide)
            player.sendAreaActionBarMessage("message.lmr.protection.interactBlock", TextFormatting.RED, area.name)

        isCanceled = true
    }
}