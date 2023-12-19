package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class ExplosionDetonateHandler : IEventHandler<ExplosionEvent.Detonate> {

    @SubscribeEvent
    override fun handleEvent(event: ExplosionEvent.Detonate) = event.run {

        affectedBlocks.removeIf { pos ->
            return@removeIf !(EventUtil.basicChecks(explosion.exploder, world, pos) ?: return@removeIf false).getConfig<VanillaConfig>().explosion()
        }

        (explosion.exploder as? PlayerEntity)?.sendActionBarMessage("message.lmr.protection.explosion", TextFormatting.RED)

        return@run
    }
}