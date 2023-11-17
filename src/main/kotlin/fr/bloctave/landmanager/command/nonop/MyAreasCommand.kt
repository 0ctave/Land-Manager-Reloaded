package fr.bloctave.landmanager.command.nonop

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand.AREA_REGEX
import fr.bloctave.landmanager.command.LMCommand.PAGE
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.util.Util
import fr.bloctave.landmanager.util.getAreas
import fr.bloctave.landmanager.util.thenArgument
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
		val uuid = source.asPlayer().uniqueID
		val regex = Regex(areaName)
		val areas = source.server.getAreas { it.isOwner(uuid) && (areaName.isBlank() || regex.matches(it.name)) }
			.sorted(Comparator.comparing(Area::name))
			.toList()
		val message = Util.createListMessage(
			true,
			areas,
			page,
			"lm.command.myareas.title",
			{ "/lm myareas $it $areaName" },
			{ StringTextComponent("  ${it.name}") }
		)
		source.sendFeedback(message, false)
		return areas.size
	}
}
