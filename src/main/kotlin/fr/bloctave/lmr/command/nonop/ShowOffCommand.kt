package fr.bloctave.lmr.command.nonop

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.message.MessageShowArea
import fr.bloctave.lmr.util.sendToPlayer
import net.minecraft.util.text.TranslationTextComponent

object ShowOffCommand : AbstractCommand(
	"showoff",
	{
		// showoff
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(""), it.source.playerOrException)
			it.source.sendSuccess(TranslationTextComponent("lmr.command.showoff"), false)
			return@executes 1
		}
	}
)
