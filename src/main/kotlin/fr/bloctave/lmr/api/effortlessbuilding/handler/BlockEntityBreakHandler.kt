package fr.bloctave.lmr.api.effortlessbuilding.handler

import fr.bloctave.lmr.api.effortlessbuilding.event.EffortlessBuildingBreakEvent
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.eventbus.api.SubscribeEvent
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager

class BlockEntityBreakHandler : IEventHandler<EffortlessBuildingBreakEvent> {

    @SubscribeEvent
    override fun handleEvent(event: EffortlessBuildingBreakEvent) = event.run {

        val modeSettings = ModeSettingsManager.getModeSettings(player)
        val buildMode = modeSettings.buildMode

        val coordinates = buildMode.instance.findCoordinates(player, pos, true) ?: return@run

        coordinates.forEach {
            val area = EventUtil.basicChecks(player, world as? World, it) ?: return@forEach
            if (area.getConfig<VanillaConfig>().breakBlock())
                return@forEach

            player.sendAreaActionBarMessage("message.lmr.protection.break", TextFormatting.RED, area.name)
            event.isCanceled = true
            return@run
        }
    }
}

