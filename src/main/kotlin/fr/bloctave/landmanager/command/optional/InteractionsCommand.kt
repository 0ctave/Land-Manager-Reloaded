package fr.bloctave.landmanager.command.optional

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object InteractionsCommand : AbstractCommand(
	"interactions",
	{
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				LMCommand.permissionCommand(
					context,
					{ //it.toggleInteractions()
					},
					{ TranslationTextComponent("lm.command.interactions.success", it.getProxyConfig(VanillaProxy).interaction, it.name) }
				)
			}
		}
	}
)
