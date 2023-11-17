package fr.bloctave.landmanager.command.optional

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object PassivesCommand : AbstractCommand(
	"passives",
	{
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				LMCommand.permissionCommand(
					context,
					{// it.togglePassiveSpawning()
					},
					{ TranslationTextComponent("lm.command.passives.success", it.getProxyConfig(VanillaProxy).passiveSpawning, it.name)
					}
				)
			}
		}
	}
)
