package fr.bloctave.landmanager.command.nonop

import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.message.MessageShowArea
import fr.bloctave.landmanager.util.sendToPlayer
import fr.bloctave.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object ShowCommand : AbstractCommand(
	"show",
	{
		// show
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(null), it.source.asPlayer())
			return@executes 1
		}
		thenArgument(AREA, AreaArgument) {
			// show <area>
			executes {
				val areaName = AreaArgument.get(it, AREA).name
				LandManager.NETWORK.sendToPlayer(MessageShowArea(areaName), it.source.asPlayer())
				it.source.sendFeedback(TranslationTextComponent("lm.command.show.showing", areaName), false)
				return@executes 1
			}
		}
	}
)
