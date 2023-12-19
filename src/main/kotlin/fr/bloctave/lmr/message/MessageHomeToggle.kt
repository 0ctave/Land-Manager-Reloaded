package fr.bloctave.lmr.message

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.config.util.IProxyAreaConfig
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.areasCap
import fr.bloctave.lmr.util.canEditArea
import fr.bloctave.lmr.util.sendToPlayer
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeToggle : Message {
	private lateinit var pos: BlockPos
	private lateinit var proxy: String
	private lateinit var type: String

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, proxy: String, type: String) {
		this.pos = pos
		this.proxy = proxy
		this.type = type
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeUtf(proxy)
		writeUtf(type)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		proxy = readUtf()
		type = readUtf()

	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {

			val player = context.sender ?: return@enqueueWork
			val world = player.level
			val cap = world.areasCap
			val area = cap.intersectingArea(pos) ?: return@enqueueWork
			if (!player.canEditArea(area)) {
				sendError(player, "message.lmr.error.noPerm", area.name)
				return@enqueueWork
			}

			val config = area.getProxyConfig(LandManager.getDependencyProxy().getProxy(proxy)!!) ?: return@enqueueWork
			val preciseValue = config.fields[type] ?: return@enqueueWork
			if (preciseValue.type != Boolean::class)
				return@enqueueWork
			val oldBooleanValue = preciseValue() as Boolean


			config.setValue(type, (preciseValue() as Boolean).not())
			handleToggle(
				world,
				player,
				area,
				config,
				oldBooleanValue)
		}
	}

	private fun handleToggle(
		world: World,
		player: ServerPlayerEntity,
		area: Area,
		config: IProxyAreaConfig,
		value: Boolean,
	) {
		config.setValue(type, !value)

		/*for (playerInArea in area.members) {
			val player =
			LandManager.NETWORK.sendToPlayer(MessageHomeToggleReply(proxy, type, !value), world.getPlayerByUUID(playerInArea)?)
		}*/
		//TODO Send area modification to other players
		LandManager.NETWORK.sendToPlayer(MessageHomeToggleReply(proxy, type, !value), player)
		sendMessage(player, "message.lmr.info.toggle", type, area.name, (!value).toString())

	}

	private fun sendError(player: ServerPlayerEntity, langKey: String, vararg args: String) =
		LandManager.NETWORK.sendToPlayer(MessageHomeActionReplyMessage(langKey, 0xFF0000, *args), player)

	private fun sendMessage(player: ServerPlayerEntity, langKey: String, vararg args: String) =
		LandManager.NETWORK.sendToPlayer(MessageHomeActionReplyMessage(langKey, 0x00FF00, *args), player)
}
