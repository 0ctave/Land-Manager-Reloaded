package fr.bloctave.landmanager.proxy

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import fr.bloctave.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

class ProxyConfigCommand(proxy: SoftProxy<IProxyConfig<out IProxyConfig.ConfigBuilder>>, name: String): AbstractCommand(
    name,
    {
        thenArgument(LMCommand.AREA, AreaArgument) {
            executes { context ->
                LMCommand.permissionCommand(
                    context,
                    {it.getProxyConfig(proxy).toggleProperty(name)
                    },
                    { TranslationTextComponent("lm.command.${name}.success", it.getProxyConfig(VanillaProxy).explosion, it.name) }
                )
            }
        }
    }
)