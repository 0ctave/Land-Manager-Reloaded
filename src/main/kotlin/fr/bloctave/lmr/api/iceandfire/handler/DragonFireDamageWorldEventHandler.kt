package fr.bloctave.lmr.api.iceandfire.handler

import com.github.alexthe666.iceandfire.api.event.DragonFireDamageWorldEvent
import fr.bloctave.lmr.api.iceandfire.IceAndFireConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.eventbus.api.SubscribeEvent

class DragonFireDamageWorldEventHandler :
    IEventHandler<DragonFireDamageWorldEvent> {

    @SubscribeEvent
    override fun handleEvent(event: DragonFireDamageWorldEvent) = event.run {
        val area = EventUtil.basicChecks(entity, entity.level as World, BlockPos(targetX, targetY, targetZ)) ?: return@run

        /*val pos = BlockPos(targetX, targetY, targetZ)
        val level = dragon.level
        val stage = dragon.dragonStage
        val j = 2
        val k = 2
        val l = 2

        val player: PlayerEntity? = dragon.ridingPlayer*/

        /*if (stage <= 3) {
            BlockPos.betweenClosedStream(pos.offset(-j, -k, -l), pos.offset(j, k, l)).forEach { pos ->
                val area = EventUtil.basicChecks(player, level as World, BlockPos(targetX, targetY, targetZ)) ?: return@forEach

                if (area.getConfig<IceAndFireConfig>().dragonFireDamage()) {
                    return@run
                }
            }
        } else {
            val radius = (if (stage == 4) 2 else 3) + 2
            BlockPos.betweenClosedStream(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius)).forEach { pos ->
                val area = EventUtil.basicChecks(player, level as World, BlockPos(targetX, targetY, targetZ)) ?: return@forEach

                if (area.getConfig<IceAndFireConfig>().dragonFireDamage()) {
                    return@run
                }
            }
        }*/

        //val area = EventUtil.basicChecks(entity, entity.level as World, BlockPos(targetX, targetY, targetZ)) ?: return@run

        if (area.getConfig<IceAndFireConfig>().dragonFireDamage()) {
            return@run
        }

        isCanceled = true
    }

}