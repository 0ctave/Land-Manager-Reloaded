package fr.bloctave.landmanager.command.nonop

import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.message.MessageShowArea
import fr.bloctave.landmanager.util.sendToPlayer
import net.minecraft.util.text.TranslationTextComponent

object ShowOffCommand : AbstractCommand(
	"showoff",
	{
		// showoff
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(""), it.source.asPlayer())
			it.source.sendFeedback(TranslationTextComponent("lm.command.showoff"), false)
			return@executes 1
		}
	}
)
