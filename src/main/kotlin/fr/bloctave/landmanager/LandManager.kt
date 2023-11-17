package fr.bloctave.landmanager

import fr.bloctave.landmanager.command.LMCommand
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.command.argumentType.RequestArgument
import fr.bloctave.landmanager.init.LMBlocks
import fr.bloctave.landmanager.init.LMCapabilities
import fr.bloctave.landmanager.init.LMItems
import fr.bloctave.landmanager.message.*
import fr.bloctave.landmanager.util.*
import com.mojang.brigadier.context.CommandContext
import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireConfig
import fr.bloctave.landmanager.proxy.iceandfire.IceAndFireProxy
import fr.bloctave.landmanager.proxy.DependencyProxy
import fr.bloctave.landmanager.proxy.IProxyConfig
import fr.bloctave.landmanager.proxy.SoftProxy
import fr.bloctave.landmanager.proxy.vanilla.VanillaProxy
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.FileUtils
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.registerConfig

@Mod(LandManager.MOD_ID)
object LandManager {
	const val MOD_ID = "lmr"
	val LOGGER = LogManager.getLogger(LandManager::class.java)

	val group = object : ItemGroup(MOD_ID) {
		override fun createIcon() = ItemStack(area_create!!)
	}

	private val dependencyProxy = DependencyProxy(MOD_ID)

	private const val NETWORK_PROTOCOL = "1"
	val NETWORK: SimpleChannel = NetworkRegistry.newSimpleChannel(
		ResourceLocation(MOD_ID, "main"),
		{ NETWORK_PROTOCOL },
		NETWORK_PROTOCOL::equals,
		NETWORK_PROTOCOL::equals
	).apply {
		arrayOf(
			MessageAreaAdd::class,
			MessageAreaChange::class,
			MessageAreaDelete::class,
			MessageAreaRename::class,
			MessageChatLog::class,
			MessageCreateArea::class,
			MessageCreateAreaReply::class,
			MessageHomeActionAdd::class,
			MessageHomeActionKickOrPass::class,
			MessageHomeActionReply::class,
			MessageHomeActionReplyError::class,
			MessageHomeToggle::class,
			MessageHomeToggleReply::class,
			MessageOpenCreateAreaGui::class,
			MessageOpenHomeGui::class,
			MessageShowArea::class,
			MessageUpdateAreasCap::class
		).forEachIndexed { i, kClass -> registerMessage(kClass, i) }
	}

	init {
		MOD_BUS.apply {
			addListener<ModConfigEvent> { if (it.config.modId == MOD_ID) LMConfig.bake() }
			addListener<FMLCommonSetupEvent> { it.enqueueWork { LMCapabilities.register() } }

			addGenericListener(LMBlocks::register)
			addGenericListener(LMItems::register)
		}
		FORGE_BUS.apply {
			addGenericListener(LMCapabilities::attach)
			addListener(LMCapabilities::playerLoggedIn)
			addListener(LMCapabilities::playerRespawn)
			addListener(LMCapabilities::playerChangedDimension)
			addListener<RegisterCommandsEvent> { it.dispatcher.register(LMCommand) }
		}

		regCommandArgType<AreaArgument>("area")
		regCommandArgType<RequestArgument>("request")


		registerConfig(ModConfig.Type.CLIENT, LMConfig.CLIENT_SPEC)
		registerConfig(ModConfig.Type.COMMON, LMConfig.COMMON_SPEC)
		registerConfig(ModConfig.Type.SERVER, LMConfig.SERVER_SPEC)


		dependencyProxy.addDependency(VanillaProxy)
		dependencyProxy.addDependency(IceAndFireProxy)

		dependencyProxy.registerDependencies()
	}

	fun areaChange(context: CommandContext<CommandSource>, type: AreaChangeType, areaName: String) =
		areaChange(context.source.server, type, areaName, context.getSenderName())

	fun areaChange(
		server: MinecraftServer,
		type: AreaChangeType,
		areaName: String,
		sender: ServerPlayerEntity? = null
	) {
		val timestamp = System.currentTimeMillis()
		val senderName = sender?.gameProfile?.name ?: server.name
		server.playerList.oppedPlayers.keys
			.mapNotNull { server.playerList.getPlayerByUsername(it) }
			.filter { it != sender }
			.forEach { NETWORK.sendToPlayer(MessageChatLog(timestamp, type, areaName, senderName), it) }
	}

	private fun areaChange(server: MinecraftServer, type: AreaChangeType, areaName: String, name: String) {
		val timestamp = System.currentTimeMillis()
		server.playerList.oppedPlayers.keys
			.filter { !it.equals(name, true) }
			.mapNotNull { server.playerList.getPlayerByUsername(it) }
			.forEach { NETWORK.sendToPlayer(MessageChatLog(timestamp, type, areaName, name), it) }
	}

	fun getDependencyProxy(): DependencyProxy = dependencyProxy

}
