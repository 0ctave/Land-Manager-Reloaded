package brightspark.landmanager;

import brightspark.landmanager.data.CapStorage;
import brightspark.landmanager.data.CapabilityAreas;
import brightspark.landmanager.data.CapabilityAreasImpl;
import brightspark.landmanager.gui.GuiHandler;
import brightspark.landmanager.item.LMItems;
import brightspark.landmanager.message.MessageCreateArea;
import brightspark.landmanager.message.MessageCreateAreaReply;
import brightspark.landmanager.message.MessageUpdateCapability;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = LandManager.MOD_ID, name = LandManager.MOD_NAME, version = LandManager.VERSION)
public class LandManager
{
    public static final String MOD_ID = "landmanager";
    public static final String MOD_NAME = "Land Manager";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static LandManager INSTANCE;
    public static Logger LOGGER;
    public static SimpleNetworkWrapper NETWORK;

    @CapabilityInject(CapabilityAreas.class)
    public static Capability<CapabilityAreas> CAPABILITY_AREAS = null;

    public static final CreativeTabs LM_TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(LMItems.adminItem);
        }
    };

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        NETWORK.registerMessage(new MessageCreateArea.Handler(), MessageCreateArea.class, 0, Side.SERVER);
        NETWORK.registerMessage(new MessageCreateAreaReply.Handler(), MessageCreateAreaReply.class, 1, Side.CLIENT);
        NETWORK.registerMessage(new MessageUpdateCapability.Handler(), MessageUpdateCapability.class, 2, Side.CLIENT);

        CapabilityManager.INSTANCE.register(CapabilityAreas.class, new CapStorage<>(), CapabilityAreasImpl::new);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        //TODO: Commands
    }
}