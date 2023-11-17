package fr.bloctave.landmanager.command.optional

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.util.canEditArea
import fr.bloctave.landmanager.util.getWorldCapForArea
import fr.bloctave.landmanager.util.thenArgument
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.util.text.TranslationTextComponent

object RenameCommand : AbstractCommand(
	"rename",
	{
		thenArgument(AREA, AreaArgument) {
			thenArgument(RenameCommand.NEW_NAME, StringArgumentType.word()) {
				executes { context ->
					val source = context.source
					val area = AreaArgument.get(context, AREA)
					val areaName = area.name
					if (!source.source.canEditArea(area))
						throw LMCommand.ERROR_CANT_EDIT.create(areaName)
					val newName = StringArgumentType.getString(context, RenameCommand.NEW_NAME)
					if (!Area.validateName(newName))
						throw RenameCommand.INVALID_NAME.create()
					val areas = source.server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)

					val result = areas.renameArea(areaName, newName)
					val message = if (result)
						TranslationTextComponent("lm.command.rename.success", areaName, newName)
					else
						TranslationTextComponent("lm.command.rename.invalid", newName)
					source.sendFeedback(message, true)
					return@executes if (result) 1 else 0
				}
			}
		}
	}
) {
	private const val NEW_NAME = "newAreaName"
	private val INVALID_NAME =
		SimpleCommandExceptionType(TranslationTextComponent("message.landmanager.create.invalid_name"))
}
