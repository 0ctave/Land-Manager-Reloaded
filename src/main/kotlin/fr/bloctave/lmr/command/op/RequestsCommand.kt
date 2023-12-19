package fr.bloctave.lmr.command.op

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand.AREA_REGEX
import fr.bloctave.lmr.command.LMCommand.PAGE
import fr.bloctave.lmr.util.Util
import fr.bloctave.lmr.util.requests
import fr.bloctave.lmr.util.thenArgument
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.*
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.HoverEvent

object RequestsCommand : AbstractCommand(
	"requests",
	{
		// requests
		executes { RequestsCommand.doCommand(it) }
		thenArgument(PAGE, IntegerArgumentType.integer(1)) {
			// requests <pageNum>
			executes { RequestsCommand.doCommand(it, RequestsCommand.getPageNum(it)) }
			thenArgument(AREA_REGEX, StringArgumentType.word()) {
				// requests <pageNum> <areaName>
				executes {
					RequestsCommand.doCommand(
						it,
						RequestsCommand.getPageNum(it),
						RequestsCommand.getAreaName(it)
					)
				}
			}
		}
		thenArgument(AREA_REGEX, StringArgumentType.word()) {
			// requests <areaName>
			executes { RequestsCommand.doCommand(it, areaName = RequestsCommand.getAreaName(it)) }
		}
	}
) {
	private fun getPageNum(context: CommandContext<CommandSource>): Int = IntegerArgumentType.getInteger(context, PAGE)

	private fun getAreaName(context: CommandContext<CommandSource>): String =
		StringArgumentType.getString(context, AREA_REGEX)

	private fun doCommand(context: CommandContext<CommandSource>, page: Int = 1, areaName: String = ""): Int {
		val source = context.source
		val server = source.server
		val requestsWsd = server.requests
		val requests = if (areaName.isBlank()) requestsWsd.getAll()
			.sortedBy { it.id } else requestsWsd.getByAreaNameRegex(areaName)
		source.sendSuccess(
			Util.createListMessage(
			source.source is PlayerEntity,
			requests,
			page,
			"lmr.command.requests.title",
			{ "/lm op requests $it $areaName" },
			{ request ->
				val reqId = request.id
				StringTextComponent("$reqId: ")
					.append(createAction(true, reqId))
					.append(" ")
					.append(createAction(false, reqId))
					.append(" ${request.getPlayerName(server)} -> ${request.areaName} [${request.date}]")
			}
		), false)
		return requests.size
	}

	private fun createAction(approve: Boolean, requestId: Int): ITextComponent =
		StringTextComponent("[${if (approve) "/" else "X"}]").withStyle {
			it.withColor(Color.fromLegacyFormat(if (approve) TextFormatting.GREEN else TextFormatting.RED))
			val actionName = if (approve) "approve" else "disapprove"
			it.withHoverEvent(HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				TranslationTextComponent("lmr.command.requests.$actionName", requestId)
			))
			it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lm op $actionName $requestId"))
			return@withStyle it
		}
}
