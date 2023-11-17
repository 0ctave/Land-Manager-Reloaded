package fr.bloctave.landmanager.message

import fr.bloctave.landmanager.util.Message
import fr.bloctave.landmanager.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageUpdateAreasCap : Message {
	private lateinit var nbt: CompoundNBT

	@Suppress("unused")
	constructor()

	constructor(nbt: CompoundNBT) {
		this.nbt = nbt
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeCompoundTag(nbt)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		nbt = readCompoundTag()!!
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().world!!.areasCap.deserializeNBT(nbt)
		}
	}
}
