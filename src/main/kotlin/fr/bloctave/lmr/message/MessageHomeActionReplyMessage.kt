package fr.bloctave.lmr.message

import fr.bloctave.lmr.gui.HomeScreen
import fr.bloctave.lmr.util.Message
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeActionReplyMessage : Message {
	private lateinit var message: String
	private var color: Int? = null
	private lateinit var args: Array<out String>

	@Suppress("unused")
	constructor()

	constructor(message: String, color: Int, vararg args: String) {
		this.message = message
		this.color = color
		this.args = args
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeUtf(message)
		writeInt(color!!)
		writeInt(args.size)
		args.forEach { writeUtf(it) }
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		message = readUtf()
		color = readInt()
		args = Array(readInt()) { readUtf() }
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val gui = Minecraft.getInstance().screen
			if (gui is HomeScreen)
				gui.message = Pair(TranslationTextComponent(message, *args), color!!)
		}
	}
}
