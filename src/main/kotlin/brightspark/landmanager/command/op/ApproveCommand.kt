package brightspark.landmanager.command.op

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.thenArgument
import brightspark.landmanager.AreaClaimApprovalEvent
import brightspark.landmanager.LandManager
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.REQUEST
import brightspark.landmanager.command.argumentType.RequestArgument
import brightspark.landmanager.util.AreaChangeType
import brightspark.landmanager.util.getSenderName
import brightspark.landmanager.util.getWorldCapForArea
import brightspark.landmanager.util.requests
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge

object ApproveCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("approve") {
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
				if (MinecraftForge.EVENT_BUS.post(AreaClaimApprovalEvent(request, area, context.source)))
					return@executes 0

				// Approve request
				areas.setOwner(areaName, request.playerUuid)
				context.source.sendFeedback(TranslationTextComponent("lm.command.approve.success", request.id, request.getPlayerName(server), areaName), true)
				LandManager.areaChange(context, AreaChangeType.CLAIM, areaName)
				// Delete all requests for the area
				requests.deleteAllForArea(areaName)
				// Notify the player if they're online
				server.playerList.getPlayerByUUID(request.playerUuid)?.sendMessage(TranslationTextComponent("lm.command.approve.playerMessage", areaName, context.getSenderName()).applyTextStyle(TextFormatting.DARK_GREEN))
				return@executes 1
			}
		}
	}
}