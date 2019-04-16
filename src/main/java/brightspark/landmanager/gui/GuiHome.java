package brightspark.landmanager.gui;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.handler.ClientEventHandler;
import brightspark.landmanager.message.MessageHomeAction;
import brightspark.landmanager.util.HomeGuiActionType;
import brightspark.landmanager.util.HomeGuiToggleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GuiHome extends LMGui
{
	private static final int PLAYER_LIST_SIZE = 4;

	private BlockPos pos;
	private Area area = null;
	private GuiTextField input;
	private int playerListStartIndex = 0;
	private List<Pair<UUID, String>> members;

	private List<ListButton> listButtons = new ArrayList<>(PLAYER_LIST_SIZE);
	private int selectedMemberIndex = -1;
	private ArrowButton upButton, downButton;
	private ActionButton addButton, kickButton, passButton;
	private List<ToggleButton> toggleButtons = new ArrayList<>(5);

	public GuiHome(World world, BlockPos pos)
	{
		super("gui_home", 162, 144);
		this.pos = pos;
		CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
		if(cap != null)
			area = cap.intersectingArea(pos);
	}

	/**
	 * Used by {@link brightspark.landmanager.message.MessageOpenHomeGui} to set the members data
	 */
	public void setMembersData(List<Pair<UUID, String>> members)
	{
		this.members = members;
	}

	public void addMember(UUID uuid, String player)
	{
		members.add(new ImmutablePair<>(uuid, player));
		members.sort(Comparator.comparing(Pair::getRight));
		updatePlayerList();
	}

	public void removeMember(UUID player)
	{
		members.removeIf(pair -> pair.getLeft().equals(player));
		playerListStartIndex = MathHelper.clamp(playerListStartIndex, 0, members.size() - PLAYER_LIST_SIZE);
		updatePlayerList();
	}

	public void setToggle(HomeGuiToggleType type, boolean state)
	{
		switch(type)
		{
			case INTERACTIONS:
				area.setInteractions(state);
				break;
			case PASSIVE_SPAWNS:
				area.setPassiveSpawning(state);
				break;
			case HOSTILE_SPAWNS:
				area.setHostileSpawning(state);
				break;
			case EXPLOSIONS:
				area.setExplosions(state);
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		input = new GuiTextField(0, fontRenderer, guiLeft + 99, guiTop + 15, 56, 10);
		input.setEnableBackgroundDrawing(false);

		for(int i = 0; i < PLAYER_LIST_SIZE; i++)
			listButtons.add(addButton(new ListButton(7, 16 + (i * 12))));

		upButton = addButton(new ArrowButton(97, 29, true));
		downButton = addButton(new ArrowButton(97, 52, false));

		addButton = addButton(new ActionButton(111, 29, "Add", HomeGuiActionType.ADD));
		kickButton = addButton(new ActionButton(111, 41, "Kick", HomeGuiActionType.KICK));
		passButton = addButton(new ActionButton(111, 53, "Pass", HomeGuiActionType.PASS));

		toggleButtons.add(addButton(new ToggleButton(HomeGuiToggleType.BOUNDARIES, 108, 70, false, ClientEventHandler.isAreaBeingRendered(area.getName()))));
		toggleButtons.add(addButton(new ToggleButton(HomeGuiToggleType.INTERACTIONS, 108, 84, LMConfig.permissions.interactions, area.canInteract())));
		toggleButtons.add(addButton(new ToggleButton(HomeGuiToggleType.PASSIVE_SPAWNS, 108, 98, LMConfig.permissions.passiveSpawning, area.canPassiveSpawn())));
		toggleButtons.add(addButton(new ToggleButton(HomeGuiToggleType.HOSTILE_SPAWNS, 108, 112, LMConfig.permissions.hostileSpawning, area.canHostileSpawn())));
		toggleButtons.add(addButton(new ToggleButton(HomeGuiToggleType.EXPLOSIONS, 108, 126, LMConfig.permissions.explosions, area.canExplosionsCauseDamage())));
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void drawText()
	{
		input.drawTextBox();

		int colour = 4210752;
		drawString(area.getName(), 6, 5, colour, false);

		drawString("Show Boundaries", 7, 72, colour, false);
		drawString("Interact Permission", 7, 86, colour, false);
		drawString("Passive Spawning", 7, 100, colour, false);
		drawString("Hostile Spawning", 7, 114, colour, false);
		drawString("Explosions", 7, 128, colour, false);
	}

	@Override
	public void updateScreen()
	{
		input.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);
		if(input.textboxKeyTyped(typedChar, keyCode))
			updateActionButtons();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if(button instanceof ListButton)
		{
			listButtons.forEach(listButton -> listButton.selected = false);
			ListButton listButton = (ListButton) button;
			listButton.selected = true;
			selectedMemberIndex = listButton.id;
		}
		else if(button instanceof ArrowButton)
		{
			int change = ((ArrowButton) button).isUp() ? -1 : 1;
			playerListStartIndex += change;
			updatePlayerList();
		}
		else if(button instanceof ActionButton)
		{
			LandManager.NETWORK.sendToServer(new MessageHomeAction(pos, ((ActionButton) button).type, members.get(selectedMemberIndex).getLeft()));
			selectedMemberIndex = -1;
		}
		else if(button instanceof ToggleButton)
		{
			ToggleButton toggleButton = (ToggleButton) button;
			toggleButton.isOn = !toggleButton.isOn;
			switch(toggleButton.type)
			{
				case BOUNDARIES:
					ClientEventHandler.setRenderArea(area.getName(), toggleButton.isOn);
					break;
				case INTERACTIONS:
				case PASSIVE_SPAWNS:
				case HOSTILE_SPAWNS:
				case EXPLOSIONS:
					//TODO: Send message to server to change
			}
		}
	}

	private boolean canScrollUp()
	{
		return playerListStartIndex > 0;
	}

	private boolean canScrollDown()
	{
		return members.size() > playerListStartIndex + PLAYER_LIST_SIZE;
	}

	private void updatePlayerList()
	{
		if(members == null)
			return;

		//Update members list
		int membersSize = members.size();
		for(int i = 0; i < PLAYER_LIST_SIZE; i++)
		{
			int playerListI = playerListStartIndex + i;
			ListButton button = listButtons.get(i);
			button.setPlayer(playerListI < membersSize ? members.get(playerListI) : null);
			button.selected = selectedMemberIndex == playerListI;
		}

		//Update arrows
		upButton.enabled = canScrollUp();
		downButton.enabled = canScrollDown();
	}

	private void updateActionButtons()
	{
		addButton.enabled = StringUtils.isNotBlank(input.getText());
		kickButton.enabled = passButton.enabled = selectedMemberIndex >= 0;
	}

	private class ListButton extends LMButton
	{
		public boolean selected = false;
		private UUID playerUuid = null;

		public ListButton(int x, int y)
		{
			super(x, y, 87, 11, 87, 144, null);
			textOffset = 1;
		}

		public void setPlayer(Pair<UUID, String> player)
		{
			if(player == null)
			{
				displayString = null;
				playerUuid = null;
				enabled = false;
			}
			else
			{
				displayString = player.getRight();
				playerUuid = player.getLeft();
				enabled = true;
			}
		}

		public UUID getPlayerUuid()
		{
			return playerUuid;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if(!visible || !enabled) return;
			this.hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			//Draw button
			if(selected)
			{
				mc.getTextureManager().bindTexture(image);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				drawTexturedModalRect(x, y, getIconX(), getIconY(), width, height);
			}
		}
	}

	private class ArrowButton extends LMButton
	{
		private boolean isUp;

		public ArrowButton(int x, int y, boolean isUp)
		{
			super(x, y, 10, 13, 162, 84, null);
			this.isUp = isUp;
		}

		public boolean isUp()
		{
			return isUp;
		}

		@Override
		protected int getIconY()
		{
			return isUp ? iconY : iconY + height;
		}
	}

	private class ActionButton extends LMButton
	{
		private final HomeGuiActionType type;

		public ActionButton(int x, int y, String buttonText, HomeGuiActionType type)
		{
			super(x, y, 45, 12, 162, 0, buttonText);
			this.type = type;
			textOffset = 12;
		}

		@Override
		protected int getIconY()
		{
			return iconY + (type.ordinal() * height);
		}
	}

	private class ToggleButton extends LMButton
	{
		public final HomeGuiToggleType type;
		public boolean locked;
		public boolean isOn;

		public ToggleButton(HomeGuiToggleType type, int x, int y, boolean locked, boolean isOn)
		{
			super(x, y, 12, 12, 162, 36, null);
			this.type = type;
			this.locked = locked;
			this.isOn = isOn;
		}

		@Override
		protected int getIconY()
		{
			int y = iconY;
			if(locked)
				y += height * 2;
			if(isOn)
				y += height;
			return y;
		}
	}
}
