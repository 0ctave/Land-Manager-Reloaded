package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.EventUtil
import net.minecraft.block.Blocks
import net.minecraft.world.World
import net.minecraftforge.event.world.PistonEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class PistonHandler : IEventHandler<PistonEvent.Pre> {

    @SubscribeEvent
    override fun handleEvent(event: PistonEvent.Pre) = event.run {

        if (state.block == Blocks.STICKY_PISTON && !this.pistonMoveType.isExtend) {
            val pos = pos.relative(direction, 2)
            if (world.getBlockState(pos).isAir) return@run
            EventUtil.basicChecks(null, world as World?, pos)
                ?.let { if (it.getConfig<VanillaConfig>().pistonAction()) return@run } ?: return@run
        } else if (world.getBlockState(pos.relative(direction)).isAir) {
            EventUtil.basicChecks(null, world as World?, pos.relative(direction))
                ?.let { if (it.getConfig<VanillaConfig>().pistonAction()) { return@run }} ?: return@run
        } else {
            val structure = structureHelper ?: return@run

            if (!structure.resolve()) return@run


            if (structure.toPush.map { it.relative(direction) }.fold(true) { result, value ->
                    val area = EventUtil.basicChecks(null, world as World?, value) ?: return@fold result

                    return@fold area.getConfig<VanillaConfig>().pistonAction()
                }) return@run


        }

        isCanceled = true
    }
}