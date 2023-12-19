package fr.bloctave.lmr.util

import fr.bloctave.lmr.data.areas.Area
import net.minecraft.entity.player.PlayerEntity

object CommandUtil {

    fun canSeeArea(area: Area, player: PlayerEntity): Boolean = area.isMember(player.uuid) || player.canEditArea(area)


}