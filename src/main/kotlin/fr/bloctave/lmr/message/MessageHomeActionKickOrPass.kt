package fr.bloctave.lmr.message

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.util.*
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*

class MessageHomeActionKickOrPass : Message {
	private lateinit var pos: BlockPos
	private var isPass: Boolean = false
	private lateinit var uuid: UUID

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, isPass: Boolean, uuid: UUID) {
		this.pos = pos
		this.isPass = isPass
		this.uuid = uuid
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeBoolean(isPass)
		writeUUID(uuid)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		isPass = readBoolean()
		uuid = readUUID()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val player = context.sender ?: return@enqueueWork
			val world = player.level
			val server = world.server ?: return@enqueueWork
			val cap = world.areasCap
			val area = cap.intersectingArea(pos)
			if (!player.canEditArea(area)) {
				LandManager.NETWORK.sendToPlayer(
					MessageHomeActionReplyMessage("message.lmr.error.noPerm", 0xFF0000),
					player
				)
				return@enqueueWork
			}
			val profile = server.profileCache.get(uuid) ?: run {
				LandManager.NETWORK.sendToPlayer(
					MessageHomeActionReplyMessage("message.lmr.error.noPlayer", 0xFF0000),
					player
				)
				return@enqueueWork
			}

			var changed = true
			if (isPass) {
				val oldOwner = area!!.owner
				area.owner = uuid
				area.removeMember(uuid)
				oldOwner?.let { area.addMember(oldOwner) }
			} else {
				changed = area!!.removeMember(uuid)
				if (changed)
					cap.decreasePlayerAreasNum(uuid)
			}
			if (changed) {
				cap.dataChanged(area, AreaUpdateType.CHANGE)
				LandManager.NETWORK.sendToPlayer(MessageHomeActionReply(if (isPass) HomeGuiActionType.PASS else HomeGuiActionType.KICK, player.uuid, profile.name), player)
			}
		}
	}
}
