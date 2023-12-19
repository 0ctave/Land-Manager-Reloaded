package fr.bloctave.lmr.api.proxy

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.config.util.PreciseValue
import fr.bloctave.lmr.util.thenArgument
import fr.bloctave.lmr.util.thenLiteral
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent

class ProxyConfigCommand<T : PreciseValue<Any>>(proxy: SoftProxy<*>, name: String, value: T) : AbstractCommand(
    proxy.modid,
    {
        thenLiteral(name) {

            when (value.type) {
                Boolean::class -> {
                    executes { context ->
                        CommandProxyFactory.permissionCommand(
                            context,
                            {
                                it.getProxyConfig(proxy)?.setValue(name, (value() as Boolean).not())
                            },
                            {
                                TranslationTextComponent("lmr.command.permission.success", name).append(
                                    StringTextComponent(" ${(value() as Boolean).not()}").withStyle(TextFormatting.GOLD)
                                )
                            })
                    }
                    thenArgument("boolean", BoolArgumentType.bool()) {
                        executes { context ->
                            CommandProxyFactory.permissionCommand(
                                context,
                                {
                                    it.getProxyConfig(proxy)?.setValue(name, BoolArgumentType.getBool(context, "boolean"))
                                },
                                {
                                    TranslationTextComponent("lmr.command.permission.success", name).append(
                                        StringTextComponent(" ${BoolArgumentType.getBool(context, "boolean")}").withStyle(TextFormatting.GOLD)
                                    )
                                }
                            )
                        }
                    }
                }

                Int::class -> thenArgument("number", IntegerArgumentType.integer()) {
                    executes { context ->
                        CommandProxyFactory.permissionCommand(
                            context,
                            {
                                it.getProxyConfig(proxy)?.setValue(name, IntegerArgumentType.getInteger(context, "number"))
                            },
                            {
                                TranslationTextComponent("lmr.command.permission.success", name).append(
                                    StringTextComponent(" ${IntegerArgumentType.getInteger(context, "number")}").withStyle(TextFormatting.GOLD)
                                )
                            })
                    }
                }
                Double::class -> thenArgument("double", DoubleArgumentType.doubleArg()) {
                    executes { context ->
                        CommandProxyFactory.permissionCommand(
                            context,
                            {
                                it.getProxyConfig(proxy)?.setValue(name, DoubleArgumentType.getDouble(context, "double"))
                            },
                            {
                                TranslationTextComponent("lmr.command.permission.success", name).append(
                                    StringTextComponent(" ${DoubleArgumentType.getDouble(context, "double")}").withStyle(TextFormatting.GOLD)
                                )
                            })
                    }
                }
                else -> throw IllegalArgumentException("Unknown type")
            }

            /*executes { context ->
                val booleanValue = AreaArgument.get(context, CommandProxyFactory.AREA).getProxyConfig(proxy)
                    ?.getPreciseValue<T>(name)? ?: return@executes 0
                CommandProxyFactory.permissionCommand(
                    context,
                    {
                        it.getProxyConfig(proxy)?.toggleProperty(name)
                    },
                    {
                        TranslationTextComponent(
                            "lmr.command.${name}.success",
                            it.getProxyConfig(proxy)?.getBoolean(name),
                            it.name
                        )
                    }
                )
            }
            thenArgument()
            thenLiteral("true") {
                executes { context ->
                    CommandProxyFactory.permissionCommand(
                        context,
                        {
                            it.getProxyConfig(proxy)?.setProperty(name, true)
                        },
                        {
                            TranslationTextComponent(
                                "lmr.command.${name}.success",
                                it.getProxyConfig(proxy)?.getBoolean(name),
                                it.name
                            )
                        }
                    )
                }
            },*/
        }
    }
)