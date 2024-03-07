package fr.bloctave.lmr.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.command.nonop.*
import fr.bloctave.lmr.command.op.ApproveCommand
import fr.bloctave.lmr.command.op.DeleteCommand
import fr.bloctave.lmr.command.op.DisapproveCommand
import fr.bloctave.lmr.command.op.RequestsCommand
import fr.bloctave.lmr.command.optional.ToolCommand
import fr.bloctave.lmr.config.CommonConfig
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.util.canEditArea
import fr.bloctave.lmr.util.getWorldCapForArea
import fr.bloctave.lmr.util.thenCommand
import fr.bloctave.lmr.util.thenLiteral
import net.minecraft.command.CommandSource
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent

object LMCommand : AbstractCommand(
	"lmr",
	{
		// Non-op
		thenCommand(AreaCommand)
		thenCommand(ClaimCommand)
		thenCommand(MembersCommand)
		thenCommand(MyAreasCommand)
		thenCommand(SetOwnerCommand)
		thenCommand(ShowCommand)
		thenCommand(ShowOffCommand)
		thenCommand(DeleteCommand)

		if (CommonConfig.tool())
			thenCommand(ToolCommand)

		// Op
		thenLiteral("op") {
			requires { it.hasPermission(2) }
			thenCommand(ApproveCommand)
			thenCommand(DeleteCommand)
			thenCommand(DisapproveCommand)
			thenCommand(RequestsCommand)
			thenCommand(AreasCommand)

			if (!CommonConfig.tool())
				thenCommand(ToolCommand)

		}
	}
) {
	const val AREA = "areaName"
	const val PAGE = "pageNum"
	const val AREA_REGEX = "areaNameRegex"
	const val PLAYER = "playerName"
	const val REQUEST = "requestId"

	val ERROR_CANT_EDIT = DynamicCommandExceptionType { TranslationTextComponent("lmr.command.noPerm", it) }
	val ERROR_NO_PLAYER = DynamicCommandExceptionType { TranslationTextComponent("lmr.command.noPlayer", it) }
	val ERROR_NO_AREA = DynamicCommandExceptionType { TranslationTextComponent("lmr.command.none", it) }

	fun permissionCommand(
		context: CommandContext<CommandSource>,
		action: (Area) -> Unit,
		feedback: (Area) -> ITextComponent
	): Int {
		val source = context.source
		val area = AreaArgument.get(context, AREA)
		val areas = source.server.getWorldCapForArea(area) ?: throw ERROR_NO_AREA.create(area.name)
		val sender = source.source
		if (!sender.canEditArea(area))
			throw ERROR_CANT_EDIT.create(area.name)
		action(area)
		areas.dataChanged(area, AreaUpdateType.CHANGE)
		source.sendSuccess(feedback(area), true)
		return 1
	}
}
