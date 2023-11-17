package fr.bloctave.landmanager.data.areas

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.proxy.IProxyConfig
import fr.bloctave.landmanager.proxy.SoftProxy
import fr.bloctave.landmanager.util.Cached
import fr.bloctave.landmanager.util.toVec3d
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import org.apache.commons.lang3.builder.EqualsBuilder
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class Area : INBTSerializable<CompoundNBT> {
    companion object {
        private val AREA_NAME = Regex("^\\w+\$")

        fun validateName(areaName: String): Boolean = AREA_NAME.matches(areaName)
    }

    @set:JvmName("_setName")
    lateinit var name: String
        private set
    lateinit var dim: ResourceLocation
        private set
    lateinit var minPos: BlockPos
        private set
    lateinit var maxPos: BlockPos
        private set
    var owner: UUID? = null
    val members = mutableSetOf<UUID>()


    val permissions: HashMap<String, IProxyConfig<out IProxyConfig.ConfigBuilder>> = HashMap()


    /*var canPassiveSpawn = LMConfig.canPassiveSpawn
    var canHostileSpawn = LMConfig.canHostileSpawn
    var explosions = LMConfig.explosions
    var interactions = LMConfig.interactions*/


    var collisionAabb: Cached<AxisAlignedBB> =
        Cached { AxisAlignedBB(minPos.toVec3d().add(0.4, 0.4, 0.4), maxPos.toVec3d().add(0.6, 0.6, 0.6)) }
        private set
    var displayAabb: Cached<AxisAlignedBB> =
        Cached { AxisAlignedBB(minPos.toVec3d().subtract(0.1, 0.1, 0.1), maxPos.toVec3d().add(1.1, 1.1, 1.1)) }
        private set

    constructor(name: String, dimension: ResourceLocation, position1: BlockPos, position2: BlockPos) : this() {
        this.name = name
        dim = dimension
        minPos = BlockPos(
            min(position1.x, position2.x),
            min(position1.y, position2.y),
            min(position1.z, position2.z)
        )
        maxPos = BlockPos(
            max(position1.x, position2.x),
            max(position1.y, position2.y),
            max(position1.z, position2.z)
        )
    }

    constructor(nbt: CompoundNBT) : this() {
        LandManager.getDependencyProxy().getDependencies().forEach {
            permissions[it.key] = it.value.createConfig()
            println("Added ${it.key} to permissions")
        }

        deserializeNBT(nbt)
    }

    constructor() {

    }


    fun setName(name: String): Boolean = if (validateName(name)) {
        this.name = name
        true
    } else {
        false
    }

    fun isOwner(playerUuid: UUID): Boolean = owner == playerUuid

    fun addMember(playerUuid: UUID): Boolean = members.add(playerUuid)

    fun removeMember(playerUuid: UUID): Boolean = members.remove(playerUuid)

    fun isMember(playerUuid: UUID): Boolean = isOwner(playerUuid) || members.contains(playerUuid)

    /*fun togglePassiveSpawning() {
        canPassiveSpawn = canPassiveSpawn.not()
    }

    fun toggleHostileSpawning() {
        canHostileSpawn = canHostileSpawn.not()
    }

    fun toggleExplosions() {
        explosions = explosions.not()
    }

    fun toggleInteractions() {
        interactions = interactions.not()
    }*/

    fun intersects(aabb: AxisAlignedBB): Boolean = collisionAabb.get().intersects(aabb)

    fun intersects(area: Area): Boolean = intersects(area.collisionAabb.get())

    fun intersects(pos: Vector3d): Boolean = collisionAabb.get().contains(pos)

    fun intersects(pos: BlockPos): Boolean = intersects(pos.toVec3d().add(0.5, 0.5, 0.5))

    fun extendToMinMaxY(world: World) {
        minPos = BlockPos(minPos.x, 0, minPos.z)
        maxPos = BlockPos(maxPos.x, world.height, maxPos.z)
        collisionAabb.clear()
        displayAabb.clear()
    }

    fun closestPosTo(pos: BlockPos): BlockPos = BlockPos(
        MathHelper.clamp(pos.x, minPos.x, maxPos.x),
        MathHelper.clamp(pos.y, minPos.y, maxPos.y),
        MathHelper.clamp(pos.z, minPos.z, maxPos.z)
    )

    override fun serializeNBT(): CompoundNBT = CompoundNBT().apply {
        putString("name", name)
        putString("dimension", dim.toString())
        putLong("position1", minPos.toLong())
        putLong("position2", maxPos.toLong())
        owner?.let { putUniqueId("player", it) }
        if (members.isNotEmpty()) {
            put("members", ListNBT().apply {
                members.forEach {
                    add(CompoundNBT().apply { putUniqueId("uuid", it) })
                }
            })
        }

        /*putBoolean("passive", canPassiveSpawn)
        putBoolean("hostile", canHostileSpawn)
        putBoolean("explosions", explosions)
        putBoolean("interact", interactions)*/

        permissions.onEach { (modid, config) ->
            put(modid, CompoundNBT().apply { config.serializeNBT() })
        }
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        name = nbt.getString("name")
        dim = ResourceLocation(nbt.getString("dimension"))
        minPos = BlockPos.fromLong(nbt.getLong("position1"))
        maxPos = BlockPos.fromLong(nbt.getLong("position2"))
        collisionAabb.clear()
        displayAabb.clear()
        if (nbt.hasUniqueId("player"))
            owner = nbt.getUniqueId("player")
        members.clear()
        if (nbt.contains("members")) {
            nbt.getList("members", Constants.NBT.TAG_COMPOUND).forEach {
                members.add((it as CompoundNBT).getUniqueId("uuid"))
            }
        }


        /*canPassiveSpawn = nbt.getBoolean("passive")
        canHostileSpawn = nbt.getBoolean("hostile")
        explosions = nbt.getBoolean("explosions")
        interactions = nbt.getBoolean("interact")*/


        permissions.onEach { (modid, config) -> nbt.getCompound(modid).let { config.deserializeNBT(it) } }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other !is Area) return false
        return EqualsBuilder()
            .append(name, other.name)
            .append(dim, other.dim)
            .append(minPos, other.minPos)
            .append(maxPos, other.maxPos)
            .isEquals
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + dim.hashCode()
        return result
    }

    inline fun <reified C : IProxyConfig<out IProxyConfig.ConfigBuilder>> getProxyConfig(proxy: SoftProxy<C>): C {
        return permissions.values.find { it::class == proxy.getConfigClass() } as C
    }
}
