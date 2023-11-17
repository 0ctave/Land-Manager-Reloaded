package fr.bloctave.landmanager.message

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.data.areas.AreaUpdateType
import fr.bloctave.landmanager.data.areas.AreasCapability
import fr.bloctave.landmanager.util.*
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeToggle : Message {
	private lateinit var pos: BlockPos
	private lateinit var type: HomeGuiToggleType

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, type: HomeGuiToggleType) {
		this.pos = pos
		this.type = type
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeEnumValue(type)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		type = readEnumValue()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val player = context.sender ?: return@enqueueWork
			val world = player.world
			val cap = world.areasCap
			val area = cap.intersectingArea(pos) ?: return@enqueueWork
			if (!player.canEditArea(area))
				return@enqueueWork
			val playerIsOp = player.isOp()
			/*when (type) {
				HomeGuiToggleType.INTERACTIONS -> handleType(
					cap,
					player,
					area,
					playerIsOp,
					LMConfig.interactions,
					{ it.toggleInteractions() },
					{ it.interactions })
				HomeGuiToggleType.PASSIVES -> handleType(
					cap,
					player,
					area,
					playerIsOp,
					LMConfig.passiveSpawning,
					{ it.togglePassiveSpawning() },
					{ it.canPassiveSpawn })
				HomeGuiToggleType.HOSTILES -> handleType(
					cap,
					player,
					area,
					playerIsOp,
					LMConfig.hostileSpawning,
					{ it.toggleHostileSpawning() },
					{ it.canHostileSpawn })
				HomeGuiToggleType.EXPLOSIONS -> handleType(cap, player, area, playerIsOp, LMConfig.explosions, { it.toggleExplosions() }, { it.explosions })
				else -> Unit
			}*/
		}
	}

	private fun handleType(
		cap: AreasCapability,
		player: ServerPlayerEntity,
		area: Area,
		playerIsOp: Boolean,
		config: Boolean,
		toggle: (Area) -> Unit,
		stateGetter: (Area) -> Boolean
	) {
		if (!playerIsOp && config)
			return
		toggle(area)
		cap.dataChanged(area, AreaUpdateType.CHANGE)
		//LandManager.NETWORK.sendToPlayer(MessageHomeToggleReply(type, stateGetter(area)), player)
	}
}
