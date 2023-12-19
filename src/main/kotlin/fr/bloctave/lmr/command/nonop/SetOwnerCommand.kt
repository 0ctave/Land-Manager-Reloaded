package fr.bloctave.lmr.command.nonop

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.LMCommand.PLAYER
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.data.areas.AreasCapability
import fr.bloctave.lmr.util.AreaChangeType
import fr.bloctave.lmr.util.canEditArea
import fr.bloctave.lmr.util.getWorldCapForArea
import fr.bloctave.lmr.util.thenArgument
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TranslationTextComponent

object SetOwnerCommand : AbstractCommand(
	"setowner",
	{
		thenArgument(AREA, AreaArgument) {
			requires { it.hasPermission(2) }
			// setowner <area>
			executes {
				val area = AreaArgument.get(it, AREA)
				val cap = it.source.server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
				return@executes SetOwnerCommand.doCommand(it, cap, area)
			}
			thenArgument(PLAYER, EntityArgument.player()) {
				requires { true }
				// setowner <area> <player>
				executes {
					val area = AreaArgument.get(it, AREA)
					val player = EntityArgument.getPlayer(it, PLAYER)
					if (!it.source.playerOrException.canEditArea(area))
						throw LMCommand.ERROR_CANT_EDIT.create(area.name)
					val cap = it.source.server.getWorldCapForArea(area)
						?: throw LMCommand.ERROR_NO_AREA.create(area.name)
					return@executes SetOwnerCommand.doCommand(it, cap, area, player)
				}
			}
		}
	}
) {
	private fun doCommand(
		context: CommandContext<CommandSource>,
		cap: AreasCapability,
		area: Area,
		player: ServerPlayerEntity? = null
	): Int {
		val playerUuid = player?.uuid
		area.owner?.let { area.addMember(it) }
		area.owner = playerUuid
		playerUuid?.let { area.removeMember(it) }
		cap.dataChanged(area, AreaUpdateType.CHANGE)
		context.source.sendSuccess(
			TranslationTextComponent(
				"lmr.command.setowner.success", area.name, player?.name?.plainCopy()
					?: "None"
			), true
		)
		LandManager.areaChange(context.source.server, playerUuid?.let { AreaChangeType.CLEAR_ALLOCATION }
			?: AreaChangeType.ALLOCATE, area.name)
		return 1
	}
}
