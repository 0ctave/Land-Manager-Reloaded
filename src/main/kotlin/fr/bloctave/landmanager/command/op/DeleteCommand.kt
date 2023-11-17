package fr.bloctave.landmanager.command.op

import fr.bloctave.landmanager.AreaDeletedEvent
import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.util.*
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge
import java.util.*

object DeleteCommand : AbstractCommand(
	"delete",
	{
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				val server = context.source.server
				val area = AreaArgument.get(context, AREA)
				val areaName = area.name
				val areas = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)
				if (areas.removeArea(areaName)) {
					MinecraftForge.EVENT_BUS.post(fr.bloctave.landmanager.AreaDeletedEvent(area))
					server.requests.deleteAllForArea(areaName)
					context.source.sendFeedback(TranslationTextComponent("lm.command.delete.deleted", areaName), true)
					// Notify all area members that the area was deleted
					area.owner?.let { DeleteCommand.notifyPlayer(server, it, areaName) }
					area.members.forEach { DeleteCommand.notifyPlayer(server, it, areaName) }
					// Send chat message to OPs
					LandManager.areaChange(context, AreaChangeType.DELETE, areaName)
					return@executes 1
				}
				context.source.sendFeedback(TranslationTextComponent("lm.command.delete.failed", areaName), true)
				return@executes 0
			}
		}
	}
) {
	private fun notifyPlayer(server: MinecraftServer, uuid: UUID, areaName: String) =
		server.playerList.getPlayerByUUID(uuid)
			?.sendMessage(TranslationTextComponent("lm.command.delete.notify", areaName))
}
