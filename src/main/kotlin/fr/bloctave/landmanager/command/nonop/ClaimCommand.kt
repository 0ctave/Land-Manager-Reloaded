package fr.bloctave.landmanager.command.nonop

import fr.bloctave.landmanager.AreaClaimEvent
import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.data.areas.AreaUpdateType
import fr.bloctave.landmanager.util.*
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
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
			val player = it.source.asPlayer()
			val cap = player.world.areasCap
			val area = cap.intersectingArea(player.position) ?: throw ClaimCommand.NOT_IN_AREA.create()
			return@executes ClaimCommand.doCommand(it, area, player)
		}
		thenArgument(AREA, AreaArgument) {
			// claim <area>
			executes { ClaimCommand.doCommand(it, AreaArgument.get(it, AREA), it.source.asPlayer()) }
		}
	}
) {
	private val NOT_IN_AREA = SimpleCommandExceptionType(TranslationTextComponent("lm.command.notInArea"))
	private val ALREADY_CLAIMED =
		DynamicCommandExceptionType { TranslationTextComponent("lm.command.claim.already", it) }

	private fun doCommand(context: CommandContext<CommandSource>, area: Area, player: PlayerEntity): Int {
		area.owner?.let { throw ALREADY_CLAIMED.create(area.name) }

		// TODO: Economy support to buy areas

		val source = context.source
		if (LMConfig.claimRequest) {
			// Make this a "request" instead of immediately claiming
			source.server.requests.add(area.name, player.uniqueID)?.let {
				source.sendFeedback(TranslationTextComponent("lm.command.claim.request.success", area.name), true)
				source.server.sendToOps(
					TranslationTextComponent("lm.command.claim.request.opMessage", it, player.name, area.name)
				)
				return 1
			} ?: run {
				source.sendFeedback(TranslationTextComponent("lm.command.claim.request.failed", area.name), true)
				return 0
			}
		} else if (!MinecraftForge.EVENT_BUS.post(fr.bloctave.landmanager.AreaClaimEvent(player, area))) {
			// Claim the area
			area.owner = player.uniqueID
			player.world.areasCap.dataChanged(area, AreaUpdateType.CHANGE)
			source.sendFeedback(TranslationTextComponent("lm.command.claim.claimed", area.name), true)
			LandManager.areaChange(source.server, AreaChangeType.CLAIM, area.name, player as ServerPlayerEntity)
			return 1
		}
		return 0
	}
}
