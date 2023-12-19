package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class LivingEntityUseItemHandler : IEventHandler<LivingEntityUseItemEvent> {

    @SubscribeEvent
    override fun handleEvent(event: LivingEntityUseItemEvent) = event.run{

        val area = EventUtil.basicChecks(entity, entity.level, entity.blockPosition()) ?: return@run

        if (area.getConfig<VanillaConfig>().useItem() || (entity.isCrouching && item.item is BlockItem))
            return@run


        (entity as? PlayerEntity)?.sendAreaActionBarMessage("message.lmr.protection.useItem", TextFormatting.RED, area.name)

        isCanceled = true
    }
}