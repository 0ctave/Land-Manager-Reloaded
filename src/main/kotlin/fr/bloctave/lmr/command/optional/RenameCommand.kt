package fr.bloctave.lmr.command.optional

import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.util.canEditArea
import fr.bloctave.lmr.util.getWorldCapForArea
import fr.bloctave.lmr.util.thenArgument
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
						TranslationTextComponent("lmr.command.rename.success", areaName, newName)
					else
						TranslationTextComponent("lmr.command.rename.invalid", newName)
					source.sendSuccess(message, true)
					return@executes if (result) 1 else 0
				}
			}
		}
	}
) {
	private const val NEW_NAME = "newAreaName"
	private val INVALID_NAME =
		SimpleCommandExceptionType(TranslationTextComponent("message.lmr.create.invalid_name"))
}
