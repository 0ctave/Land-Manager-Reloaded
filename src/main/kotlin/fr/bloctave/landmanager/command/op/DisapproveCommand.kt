package fr.bloctave.landmanager.command.op

import fr.bloctave.landmanager.AreaClaimDisapprovalEvent
import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.LMCommand.REQUEST
import fr.bloctave.landmanager.command.argumentType.RequestArgument
import fr.bloctave.landmanager.util.*
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
				if (MinecraftForge.EVENT_BUS.post(
                        fr.bloctave.landmanager.AreaClaimDisapprovalEvent(
                            request,
                            area,
                            context.source
                        )
                    ))
					return@executes 0

				// Disapprove request
				requests.delete(areaName, request.id)
				context.source.sendFeedback(
					TranslationTextComponent(
						"lm.command.disapprove.success",
						request.id,
						request.getPlayerName(server),
						areaName
					), true
				)
				// Notify the player if they're online
				server.playerList.getPlayerByUUID(request.playerUuid)?.sendMessage(
					TranslationTextComponent("lm.command.disapprove.playerMessage", areaName, context.getSenderName())
						.mergeStyle(TextFormatting.RED)
				)
				return@executes 1
			}
		}
	}
)
