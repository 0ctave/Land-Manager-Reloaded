package fr.bloctave.lmr.command.argumentType

import fr.bloctave.lmr.util.requests
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import fr.bloctave.lmr.data.requests.ClientRequest
import fr.bloctave.lmr.data.requests.IRequestData
import fr.bloctave.lmr.data.requests.Request
import fr.bloctave.lmr.data.requests.ServerRequest
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.concurrent.CompletableFuture

object RequestArgument : LMCommandArgType<Request>(Request::class) {
	private val INVALID = DynamicCommandExceptionType { TranslationTextComponent("lmr.command.requests.invalid", it) }
	private val REQUEST_NOT_EXISTS =
		DynamicCommandExceptionType { TranslationTextComponent("lmr.command.requests.not_exist", it) }

	override fun parse(reader: StringReader): Request = reader.readUnquotedString().let {
		it.toIntOrNull() ?: throw INVALID.createWithContext(reader, it)
	}.let {
		getRequest(it)?: throw REQUEST_NOT_EXISTS.createWithContext(reader, it)
	}

	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions>? {
		getRequests().getAll().filter { it.id.toString().startsWith(builder.remaining) }
			.forEach { builder.suggest(it.id) }
		return builder.buildFuture()
	}

	private fun getRequest(id: Int): Request? = getRequests().getById(id)

	private fun getRequests(): IRequestData = if (ServerLifecycleHooks.getCurrentServer() != null)
		getRequestsServer()
	else
		getRequestsClient()

	private fun getRequestsServer(): ServerRequest = ServerLifecycleHooks.getCurrentServer().requests

	private fun getRequestsClient(): ClientRequest = Minecraft.getInstance().level!!.requests

}
