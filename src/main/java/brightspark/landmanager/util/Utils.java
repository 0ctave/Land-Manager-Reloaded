package brightspark.landmanager.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListOpsEntry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Utils
{
	public static boolean isOp(MinecraftServer server, ICommandSender sender)
	{
		if(!(sender instanceof EntityPlayer))
			return false;
		EntityPlayer player = (EntityPlayer) sender;
		if(player.getName().equals(server.getServerOwner()))
			return true;
		UserListOpsEntry op = server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
		return op != null;
	}

	public static List<String> getAllPlayerNames(MinecraftServer server)
	{
		List<String> players = new LinkedList<>();
		PlayerProfileCache profileCache = server.getPlayerProfileCache();
		for(String name : profileCache.getUsernames())
		{
			GameProfile profile = profileCache.getGameProfileForUsername(name);
			if(profile != null)
				players.add(profile.getName());
		}
		players.sort(Comparator.naturalOrder());
		return players;
	}

	public static List<Pair<UUID, String>> getAllPlayers(MinecraftServer server)
	{
		List<Pair<UUID, String>> players = new LinkedList<>();
		PlayerProfileCache profileCache = server.getPlayerProfileCache();
		for(String name : profileCache.getUsernames())
		{
			GameProfile profile = profileCache.getGameProfileForUsername(name);
			if(profile != null)
				players.add(new ImmutablePair<>(profile.getId(), profile.getName()));
		}
		players.sort(Comparator.comparing(Pair::getRight));
		return players;
	}
}
