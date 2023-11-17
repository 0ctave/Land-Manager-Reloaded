package fr.bloctave.landmanager.util

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.data.areas.Area
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object EventUtil {


    fun getArea(world: World, pos: BlockPos): Area? = world.areasCap.intersectingArea(pos)


    fun ignoreProtection(player: PlayerEntity): Boolean =
        (LMConfig.creativeIgnoresProtection && player.isCreative) || player.hasPermissionLevel(2)

}