package fr.bloctave.lmr.message

import fr.bloctave.lmr.AreaCreationEvent
import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.data.areas.AddAreaResult
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.util.AreaChangeType
import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.areasCap
import fr.bloctave.lmr.util.sendToPlayer
import net.minecraft.network.PacketBuffer
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.network.NetworkEvent

class MessageCreateArea : Message {
	private lateinit var area: Area

	@Suppress("unused")
	constructor()

	constructor(area: Area) {
		this.area = area
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeNbt(area.serializeNBT())
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		area = Area(readNbt()!!)
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val player = context.sender ?: return@enqueueWork
			val world = player.level
			var result = AddAreaResult.INVALID
			if (area.dim == player.level.dimension().location() && area.minPos.y >= 0 && area.maxPos.y <= world.height) {
				val cap = world.areasCap
				when {
					!Area.validateName(area.name) -> result = AddAreaResult.INVALID_NAME
					cap.hasArea(area.name) -> result = AddAreaResult.NAME_EXISTS
					!cap.validAreaSize(area) -> result = AddAreaResult.INVALID_SIZE
					!cap.validAreaIntersections(area) -> result = AddAreaResult.AREA_INTERSECTS
					!MinecraftForge.EVENT_BUS.post(AreaCreationEvent(area)) -> {
						result = if (cap.addArea(area)) AddAreaResult.SUCCESS else AddAreaResult.NAME_EXISTS
						if (result == AddAreaResult.SUCCESS)
							LandManager.areaChange(
								(world as ServerWorld).server,
								AreaChangeType.CREATE,
								area.name,
								player
							)
					}
				}
			}
			LandManager.NETWORK.sendToPlayer(MessageCreateAreaReply(area.name, result), player)
		}
	}
}
