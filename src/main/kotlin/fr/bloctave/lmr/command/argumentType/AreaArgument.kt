package fr.bloctave.lmr.command.argumentType

import fr.bloctave.lmr.data.areas.Area
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import fr.bloctave.lmr.util.*
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

object AreaArgument : LMCommandArgType<Area>(Area::class) {
	private val AREA_NOT_EXISTS =
		DynamicCommandExceptionType { TranslationTextComponent("lmr.command.area.not_exist", it) }

	override fun parse(reader: StringReader): Area = reader.readUnquotedString().let {
		getArea(it) ?: throw AREA_NOT_EXISTS.createWithContext(reader, it)
	}

	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		getAreaNames().filter { Minecraft.getInstance().player.canEditArea(getArea(it)) && it.startsWith(builder.remaining) }
			.sorted(Comparator.naturalOrder())
			.forEach { builder.suggest(it) }
		return builder.buildFuture()
	}

	// Need to use an `if` statement rather than `?.let {} ?:` to avoid NoSuchMethodError on dedicated server
	private fun getArea(name: String): Area? = if (ServerLifecycleHooks.getCurrentServer() != null)
		getAreaServer(name)
	else
		getAreaClient(name)

	@OnlyIn(Dist.CLIENT)
	private fun getAreaClient(name: String): Area? = Minecraft.getInstance().level!!.areasCap.getArea(name)

	private fun getAreaServer(name: String): Area? = ServerLifecycleHooks.getCurrentServer().getArea(name)

	private fun getAreaNames(): Stream<String> =
		ServerLifecycleHooks.getCurrentServer()?.let { getAreaNamesServer() } ?: getAreaNamesClient()

	@OnlyIn(Dist.CLIENT)
	private fun getAreaNamesClient(): Stream<String> =
		Minecraft.getInstance().level!!.areasCap.getAllAreaNames().stream()

	private fun getAreaNamesServer(): Stream<String> = ServerLifecycleHooks.getCurrentServer().getAreaNames()
}
