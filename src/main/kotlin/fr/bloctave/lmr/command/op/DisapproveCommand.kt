package fr.bloctave.lmr.command.op


import fr.bloctave.lmr.AreaClaimDisapprovalEvent
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.LMCommand.REQUEST
import fr.bloctave.lmr.command.argumentType.RequestArgument
import fr.bloctave.lmr.util.*
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge

object DisapproveCommand : AbstractCommand(
	"disapprove",
	{
		thenArgument(REQUEST, RequestArgument) {
			executes { context ->
				val request = RequestArgument.get(context, REQUEST)
				val server = context.source.server
				val requests = server.requests
				val areaName = request.areaName
				val areas = server.getWorldCapForArea(areaName) ?: run {
					requests.deleteAllForArea(areaName)
					throw LMCommand.ERROR_NO_AREA.create(areaName)
				}
				val area = areas.getArea(areaName) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)
				if (MinecraftForge.EVENT_BUS.post(AreaClaimDisapprovalEvent(request, area, context.source.playerOrException)))
					return@executes 0

				// Disapprove request
				requests.delete(areaName, request.id)
				context.source.sendSuccess(
					TranslationTextComponent(
						"lmr.command.disapprove.success",
						request.id,
						request.getPlayerName(server),
						areaName
					), true
				)
				// Notify the player if they're online
				server.playerList.getPlayer(request.playerUuid)?.sendMessage(
					TranslationTextComponent("lmr.command.disapprove.playerMessage", areaName, context.getSenderName())
						.withStyle(TextFormatting.RED)
				)
				return@executes 1
			}
		}
	}
)
