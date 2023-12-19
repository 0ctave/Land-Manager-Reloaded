package fr.bloctave.lmr.data.areas

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

class Position : INBTSerializable<CompoundNBT> {
	lateinit var dimension: ResourceLocation
	lateinit var position: BlockPos

	constructor(dimension: ResourceLocation, position: BlockPos) {
		this.dimension = dimension
		this.position = position
	}

	constructor(nbt: CompoundNBT) {
		deserializeNBT(nbt)
	}

	override fun serializeNBT() = CompoundNBT().apply {
		putString("dimension", dimension.toString())
		putLong("position", position.asLong())
	}

	override fun deserializeNBT(nbt: CompoundNBT) {
		dimension = ResourceLocation(nbt.getString("dimension"))
		position = BlockPos.of(nbt.getLong("position"))
	}
}
