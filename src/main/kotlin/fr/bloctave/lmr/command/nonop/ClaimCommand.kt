package fr.bloctave.lmr.command.nonop

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.config.ServerConfig
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.util.*
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge

object ClaimCommand : AbstractCommand(
	"claim",
	{
		// claim
		executes {
			val player = it.source.playerOrException
			val cap = player.level.areasCap
			val area = cap.intersectingArea(player.blockPosition()) ?: throw ClaimCommand.NOT_IN_AREA.create()
			return@executes ClaimCommand.doCommand(it, area, player)
		}
		thenArgument(AREA, AreaArgument) {
			// claim <area>
			executes { ClaimCommand.doCommand(it, AreaArgument.get(it, AREA), it.source.playerOrException) }
		}
	}
) {
	private val NOT_IN_AREA = SimpleCommandExceptionType(TranslationTextComponent("lmr.command.notInArea"))
	private val ALREADY_CLAIMED =
		DynamicCommandExceptionType { TranslationTextComponent("lmr.command.claim.already", it) }

	private fun doCommand(context: CommandContext<CommandSource>, area: Area, player: PlayerEntity): Int {
		area.owner?.let { throw ALREADY_CLAIMED.create(area.name) }

		// TODO: Economy support to buy areas

		val source = context.source
		if (ServerConfig.claimRequest()) {

			// Make this a "request" instead of immediately claiming
			source.server.requests.add(area.name, player.uuid)?.let {
				source.sendSuccess(TranslationTextComponent("lmr.command.claim.request.success", area.name), true)

				source.server.sendToOps(
					TranslationTextComponent("lmr.command.claim.request.opMessage", it, player.name, area.name)
				)
				return 1
			} ?: run {
				source.sendSuccess(TranslationTextComponent("lmr.command.claim.request.failed", area.name), true)
				return 0
			}
		} else if (!MinecraftForge.EVENT_BUS.post(fr.bloctave.lmr.AreaClaimEvent(player, area))) {
			// Claim the area
			area.owner = player.uuid
			player.level.areasCap.dataChanged(area, AreaUpdateType.CHANGE)
			source.sendSuccess(TranslationTextComponent("lmr.command.claim.claimed", area.name), true)
			LandManager.areaChange(source.server, AreaChangeType.CLAIM, area.name, player as ServerPlayerEntity)
			return 1
		}
		return 0
	}
}
