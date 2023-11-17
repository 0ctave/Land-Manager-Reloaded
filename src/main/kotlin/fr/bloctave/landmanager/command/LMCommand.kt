package fr.bloctave.landmanager.command

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.command.nonop.*
import fr.bloctave.landmanager.command.op.ApproveCommand
import fr.bloctave.landmanager.command.op.DeleteCommand
import fr.bloctave.landmanager.command.op.DisapproveCommand
import fr.bloctave.landmanager.command.op.RequestsCommand
import fr.bloctave.landmanager.command.optional.*
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.data.areas.AreaUpdateType
import fr.bloctave.landmanager.util.canEditArea
import fr.bloctave.landmanager.util.getWorldCapForArea
import fr.bloctave.landmanager.util.thenCommand
import fr.bloctave.landmanager.util.thenLiteral
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.command.CommandSource
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent

object LMCommand : AbstractCommand(
	"lm",
	{
		// Non-op
		thenCommand(AreaCommand)
		thenCommand(AreasCommand)
		thenCommand(ClaimCommand)
		thenCommand(MembersCommand)
		thenCommand(MyAreasCommand)
		thenCommand(SetOwnerCommand)
		thenCommand(ShowCommand)
		thenCommand(ShowOffCommand)

		// Op
		thenLiteral("op") {
			requires { it.hasPermissionLevel(2) }
			thenCommand(ApproveCommand)
			thenCommand(DeleteCommand)
			thenCommand(DisapproveCommand)
			thenCommand(RequestsCommand)

			// Optional
			/*if (!LMConfig.explosions)
				thenCommand(ExplosionsCommand)
			if (!LMConfig.hostileSpawning)
				thenCommand(HostilesCommand)
			if (!LMConfig.interactions)
				thenCommand(InteractionsCommand)
			if (!LMConfig.passiveSpawning)
				thenCommand(PassivesCommand)*/
			if (!LMConfig.rename)
				thenCommand(RenameCommand)
			if (!LMConfig.tool)
				thenCommand(ToolCommand)
		}

		// Optional
		/*if (LMConfig.explosions)
			thenCommand(ExplosionsCommand)
		if (LMConfig.hostileSpawning)
			thenCommand(HostilesCommand)
		if (LMConfig.interactions)
			thenCommand(InteractionsCommand)
		if (LMConfig.passiveSpawning)
			thenCommand(PassivesCommand)*/
		if (LMConfig.rename)
			thenCommand(RenameCommand)
		if (LMConfig.tool)
			thenCommand(ToolCommand)
	}
) {
	const val AREA = "areaName"
	const val PAGE = "pageNum"
	const val AREA_REGEX = "areaNameRegex"
	const val PLAYER = "playerName"
	const val REQUEST = "requestId"

	val ERROR_CANT_EDIT = DynamicCommandExceptionType { TranslationTextComponent("lm.command.noPerm", it) }
	val ERROR_NO_AREA = DynamicCommandExceptionType { TranslationTextComponent("lm.command.none", it) }

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
		source.sendFeedback(feedback(area), true)
		return 1
	}
}
