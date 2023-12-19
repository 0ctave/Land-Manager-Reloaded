package fr.bloctave.lmr.util

import fr.bloctave.lmr.config.CommonConfig
import fr.bloctave.lmr.data.areas.Area
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object EventUtil {


    fun getArea(world: World?, pos: BlockPos): Area? =
        if(world is World) world.areasCap.intersectingArea(pos) else null


    fun ignoreProtection(player: PlayerEntity): Boolean =
        ((CommonConfig.creativeIgnoresProtection() && player.isCreative) || player.hasPermissions(2))

    fun basicChecks(entity: Entity?, world: World?, pos: BlockPos): Area? {
        if(entity != null && entity is PlayerEntity && ignoreProtection(entity)) return null

        val area = getArea(world, pos)

        if (area == null || (entity is PlayerEntity && area.isMember(entity.uuid)))
            return null

        return area
    }

}