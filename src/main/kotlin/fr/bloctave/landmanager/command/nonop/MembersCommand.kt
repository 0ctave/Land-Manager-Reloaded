package fr.bloctave.landmanager.command.nonop

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.LMCommand.PLAYER
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.data.areas.AreaUpdateType
import fr.bloctave.landmanager.util.canEditArea
import fr.bloctave.landmanager.util.getWorldCapForArea
import fr.bloctave.landmanager.util.thenArgument
import fr.bloctave.landmanager.util.thenLiteral
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
						if (!player.canEditArea(area))
							throw LMCommand.ERROR_CANT_EDIT.create(area.name)

						val server = context.source.server
						val cap = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
						val uuid = player.uniqueID
						if (!cap.canJoinArea(uuid))
							throw MembersCommand.CANT_JOIN.create(cap.getNumAreasJoined(uuid))

						if (area.addMember(uuid)) {
							cap.increasePlayerAreasNum(uuid)
							cap.dataChanged(area, AreaUpdateType.CHANGE)
							context.source.sendFeedback(TranslationTextComponent("lm.command.members.add.success", player.displayName, area.name), true)
							return@executes 1
						}
						context.source.sendFeedback(TranslationTextComponent("lm.command.members.add.already", player.displayName, area.name), true)
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
						if (!player.canEditArea(area))
							throw LMCommand.ERROR_CANT_EDIT.create(area.name)

						val server = context.source.server
						val cap = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
						val uuid = player.uniqueID
						if (!cap.canJoinArea(uuid))
							throw MembersCommand.CANT_JOIN.create(cap.getNumAreasJoined(uuid))

						if (area.removeMember(uuid)) {
							cap.decreasePlayerAreasNum(uuid)
							cap.dataChanged(area, AreaUpdateType.CHANGE)
							context.source.sendFeedback(
								TranslationTextComponent(
									"lm.command.members.remove.success",
									player.displayName,
									area.name
								), true
							)
							return@executes 1
						}
						context.source.sendFeedback(
							TranslationTextComponent(
								"lm.command.members.remove.already",
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
		DynamicCommandExceptionType { TranslationTextComponent("message.landmanager.error.maxJoined", it) }
}
