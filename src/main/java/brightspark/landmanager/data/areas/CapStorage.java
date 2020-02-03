package brightspark.landmanager.data.areas;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class CapStorage<T extends INBTSerializable<NBTTagCompound>> implements Capability.IStorage<T> {
	@Nullable
	@Override
	public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
		return instance.serializeNBT();
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
		instance.deserializeNBT((NBTTagCompound) nbt);
	}
}
