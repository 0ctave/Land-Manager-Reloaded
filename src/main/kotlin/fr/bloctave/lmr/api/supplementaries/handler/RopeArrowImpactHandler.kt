package fr.bloctave.lmr.proxy.supplementaries

import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.supplementaries.SupplementariesConfig
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendActionBarMessage
import net.mehvahdjukaar.supplementaries.entities.RopeArrowEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class RopeArrowImpactHandler : IEventHandler<ProjectileImpactEvent.Arrow> {

    @SubscribeEvent
    override fun handleEvent(event: ProjectileImpactEvent.Arrow) = event.run {
        if (arrow !is RopeArrowEntity) {
            return@run
        }

        val projectile = arrow as RopeArrowEntity
        val shooter = projectile.owner

        println(rayTraceResult.location)

        val area = EventUtil.getArea(projectile.level as World, BlockPos(rayTraceResult.location)) ?: return@run

        if (area.getConfig<SupplementariesConfig>().ropeArrow()) {
            return@run
        }

        if(shooter != null && shooter is PlayerEntity)
            shooter.sendActionBarMessage("message.lmr.protection.throw", TextFormatting.RED)

        isCanceled = true
    }
}