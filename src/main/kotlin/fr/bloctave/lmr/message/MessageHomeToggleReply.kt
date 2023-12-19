package fr.bloctave.lmr.message

import fr.bloctave.lmr.gui.HomeScreen
import fr.bloctave.lmr.handler.ClientEventHandler
import fr.bloctave.lmr.util.Message
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeToggleReply : Message {
	private lateinit var proxy: String
	private lateinit var type: String
	private var state: Boolean = false

	@Suppress("unused")
	constructor()

	constructor(proxy : String, type: String, state: Boolean) {
		this.type = type
		this.proxy = proxy
		this.state = state
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeUtf(proxy)
		writeUtf(type)
		writeBoolean(state)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		proxy = readUtf()
		type = readUtf()
		state = readBoolean()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val gui = Minecraft.getInstance().screen

			if (gui is HomeScreen) {

				if (type == "showArea") {
					ClientEventHandler.setRenderArea(gui.area.name, state)
				}

				val index =
                    gui.permissionList.members.indexOf(gui.permissionList.members.firstOrNull { it.first == "$proxy:$type" })// as HomeScreen.PermissionListButton
				gui.permissionList.members[index] = Pair("$proxy:$type", state)
				gui.permissionList.update()
			//button.isOn = state
			}
		}
	}
}
