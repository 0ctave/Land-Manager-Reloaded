package fr.bloctave.lmr.command.nonop

import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.LMCommand.PLAYER
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.util.canEditArea
import fr.bloctave.lmr.util.getWorldCapForArea
import fr.bloctave.lmr.util.thenArgument
import fr.bloctave.lmr.util.thenLiteral
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.util.text.TranslationTextComponent

object MembersCommand : AbstractCommand(
	"members",
	{
		thenLiteral("add") {
			thenArgument(AREA, AreaArgument) {
				thenArgument(PLAYER, EntityArgument.player()) {
					// members add <area> <player>
					executes { context ->
						val area = AreaArgument.get(context, AREA)
						val player = EntityArgument.getPlayer(context, PLAYER)
						if (!context.source.playerOrException.canEditArea(area))
							throw LMCommand.ERROR_CANT_EDIT.create(area.name)

						val server = context.source.server
						val cap = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
						val uuid = player.uuid
						if (!cap.canJoinArea(uuid))
							throw MembersCommand.CANT_JOIN.create(cap.getNumAreasJoined(uuid))

						if (area.addMember(uuid)) {
							cap.increasePlayerAreasNum(uuid)
							cap.dataChanged(area, AreaUpdateType.CHANGE)
							context.source.sendSuccess(TranslationTextComponent("lmr.command.members.add.success", player.displayName, area.name), true)
							return@executes 1
						}
						context.source.sendSuccess(TranslationTextComponent("lmr.command.members.add.already", player.displayName, area.name), true)
						return@executes 0
					}
				}
			}
		}
		thenLiteral("remove") {
			thenArgument(AREA, AreaArgument) {
				thenArgument(PLAYER, EntityArgument.player()) {
					// members remove <area> <player>
					executes { context ->
						val area = AreaArgument.get(context, AREA)
						val player = EntityArgument.getPlayer(context, PLAYER)
						if (!context.source.playerOrException.canEditArea(area))
							throw LMCommand.ERROR_CANT_EDIT.create(area.name)

						val server = context.source.server
						val cap = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
						val uuid = player.uuid
						if (!cap.canJoinArea(uuid))
							throw MembersCommand.CANT_JOIN.create(cap.getNumAreasJoined(uuid))

						if (area.removeMember(uuid)) {
							cap.decreasePlayerAreasNum(uuid)
							cap.dataChanged(area, AreaUpdateType.CHANGE)
							context.source.sendSuccess(
								TranslationTextComponent(
									"lmr.command.members.remove.success",
									player.displayName,
									area.name
								), true
							)
							return@executes 1
						}
						context.source.sendSuccess(
							TranslationTextComponent(
								"lmr.command.members.remove.already",
								player.displayName,
								area.name
							), true
						)
						return@executes 0
					}
				}
			}
		}
	}
) {
	private val CANT_JOIN =
		DynamicCommandExceptionType { TranslationTextComponent("message.lmr.error.maxJoined", it) }
}
