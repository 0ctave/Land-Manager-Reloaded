package brightspark.landmanager.block;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.message.MessageOpenHomeGui;
import brightspark.landmanager.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockHome extends Block
{
	public BlockHome()
	{
		super(Material.WOOD);
		setRegistryName("home");
		setTranslationKey("home");
		setCreativeTab(LandManager.LM_TAB);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote && world instanceof WorldServer && player instanceof EntityPlayerMP && !player.isSneaking())
		{
			CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
			if(cap == null)
			{
				LandManager.LOGGER.error("Failed to get areas capability from dimension {}", world.provider.getDimension());
				return true;
			}
			Area area = cap.intersectingArea(pos);
			if(area == null)
				player.sendMessage(new TextComponentTranslation("message.home.none"));
			else if(!area.isMember(player.getUniqueID()) && !Utils.isOp(world.getMinecraftServer(), player))
				player.sendMessage(new TextComponentTranslation("message.home.notMember"));
			else
				LandManager.NETWORK.sendTo(new MessageOpenHomeGui(pos, Utils.getAllPlayers(world.getMinecraftServer())), (EntityPlayerMP) player);
		}
		return true;
	}
}
