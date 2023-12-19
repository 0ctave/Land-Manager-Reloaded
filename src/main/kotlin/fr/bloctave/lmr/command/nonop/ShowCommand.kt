package fr.bloctave.lmr.command.nonop

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.message.MessageShowArea
import fr.bloctave.lmr.util.sendToPlayer
import fr.bloctave.lmr.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object ShowCommand : AbstractCommand(
	"show",
	{
		// show
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(null), it.source.playerOrException)
			return@executes 1
		}
		thenArgument(AREA, AreaArgument) {
			// show <area>
			executes {
				val areaName = AreaArgument.get(it, AREA).name
				LandManager.NETWORK.sendToPlayer(MessageShowArea(areaName), it.source.playerOrException)
				it.source.sendSuccess(TranslationTextComponent("lmr.command.show.showing", areaName), false)
				return@executes 1
			}
		}
	}
)
