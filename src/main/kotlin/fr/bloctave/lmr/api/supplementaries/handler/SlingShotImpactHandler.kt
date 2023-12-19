package fr.bloctave.lmr.proxy.supplementaries

import fr.bloctave.lmr.api.supplementaries.SupplementariesConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendActionBarMessage
import net.mehvahdjukaar.supplementaries.entities.SlingshotProjectileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class SlingShotImpactHandler : IEventHandler<ProjectileImpactEvent.Throwable> {

    @SubscribeEvent
    override fun handleEvent(event: ProjectileImpactEvent.Throwable) = event.run {
        if (throwable !is SlingshotProjectileEntity) {
            return@run
        }

        val projectile = throwable as SlingshotProjectileEntity
        val shooter = projectile.owner
        if (shooter == null)
            println("SHOOOTER IS NULL")

        val area = EventUtil.basicChecks(entity, projectile.level as World, projectile.blockPosition()) ?: return@run


        if (area.getConfig<SupplementariesConfig>().slingShot()) {
            return@run
        }

        if(shooter != null && shooter is PlayerEntity)
            shooter.sendActionBarMessage("message.lmr.protection.throw", TextFormatting.RED)

        isCanceled = true
    }
}