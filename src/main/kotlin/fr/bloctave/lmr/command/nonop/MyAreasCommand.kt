package fr.bloctave.lmr.command.nonop

import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand.AREA_REGEX
import fr.bloctave.lmr.command.LMCommand.PAGE
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.util.Util
import fr.bloctave.lmr.util.getAreas
import fr.bloctave.lmr.util.thenArgument
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.util.text.StringTextComponent
import kotlin.streams.toList

object MyAreasCommand : AbstractCommand(
	"myareas",
	{
		// myareas
		executes { MyAreasCommand.doCommand(it) }
		thenArgument(PAGE, IntegerArgumentType.integer(1)) {
			// myareas <page>
			executes { MyAreasCommand.doCommand(it, IntegerArgumentType.getInteger(it, PAGE)) }
			thenArgument(AREA_REGEX, StringArgumentType.word()) {
				// myareas <page> <areaName>
				executes {
					MyAreasCommand.doCommand(
						it,
						IntegerArgumentType.getInteger(it, PAGE),
						StringArgumentType.getString(it, AREA_REGEX)
					)
				}
			}
		}
		thenArgument(AREA_REGEX, StringArgumentType.word()) {
			// myareas <areaName>
			executes { MyAreasCommand.doCommand(it, areaName = StringArgumentType.getString(it, AREA_REGEX)) }
		}
	}
) {
	private fun doCommand(context: CommandContext<CommandSource>, page: Int = 1, areaName: String = ""): Int {
		val source = context.source
		val uuid = source.playerOrException.uuid
		val regex = Regex(areaName)
		val areas = source.server.getAreas { it.isOwner(uuid) && (areaName.isBlank() || regex.matches(it.name)) }
			.sorted(Comparator.comparing(Area::name)).toList<Area>()
		val message = Util.createListMessage(
			true,
			areas,
			page,
			"lmr.command.myareas.title",
			{ "/lm myareas $it $areaName" },
			{ StringTextComponent("  ${it.name}") }
		)
		source.sendSuccess(message, false)
		return areas.size
	}
}
