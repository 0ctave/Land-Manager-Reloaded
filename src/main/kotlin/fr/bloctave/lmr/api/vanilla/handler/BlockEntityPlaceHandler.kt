package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class BlockEntityPlaceHandler : IEventHandler<BlockEvent.EntityPlaceEvent> {

    @SubscribeEvent
    override fun handleEvent(event: BlockEvent.EntityPlaceEvent) = event.run {

        val area = EventUtil.basicChecks(entity, world as? World, pos) ?: return@run

        if (area.getConfig<VanillaConfig>().placeBlock())
            return@run


        (entity as? PlayerEntity)?.sendAreaActionBarMessage("message.lmr.protection.place", TextFormatting.RED, area.name)

        isCanceled = true
    }
}