package fr.bloctave.lmr

import com.mojang.brigadier.context.CommandContext
import fr.bloctave.lmr.data.requests.ClientRequest
import fr.bloctave.lmr.message.MessageRequestAdd
import fr.bloctave.lmr.message.MessageRequestDelete
import fr.bloctave.lmr.api.create.CreateProxy
import fr.bloctave.lmr.api.effortlessbuilding.EffortlessBuildingProxy
import fr.bloctave.lmr.api.holefiller.HoleFillerProxy
import fr.bloctave.lmr.api.iceandfire.IceAndFireProxy
import fr.bloctave.lmr.api.supplementaries.SupplementariesProxy
import fr.bloctave.lmr.command.LMCommand
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.command.argumentType.RequestArgument
import fr.bloctave.lmr.init.LMBlocks
import fr.bloctave.lmr.init.LMCapabilities
import fr.bloctave.lmr.init.LMItems
import fr.bloctave.lmr.message.*
import fr.bloctave.lmr.api.proxy.DependencyProxy

import fr.bloctave.lmr.api.vanilla.VanillaProxy
import fr.bloctave.lmr.config.ClientConfig
import fr.bloctave.lmr.config.CommonConfig
import fr.bloctave.lmr.config.ServerConfig

import fr.bloctave.lmr.util.*
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.MixinEnvironment
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.registerConfig


@Mod(LandManager.MOD_ID)
object LandManager {
	const val MOD_ID = "lmr"
	val LOGGER = LogManager.getLogger(LandManager::class.java)

	val group = object : ItemGroup(MOD_ID) {
		override fun makeIcon() = ItemStack(area_create!!)
	}

	private val dependencyProxy = DependencyProxy(MOD_ID)

	private val clientConfig = ClientConfig
	private val commonConfig = CommonConfig
	private val serverConfig = ServerConfig

	private val clientRequest: ClientRequest

	private const val NETWORK_PROTOCOL = "1"
	val NETWORK: SimpleChannel = NetworkRegistry.newSimpleChannel(
		ResourceLocation(MOD_ID, "main"),
		{ NETWORK_PROTOCOL },
		NETWORK_PROTOCOL::equals,
		NETWORK_PROTOCOL::equals
	).apply {
		arrayOf(
			MessageRequestAdd::class,
			MessageRequestDelete::class,
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
			MessageHomeActionReplyMessage::class,
			MessageHomeToggle::class,
			MessageHomeToggleReply::class,
			MessageOpenCreateAreaGui::class,
			MessageOpenHomeGui::class,
			MessageShowArea::class,
			MessageUpdateAreasCap::class
		).forEachIndexed { i, kClass -> registerMessage(kClass, i) }
	}

	/*val EVENT_NETWORK_CHANNEL = NetworkRegistry.newEventChannel(
		ResourceLocation(MOD_ID, "listener"),
		{ NETWORK_PROTOCOL },
		NETWORK_PROTOCOL::equals,
		NETWORK_PROTOCOL::equals
	).apply { it -> it.register(this, MessageAreaChange::class.java, 0) }*/

	init {

		MOD_BUS.apply {
			addListener<ModConfigEvent> {
				if (it.config.modId == MOD_ID) {
					/*clientConfig.register()
					commonConfig.register()
					serverConfig.register()*/
				}
			}
			addListener<FMLCommonSetupEvent> {
				it.enqueueWork { LMCapabilities.register() }
			}

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

		NETWORK_PROTOCOL



		regCommandArgType<AreaArgument>("area")
		regCommandArgType<RequestArgument>("request")


		registerConfig(ModConfig.Type.CLIENT, clientConfig.bakeConfig().second)
		registerConfig(ModConfig.Type.COMMON, commonConfig.bakeConfig().second)
		registerConfig(ModConfig.Type.SERVER, serverConfig.bakeConfig().second)

		MixinEnvironment.Side.CLIENT.run {
			clientRequest = ClientRequest()
		}

		dependencyProxy.addDependency(VanillaProxy)
		dependencyProxy.addDependency(IceAndFireProxy)
		dependencyProxy.addDependency(SupplementariesProxy)
		dependencyProxy.addDependency(HoleFillerProxy)
		dependencyProxy.addDependency(EffortlessBuildingProxy)
		dependencyProxy.addDependency(CreateProxy)

		dependencyProxy.registerDependencies()

		//MyPacketHandler()
		//EVENT_NETWORK_CHANNEL.addListener(this::handleEvent)

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
		val senderName = sender?.gameProfile?.name ?: server.name()
		server.playerList.ops.userList
			.mapNotNull { server.playerList.getPlayerByName(it) }
			.filter { it != sender }
			.forEach { NETWORK.sendToPlayer(MessageChatLog(timestamp, type, areaName, senderName), it) }
	}

	private fun areaChange(server: MinecraftServer, type: AreaChangeType, areaName: String, name: String) {
		val timestamp = System.currentTimeMillis()
		server.playerList.ops.userList
			.filter { !it.equals(name, true) }
			.mapNotNull { server.playerList.getPlayerByName(it) }
			.forEach { NETWORK.sendToPlayer(MessageChatLog(timestamp, type, areaName, name), it) }
	}

	fun getDependencyProxy(): DependencyProxy = dependencyProxy


	@SubscribeEvent
	fun handleEvent(event: NetworkEvent.ServerCustomPayloadEvent) = event.run {
		LOGGER.info("packet received")
	}

	fun getRequests(): ClientRequest {
		return clientRequest
	}
}
