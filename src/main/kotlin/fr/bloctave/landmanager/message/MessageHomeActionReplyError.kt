package fr.bloctave.landmanager.message

import fr.bloctave.landmanager.gui.HomeScreen
import fr.bloctave.landmanager.util.Message
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeActionReplyError : Message {
	private lateinit var errorMessage: String
	private lateinit var args: Array<out String>

	@Suppress("unused")
	constructor()

	constructor(errorMessage: String, vararg args: String) {
		this.errorMessage = errorMessage
		this.args = args
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeString(errorMessage)
		writeInt(args.size)
		args.forEach { writeString(it) }
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		errorMessage = readString()
		args = Array(readInt()) { readString() }
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val gui = Minecraft.getInstance().currentScreen
			if (gui is HomeScreen)
				gui.errorMessage = TranslationTextComponent(errorMessage, *args)
		}
	}
}
