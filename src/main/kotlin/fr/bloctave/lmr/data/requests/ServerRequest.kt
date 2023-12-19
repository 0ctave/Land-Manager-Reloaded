package fr.bloctave.lmr.data.requests

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.message.MessageRequestDelete
import fr.bloctave.lmr.util.sendToAll
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.server.MinecraftServer
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.common.util.Constants.NBT
import java.util.*
import kotlin.streams.toList

class ServerRequest : WorldSavedData(NAME), IRequestData {

    val FILE_ID = NAME

    companion object {

        const val NAME = LandManager.MOD_ID + "requests"

        fun get(server: MinecraftServer): ServerRequest =
            server.allLevels.first().dataStorage.computeIfAbsent(::ServerRequest, NAME)
    }

    private var nextId = 0
    private val requestsByArea = mutableMapOf<String, MutableSet<Request>>()
    private val requests = mutableSetOf<Request>()


    private fun hasRequest(areaName: String, playerUuid: UUID): Boolean =
        requestsByArea[areaName]?.any { it.areaName == areaName && it.playerUuid == playerUuid } ?: false

    override fun getById(id: Int): Request? = requests.find { it.id == id }

    fun getByAreaNameRegex(pattern: String): List<Request> {
        val regex = Regex(pattern)
        return requestsByArea.entries.stream()
            .filter { regex.matches(it.key) }
            .flatMap { it.value.stream() }
            .sorted(Comparator.comparingInt { it.id })
            .toList<Request>()
    }

    fun getByAreaName(areaName: String): MutableSet<Request> =
        requestsByArea.computeIfAbsent(areaName) { mutableSetOf() }

    override fun getAll(): Set<Request> {
        println(requests)
        return requests.toSet()
    }

    fun add(areaName: String, playerUuid: UUID): Request? {
        if (hasRequest(areaName, playerUuid))
            return null
        val request = Request(nextId++, areaName, playerUuid)
        println("YUUP $request $requests")
        requests.add(request)

        getByAreaName(areaName).add(request)

        setDirty()

        return request
    }

    fun delete(areaName: String?, requestId: Int): Boolean {
        val request = areaName?.let { getByAreaName(areaName).find { it.id == requestId } ?: return false }
            ?: requests.find { it.id == requestId }
            ?: return false

        requestsByArea.entries.find { it.key == request.areaName }?.value?.removeIf { it.id == requestId }
        requests.removeIf { it.id == requestId }
        setDirty()

        LandManager.NETWORK.sendToAll(MessageRequestDelete(request))

        return true
    }

    fun deleteAllForArea(areaName: String): Boolean {

        requests.forEach {
            if (it.areaName == areaName) {
                LandManager.NETWORK.sendToAll(MessageRequestDelete(it))
            }
        }

        val removed1 = requestsByArea.remove(areaName) != null
        val removed2 = requests.removeIf { it.areaName == areaName }
        val removedAny = removed1 || removed2
        if (removedAny)
            setDirty()

        return removedAny
    }

    override fun load(nbt: CompoundNBT) {
        nbt.run {
            nextId = getInt("nextId")
            requests.clear()
            requestsByArea.clear()
            nbt.getList("list", NBT.TAG_COMPOUND.toInt()).forEach {
                val request = Request(it as CompoundNBT)
                requests += request
                getByAreaName(request.areaName) += request
            }
        }
    }

    override fun save(nbt: CompoundNBT) = nbt.apply {
        putInt("nextId", nextId)
        put("list", ListNBT().apply { requests.forEach { add(it.serializeNBT()) } })
    }
}
