package fr.bloctave.lmr.command.op


import com.mojang.brigadier.context.CommandContext
import fr.bloctave.lmr.AreaClaimApprovalEvent
import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.LMCommand.REQUEST
import fr.bloctave.lmr.command.argumentType.RequestArgument
import fr.bloctave.lmr.data.requests.Request
import fr.bloctave.lmr.util.*
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge

object ApproveCommand : AbstractCommand(
    "approve",
    {
        thenArgument(REQUEST, RequestArgument) {
            executes {

                ApproveCommand.doCommand(it, RequestArgument.get(it, REQUEST), it.source.playerOrException)
                /*context ->
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
                context.source.sendSuccess(
                    TranslatableComponent(
                        "lmr.command.approve.success",
                        request.id,
                        request.getPlayerName(server),
                        areaName
                    ),
                    true
                )
                LandManager.areaChange(context, AreaChangeType.CLAIM, areaName)
                // Delete all requests for the area
                requests.deleteAllForArea(areaName)
                // Notify the player if they're online
                server.playerList.getPlayer(request.playerUuid)?.sendMessage(
                    TranslatableComponent("lmr.command.approve.playerMessage", areaName, context.getSenderName())
                )
                return@executes 1*/
            }
        }
    }
) {
    private fun doCommand(context: CommandContext<CommandSource>, request: Request, player: ServerPlayerEntity): Int {
        val server = context.source.server
        val requests = server.requests
        val areaName = request.areaName
        val areas = server.getWorldCapForArea(areaName) ?: run {
            requests.deleteAllForArea(areaName)
            throw LMCommand.ERROR_NO_AREA.create(areaName)
        }
        val area = areas.getArea(areaName) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)
        if (MinecraftForge.EVENT_BUS.post(AreaClaimApprovalEvent(request, area, player)))
            return 0

        // Approve request
        if (areas.setOwner(area, request.playerUuid)) {
            context.source.sendSuccess(
                TranslationTextComponent(
                    "lmr.command.approve.success",
                    request.id,
                    request.getPlayerName(server),
                    areaName
                ),
                true
            )
            LandManager.areaChange(context, AreaChangeType.CLAIM, areaName)
            // Delete all requests for the area
            requests.deleteAllForArea(areaName)
            // Notify the player if they're online
            server.playerList.getPlayer(request.playerUuid)?.sendMessage(
                TranslationTextComponent("lmr.command.approve.playerMessage", areaName, context.getSenderName())
            )
            return 1

        } else {
            context.source.sendFailure(TranslationTextComponent("lmr.command.approve.ownLimit", areaName))
            return 0
        }
    }
}