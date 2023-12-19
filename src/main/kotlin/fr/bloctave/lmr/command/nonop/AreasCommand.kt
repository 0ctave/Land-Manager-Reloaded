package fr.bloctave.lmr.command.nonop

import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand.AREA_REGEX
import fr.bloctave.lmr.command.LMCommand.PAGE
import fr.bloctave.lmr.data.areas.Area
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import fr.bloctave.lmr.util.*
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.StringTextComponent
import kotlin.streams.toList

object AreasCommand : AbstractCommand(
	"areas",
	{
		// areas
		executes { AreasCommand.doCommand(it) }
		thenArgument(PAGE, IntegerArgumentType.integer(1)) {
			// areas <pageNum>
			executes { AreasCommand.doCommand(it, AreasCommand.getPageNum(it)) }
			thenArgument(AREA_REGEX, StringArgumentType.word()) {
				// areas <pageNum> <areaName>
				executes { AreasCommand.doCommand(it, AreasCommand.getPageNum(it), AreasCommand.getAreaName(it)) }
			}
		}
		thenArgument(AREA_REGEX, StringArgumentType.word()) {
			// area <areaName>
			executes { AreasCommand.doCommand(it, areaName = AreasCommand.getAreaName(it)) }
		}
	}
) {
	private fun getPageNum(context: CommandContext<CommandSource>): Int = IntegerArgumentType.getInteger(context, PAGE)

	private fun getAreaName(context: CommandContext<CommandSource>): String =
		StringArgumentType.getString(context, AREA_REGEX)

	private fun doCommand(context: CommandContext<CommandSource>, page: Int = 1, areaName: String = ""): Int {
		val source = context.source
		val server = source.server
		val regex = Regex(areaName)
		val areas = server.getAreas { areaName.isBlank() || regex.matches(it.name) }.filter { CommandUtil.canSeeArea(it, source.playerOrException) }
			.sorted(Comparator.comparing(Area::name)).toList<Area>()
		source.sendSuccess(Util.createListMessage(
			source.source is PlayerEntity,
			areas,
			page,
			"lmr.command.areas.title",
			{ "/lm areas $it $areaName" },
			{ area ->
				StringTextComponent(area.owner?.let { "  ${area.name} -> ${server.getUsernameFromUuid(it)}" }
					?: "  ${area.name}")
			}
		), false)
		return areas.size
	}
}
