package fr.bloctave.lmr.api.holefiller.handler

import com.dannyboythomas.hole_filler_mod.entities.EntityThrowableHoleFillerBase
import fr.bloctave.lmr.api.holefiller.HoleFillerConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class HoleFillerImpactHandler : IEventHandler<ProjectileImpactEvent.Throwable> {

    @SubscribeEvent
    override fun handleEvent(event: ProjectileImpactEvent.Throwable) = event.run {
        if (throwable !is EntityThrowableHoleFillerBase) {
            return@run
        }

        val projectile = throwable as EntityThrowableHoleFillerBase
        val shooter = projectile.owner

        val area = EventUtil.basicChecks(shooter, projectile.level as World, projectile.blockPosition()) ?: return@run

        if (area.getConfig<HoleFillerConfig>().holeFilling()) {
            return@run
        }

        if(shooter != null && shooter is PlayerEntity)
            shooter.sendActionBarMessage("message.lmr.protection.throw", TextFormatting.RED)

        isCanceled = true
    }
}