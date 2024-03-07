package fr.bloctave.lmr.message

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.util.*
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeActionAdd : Message {
	private lateinit var pos: BlockPos
	private lateinit var name: String

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, name: String) {
		this.pos = pos
		this.name = name
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeUtf(name)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		name = readUtf()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val player = context.sender ?: return@enqueueWork
			val world = player.level
			val server = world.server ?: return@enqueueWork
			val cap = world.areasCap
			val area = cap.intersectingArea(pos)?: return@enqueueWork

			if (!player.canEditArea(area)) {
				sendError(player, "message.lmr.error.noPerm")
				return@enqueueWork
			}

			val profile = server.profileCache.getProfileForUsername(name) ?: run {
				sendError(player, "message.lmr.error.noPlayerName", name)
				return@enqueueWork
			}
			val uuid = profile.id
			if (!cap.canJoinArea(area, uuid)) {
				sendError(player, "message.lmr.error.cantJoin", name)
				return@enqueueWork
			}
			if (area.addMember(uuid)) {
				cap.dataChanged(area, AreaUpdateType.CHANGE)
				LandManager.NETWORK.sendToPlayer(MessageHomeActionReply(HomeGuiActionType.ADD, uuid, profile.name), player)
			} else
				sendError(player, "message.lmr.error.alreadyMember", name)
		}
	}

	private fun sendError(player: ServerPlayerEntity, langKey: String, vararg args: String) =
		LandManager.NETWORK.sendToPlayer(MessageHomeActionReplyMessage(langKey, 0xFF0000, *args), player)
}
