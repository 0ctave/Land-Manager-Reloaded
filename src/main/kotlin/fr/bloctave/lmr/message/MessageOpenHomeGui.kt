package fr.bloctave.lmr.message


import fr.bloctave.lmr.gui.HomeScreen
import fr.bloctave.lmr.util.Message
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*

class MessageOpenHomeGui : Message {
    private lateinit var pos: BlockPos
    private var isOp: Boolean = false
    private var owner: Pair<String, UUID>? = null
    private lateinit var members: List<Pair<String, UUID>>

    @Suppress("unused")
    constructor()

    constructor(pos: BlockPos, isOp: Boolean, owner: Pair<String, UUID>?, members: List<Pair<String, UUID>>) {
        this.pos = pos
        this.isOp = isOp
        this.owner = owner
        this.members = members
    }

    override fun encode(buffer: PacketBuffer): Unit = buffer.run {
        writeBlockPos(pos)
        writeBoolean(isOp)
        writeBoolean(owner != null)
        owner?.let {
            writeUtf(it.first)
            writeUUID(it.second)
        }
        writeInt(members.size)
        members.forEach {
            writeUtf(it.first)
            writeUUID(it.second)
        }
    }

    override fun decode(buffer: PacketBuffer): Unit = buffer.run {
        pos = readBlockPos()
        isOp = readBoolean()
        if (readBoolean())
            owner = readUtf() to readUUID()
        members = Array<Pair<String, UUID>>(readInt()) {
            readUtf() to readUUID()
        }.toList()
    }

    override fun consume(context: NetworkEvent.Context) {
        context.enqueueWork {
            val mc = Minecraft.getInstance()
            val gui = HomeScreen(mc.player!!, pos)
            mc.setScreen(gui)
            gui.apply {
                setMembersData(owner, members)
                if (isOp)
                    setClientIsOp()
            }
        }
    }
}
