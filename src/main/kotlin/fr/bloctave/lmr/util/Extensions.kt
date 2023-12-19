package fr.bloctave.lmr.util

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.data.areas.AreasCapability
import fr.bloctave.lmr.data.requests.ClientRequest
import fr.bloctave.lmr.data.requests.ServerRequest
import fr.bloctave.lmr.init.LMCapabilities
import net.minecraft.client.world.ClientWorld
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.ICommandSource
import net.minecraft.command.arguments.ArgumentSerializer
import net.minecraft.command.arguments.ArgumentTypes
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.management.PlayerProfileCache
import net.minecraft.util.Util
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.text.*
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.thread.EffectiveSide
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.simple.SimpleChannel
import net.minecraftforge.registries.ForgeRegistryEntry
import java.awt.Color
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/*
 * ----------------
 *  SIMPLE CHANNEL
 * ----------------
 */

/**
 * Registers a [Message] with the given [index]
 */
@Suppress("INACCESSIBLE_TYPE")
fun <T : Message> SimpleChannel.registerMessage(messageClass: KClass<T>, index: Int) {
	this.registerMessage(
		index,
		messageClass.java,
		{ message, buffer -> message.encode(buffer) },
		{ message -> messageClass.createInstance().apply { decode(message) } },
		{ message, context ->
			message.consume(context.get())
			context.get().packetHandled = true
		}
	)
}

/**
 * Sends the [message] to the [player] client
 */
fun SimpleChannel.sendToPlayer(message: Message, player: ServerPlayerEntity): Unit =
	this.send(PacketDistributor.PLAYER.with { player }, message)

/**
 * Sends the [message] to all clients
 */
fun SimpleChannel.sendToAll(message: Message): Unit = this.send(PacketDistributor.ALL.noArg(), message)

fun PacketBuffer.readColor(): Color? {
	val array = arrayOf(this.readInt(), this.readInt(), this.readInt(), this.readInt())
	return if (array.any { it < 0 }) null else Color(array[0], array[1], array[2], array[3])
}

/*
 * -----------------
 *  TEXT COMPONENTS
 * -----------------
 */

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [obj]. Same as calling
 * [IFormattableTextComponent.append] and giving it the result of calling [Any.toString] on [obj].
 */
fun IFormattableTextComponent.append(obj: Any): IFormattableTextComponent = this.append(obj.toString())

/**
 * Adds a new [TranslationTextComponent] to the end of the sibling list, with the specified translation key and
 * arguments. Same as calling [IFormattableTextComponent.append] with a new [TranslationTextComponent].
 */
fun IFormattableTextComponent.appendTranslation(translationKey: String, vararg args: Any): IFormattableTextComponent =
	this.append(TranslationTextComponent(translationKey, args))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [style].
 * Same as calling [IFormattableTextComponent.append] with a new [StringTextComponent] and calling
 * [IFormattableTextComponent.setStyle] on that.
 */
fun IFormattableTextComponent.appendStyledString(text: String, style: Style): IFormattableTextComponent =
	this.append(StringTextComponent(text).setStyle(style))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [styles].
 * Same as calling [IFormattableTextComponent.append] with a new [StringTextComponent] and calling
 * [IFormattableTextComponent.withStyle] on that.
 */
fun IFormattableTextComponent.appendStyledString(
	text: String,
	vararg styles: TextFormatting
): IFormattableTextComponent = this.append(StringTextComponent(text).withStyle(*styles))

/*
 * ----------
 *  COMMANDS
 * ----------
 */

/**
 * Registers all [commands] to this [CommandDispatcher]
 */
fun CommandDispatcher<CommandSource>.register(vararg commands: AbstractCommand): Unit =
	commands.forEach { this.register(it.builder) }

fun <T : ArgumentBuilder<CommandSource, T>> T.thenLiteral(
	name: String,
	block: LiteralArgumentBuilder<CommandSource>.() -> Unit
): T = this.then(Commands.literal(name).apply(block))

fun <T : ArgumentBuilder<CommandSource, T>, ARG> T.thenArgument(
	argumentName: String,
	argument: ArgumentType<ARG>,
	block: RequiredArgumentBuilder<CommandSource, ARG>.() -> Unit
): T = this.then(Commands.argument(argumentName, argument).apply(block))

fun <T : ArgumentBuilder<CommandSource, T>> T.thenCommand(command: AbstractCommand, block: T.() -> Unit = {}): T =
	this.then(command.builder).apply(block)

/**
 * Registers a new [ArgumentType] with the given [id]
 * Note that this method requires the [ArgumentType] [T] to be a Kotlin Object
 */
inline fun <reified T : ArgumentType<out Any>> regCommandArgType(id: String) {
	val instance =
		requireNotNull(T::class.objectInstance) { "The argument type ${T::class.qualifiedName} must be a Kotlin Object!" }
	ArgumentTypes.register(id, T::class.java, ArgumentSerializer { instance })
}

inline fun <reified T : Enum<T>> PacketBuffer.readEnumValue(): T = this.readEnum(T::class.java)

/*
 * -------
 *  SIDES
 * -------
 */

/**
 * Runs the [op] when on the [side]
 */
fun runWhenOnLogical(side: LogicalSide, op: () -> Unit) {
	if (EffectiveSide.get() == side)
		op()
}

/**
 * Runs the [op] when on [LogicalSide.CLIENT]
 */
fun runWhenOnLogicalClient(op: () -> Unit): Unit = runWhenOnLogical(LogicalSide.CLIENT, op)

/**
 * Runs the [op] when on [LogicalSide.SERVER]
 */
fun runWhenOnLogicalServer(op: () -> Unit): Unit = runWhenOnLogical(LogicalSide.SERVER, op)

/*
 * --------------------
 *  CAPABILITIES / WSD
 * --------------------
 */

val World.areasCap: AreasCapability
	get() = this.getCapability(LMCapabilities.AREAS)
		.orElseThrow { RuntimeException("Areas capability not found on world") }

val MinecraftServer.allAreaNames: List<String>
	get() = mutableListOf<String>().also { list ->
		this.allLevels.forEach {
			list.addAll(it.areasCap.getAllAreaNames())
		}
	}

fun MinecraftServer.getAreaNames(): Stream<String> = Stream.builder<String>().also { stream ->
	this.allLevels.forEach { world ->
		world.areasCap.getAllAreaNames().forEach { stream.accept(it) }
	}
}.build()

fun MinecraftServer.getArea(areaName: String): Area? {
	this.allLevels.forEach { world -> world.areasCap.getArea(areaName)?.let { return it } }
	return null
}

fun MinecraftServer.getAreas(filter: (Area) -> Boolean = { true }): Stream<Area> =
	Stream.builder<Area>().also { stream ->
		this.allLevels.forEach { world ->
			world.areasCap.getAllAreas().forEach {
				if (filter(it))
					stream.accept(it)
			}
		}
	}.build()

fun MinecraftServer.getWorldCapForArea(area: Area): AreasCapability? = this.getWorldCapForArea(area.name)

fun MinecraftServer.getWorldCapForArea(areaName: String): AreasCapability? {
	this.allLevels.forEach { world ->
		world.areasCap.let {
			if (it.hasArea(areaName))
				return it
		}
	}
	return null
}

/*val MinecraftServer.requests: RequestsWSD
	get() = RequestsWSD.get(this)*/

val MinecraftServer.requests : ServerRequest
	get() = ServerRequest.get(this)

val ClientWorld.requests : ClientRequest
	get() = LandManager.getRequests()



/*
 * ------
 *  MISC
 * ------
 */

fun <T : ForgeRegistryEntry<T>> T.setRegName(name: String): T =
	this.setRegistryName(LandManager.MOD_ID, name)

/**
 * Overload for [Entity.sendMessage] which uses [Util.DUMMY_UUID] instead of an explicit UUID
 */
fun Entity.sendMessage(textComponent: ITextComponent): Unit = this.sendMessage(textComponent, Util.NIL_UUID)

fun Entity.sendMessage(langKey: String, vararg args: Any): Unit =
	this.sendMessage(TranslationTextComponent(langKey, *args))

fun PlayerEntity.sendActionBarMessage(
	langKey: String,
	colour: TextFormatting? = null,
	vararg args: Any
): Unit =
	this.displayClientMessage(TranslationTextComponent(langKey, *args).apply { colour?.let { withStyle(it) } }, true)

fun PlayerEntity.sendAreaActionBarMessage(
	langKey: String,
	colour: TextFormatting? = null,
	area: String
): Unit =
	this.displayClientMessage(TranslationTextComponent(langKey).apply { colour?.let { withStyle(it) } }.append(StringTextComponent(area).withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD)), true)

fun AxisAlignedBB.minPos(): Vector3d = Vector3d(minX, minY, minZ)

fun AxisAlignedBB.maxPos(): Vector3d = Vector3d(maxX, maxY, maxZ)

fun BlockPos.toVec3d(): Vector3d = Vector3d(x.toDouble(), y.toDouble(), z.toDouble())

fun MinecraftServer.getUsernameFromUuid(uuid: UUID): String? = this.profileCache.get(uuid)?.name

fun MinecraftServer.sendToOps(message: ITextComponent, excluding: PlayerEntity? = null): Unit = this.playerList.players
	.filter { it != excluding && this.playerList.ops.get(it.gameProfile) != null }
	.mapNotNull { this.playerList.getPlayer(it.uuid) }
	.forEach { it.sendMessage(message, Util.NIL_UUID) }

fun ICommandSource.isOp(): Boolean {
	if (this !is ServerPlayerEntity)
		return false
	val server = this.level.server ?: return false
	if (this.gameProfile.name == server.singleplayerName)
		return true
	return server.playerList.ops.get(this.gameProfile) != null
}

fun ICommandSource?.canEditArea(area: Area?): Boolean =
	this != null && area != null && (this is MinecraftServer || (this is PlayerEntity && (area.isOwner(this.uuid) || this.isOp())))

fun ServerPlayerEntity?.canEditArea(area: Area?): Boolean {
	return this != null && area != null && (area.isOwner(this.uuid) || this.isOp())
}

val ServerPlayerEntity.username: String
	get() = this.gameProfile.name

fun PlayerProfileCache.hasUsername(username: String): Boolean = this.profilesByName.containsKey(username)

fun PlayerProfileCache.getProfileForUsername(username: String): GameProfile? =
	if (this.hasUsername(username.lowercase())) this.get(username) else null

fun CommandContext<CommandSource>.getSenderName(): String = when (val sender = this.source.source) {
	is PlayerEntity -> sender.gameProfile.name
	is Entity -> sender.name.string
	else -> this.source.server.name()
}


fun AxisAlignedBB.isNested(other: AxisAlignedBB): Boolean = this.contains(other) || other.contains(this)

fun AxisAlignedBB.contains(other: AxisAlignedBB): Boolean = this.contains(other.minX, other.maxX, other.minY, other.maxY, other.minZ, other.maxZ)

fun AxisAlignedBB.contains(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Boolean = (this.minX - this.maxX) + (this.minY - this.maxY) + (this.minZ - this.maxZ) < (x1 - x2) + (y1 - y2) + (z1 - z2)

val AxisAlignedBB.size: Double
	get() = this.xsize * this.ysize * this.zsize

var PlayerEntity.blockOwned: Double
	get() = 0.0
	set(value: Double) {
		this.persistentData.putDouble("blockOwned", value)
	}