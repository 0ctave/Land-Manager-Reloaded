package fr.bloctave.lmr.command.op

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.util.*
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
					MinecraftForge.EVENT_BUS.post(fr.bloctave.lmr.AreaDeletedEvent(area))
					server.requests.deleteAllForArea(areaName)
					context.source.sendSuccess(TranslationTextComponent("lmr.command.delete.deleted", areaName), true)
					// Notify all area members that the area was deleted
					area.owner?.let { DeleteCommand.notifyPlayer(server, it, areaName) }
					area.members.forEach { DeleteCommand.notifyPlayer(server, it, areaName) }
					// Send chat message to OPs
					LandManager.areaChange(context, AreaChangeType.DELETE, areaName)
					return@executes 1
				}
				context.source.sendSuccess(TranslationTextComponent("lmr.command.delete.failed", areaName), true)
				return@executes 0
			}
		}
	}
) {
	private fun notifyPlayer(server: MinecraftServer, uuid: UUID, areaName: String) =
		server.playerList.getPlayer(uuid)
			?.sendMessage(TranslationTextComponent("lmr.command.delete.notify", areaName))
}
