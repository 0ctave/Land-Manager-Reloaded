package fr.bloctave.lmr.message

import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.areasCap
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
		writeNbt(nbt)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		nbt = readNbt()!!
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().level!!.areasCap.deserializeNBT(nbt)
		}
	}
}
