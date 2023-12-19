package fr.bloctave.lmr.message


import fr.bloctave.lmr.data.requests.Request
import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.requests
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent


class MessageRequestAdd : Message {
    private lateinit var request: Request

    @Suppress("unused")
    constructor()

    constructor(request: Request) {
        this.request = request
    }

    override fun encode(buffer: PacketBuffer): Unit = buffer.run {
        writeNbt(request.serializeNBT())
    }

    override fun decode(buffer: PacketBuffer): Unit = buffer.run {
        request = Request(readNbt()!!)
    }

    override fun consume(context: NetworkEvent.Context) {
        context.enqueueWork {
            Minecraft.getInstance().level!!.requests.add(request)
        }
    }
}
