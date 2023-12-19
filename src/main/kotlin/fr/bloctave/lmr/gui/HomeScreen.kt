package fr.bloctave.lmr.gui

import com.mojang.blaze3d.matrix.MatrixStack
import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.gui.util.ListButton
import fr.bloctave.lmr.gui.util.ListWidget
import fr.bloctave.lmr.gui.util.PreciseScreen
import fr.bloctave.lmr.message.MessageHomeActionAdd
import fr.bloctave.lmr.message.MessageHomeActionKickOrPass
import fr.bloctave.lmr.message.MessageHomeToggle
import fr.bloctave.lmr.util.HomeGuiActionType
import fr.bloctave.lmr.util.HomeGuiActionType.*
import fr.bloctave.lmr.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import org.lwjgl.glfw.GLFW
import java.awt.Rectangle
import java.util.*

class HomeScreen(player: PlayerEntity, val pos: BlockPos) : PreciseScreen("Home", "gui_home", 162, 144) {
    companion object {
        private val ownerIcon = Rectangle(162, 110, 7, 7)
    }

    private var clientIsOp = false
    private var clientIsOwner = false
    val area: Area = player.level.areasCap.intersectingArea(pos)!!

    private lateinit var inputTextField: TextFieldWidget

    private var owner: Pair<String, UUID>? = null

    private lateinit var addButton: ActionButton
    private lateinit var kickButton: ActionButton
    private lateinit var passButton: ActionButton

    lateinit var playerList: ListWidget<UUID>
    lateinit var permissionList: ListWidget<Boolean>

    private fun isOwner(uuid: UUID): Boolean = owner?.second == uuid


    private fun updateActionButtons() {
        addButton.active = inputTextField.value.isNotBlank() && clientIsOwner
        kickButton.active =
            playerList.selectedMemberIndex >= 0 && playerList.members.getOrNull(playerList.selectedMemberIndex)?.let { !isOwner(it.second) } ?: false && clientIsOwner
        passButton.active = kickButton.active
    }
    fun clearInput() {
        inputTextField.value = ""
    }

    fun clearPlayerSelection() {
        playerList.selectedMemberIndex = -1
        playerList.update()
    }

    fun clearPermissionSelection() {
        permissionList.selectedMemberIndex = -1
        permissionList.update()
    }

        fun setClientIsOp() {
        clientIsOp = true
    }

    // Used by MessageOpenHomeGui to set the members data
    fun setMembersData(owner: Pair<String, UUID>?, members: List<Pair<String, UUID>>) {
        this.owner = owner
        members.toMutableList().apply { owner?.let { add(it) } }.forEach { this.playerList.addMember(it) }
    }

    private fun onActionButtonPress(button: ActionButton) {
        message = null

        when (button.type) {
            KICK, PASS -> {
                if (playerList.selectedMemberIndex < 0)
                    return
                val uuid = playerList.members[playerList.selectedMemberIndex].second
                if (!isOwner(uuid))
                    LandManager.NETWORK.sendToServer(MessageHomeActionKickOrPass(pos, button.type == PASS, uuid))
            }

            ADD -> addMember(inputTextField.value)
        }
    }

    private fun addMember(name: String) {
        if (name.isNotBlank())
            LandManager.NETWORK.sendToServer(MessageHomeActionAdd(pos, name))
    }


    override fun init() {
        super.init()

        clientIsOwner = area.isOwner(minecraft!!.player!!.uuid)

        inputTextField = object : TextFieldWidget(font, guiLeft + 99, guiTop + 15, 56, 10, StringTextComponent("")) {
            init {
                setBordered(false)
            }

            override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean =
                super.keyPressed(keyCode, scanCode, modifiers).also { if (it) updateActionButtons() }

            override fun charTyped(char: Char, keyCode: Int): Boolean {
                val result = super.charTyped(char, keyCode)
                if (result) {
                    this@HomeScreen.message = null
                    updateActionButtons()
                } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    addMember(value)
                }
                return result
            }

            override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
                val result = super.mouseClicked(mouseX, mouseY, mouseButton)
                if (result) {
                    playerList.buttons.forEach { it.setSelected(false) }
                    permissionList.buttons.forEach { it.setSelected(false) }

                    updateActionButtons()
                    this@HomeScreen.message = null
                } else if (mouseButton == 1) {
                    // Handle right clicking text field
                    value = ""
                    this@HomeScreen.message = null
                }
                return result
            }
        }
        children += inputTextField

        //listButtons = Array(PLAYER_LIST_SIZE) { addButton(PlayerListButton(7, 16 + (it * 12), it)) }


        /*upButton = addButton(ArrowButton(97, 29, true, permissionButtons))
        downButton = addButton(ArrowButton(97, 52, false, permissionButtons))
        updatePlayerList()*/

        playerList = ListWidget(this, 7, 15, 87, 11, 4, 97, 29, 10, PlayerListButton::class)
        playerList.buttons.forEach { addButton(it) }
        playerList.update()

        addButton(playerList.upButton)
        addButton(playerList.downButton)


        addButton = addButton(ActionButton(111, 29, "gui.lmr.home.add", ADD))
        kickButton = addButton(ActionButton(111, 41, "gui.lmr.home.kick", KICK))
        passButton = addButton(ActionButton(111, 53, "gui.lmr.home.pass", PASS))
        updateActionButtons()

        /*boundariesToggle = addButton(
            HomeToggleButton(
                6,
                70,
                ClientEventHandler.isAreaBeingRendered(area.name),
                BOUNDARIES
            ).apply { active = true })*/


        permissionList = ListWidget(this, 7, 85, 136, 11, 4, 146, 86, 6, PermissionListButton::class)
        permissionList.buttons.forEach { addButton(it) }

        addButton(permissionList.upButton)
        addButton(permissionList.downButton)

        area.permissions.forEach { (proxy, config) ->
            config.fields.forEach {
                if (it.value() is Boolean)
                    permissionList.addMember(Pair("${proxy}:${it.key}", it.value() as Boolean))

            }
        }

    }

    override fun tick() {
        super.tick()
        inputTextField.tick()
    }

    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {

        super.render(matrixStack, mouseX, mouseY, partialTicks)
        inputTextField.render(matrixStack, mouseX, mouseY, partialTicks)


        drawString(matrixStack, font, "Permissions", guiLeft + 7, guiTop + 73, 0xFFFFFF)

        message?.let {
            drawCenteredString(matrixStack, font, it.first, width / 2, guiTop - 15, it.second)
        }
    }


    inner class PlayerListButton(list: ListWidget<UUID>, id: Int) : ListButton<UUID>(this, list, id, 0, 144) {
        private var isOwner: Boolean = false

        init {
            active = true
            textOffset = 2
        }

        override fun onButtonPress() {
            clearPermissionSelection()
            super.onButtonPress()
            updateActionButtons()
        }


        override fun updateValue(value: Pair<String, UUID>?) {
            if (value == null || value.first.isBlank()) {
                message = StringTextComponent("")
                active = false
                isOwner = false
                setTooltip()
            } else {
                this.value = value.first
                message = StringTextComponent(this.value)
                active = true
                isOwner = isOwner(value.second)
                if (isOwner) {
                    textOffset = 12
                    setTooltip(
                        TranslationTextComponent("gui.lmr.home.owner").withStyle(TextFormatting.GOLD),
                        message
                    )
                } else {
                    textOffset = 1
                    setTooltip(message)
                }
            }
        }

        override fun getIconY(): Int {
            var y = iconY

            if (hasIcon)
                y += height

            return y
        }

        override fun drawText(matrixStack: MatrixStack, font: FontRenderer) {
            if (isOwner) {
                minecraft!!.textureManager.bind(textureResLoc)
                blit(
                    matrixStack,
                    x + 1,
                    y + 1,
                    ownerIcon.x.toFloat(),
                    ownerIcon.y.toFloat(),
                    ownerIcon.width,
                    ownerIcon.height,
                    textureWidth,
                    textureHeight
                )
            }
            drawStringWithMaxWidth(
                matrixStack,
                message,
                x + textOffset,
                y + (height - 8) / 2,
                85 - textOffset,
                getTextColour(),
                false
            )
        }
    }

    inner class PermissionListButton(list: ListWidget<Boolean>, id: Int) :
        ListButton<Boolean>(this, list, id, 0, 166) {

        private var isOn = false

        init {
            active = true
            textOffset = 2
        }

        override fun onButtonPress() {
            clearPlayerSelection()
            if (this.list.selectedMemberIndex == this.id + this.list.startIndex)
                if (this.value.isNotBlank()) {
                    val (proxy, type) = this.value.split(":")
                    LandManager.NETWORK.sendToServer(MessageHomeToggle(pos, proxy, type))
                }
            super.onButtonPress()

        }

        override fun updateValue(value: Pair<String, Boolean>?) {
            if (value != null && value.first.isNotBlank()) {
                this.value = value.first
                message = StringTextComponent(value.first.split(":")[1].uppercase())
                active = true
                isOn = !value.second
            }
            setTooltip(message)
        }

        override fun drawText(matrixStack: MatrixStack, font: FontRenderer) {
            drawStringWithMaxWidth(
                matrixStack,
                message,
                x + textOffset,
                y + (height - 8) / 2,
                width - textOffset,
                getTextColour(),
                false
            )
        }

        //override fun getTextColour(): Int = TEXT_COLOUR_TITLE


        override fun getIconY(): Int {
            var y = iconY

            if (hasIcon)
                y += height * 2

            if (isOn)
                y += height

            return y
        }
    }

    private inner class ActionButton(x: Int, y: Int, text: String, val type: HomeGuiActionType) :
        LMButton(x, y, 45, 12, 162, 0, I18n.get(text), { onActionButtonPress(it as ActionButton) }) {
        init {
            textOffset = 12
        }

        override fun getIconY(): Int = iconY + (type.ordinal * height)

        override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
            if (!active) {
                // Manually rendering text when inactive to avoid modifying the texture with inactive states
                drawText(matrixStack, Minecraft.getInstance().font)
            } else
                super.render(matrixStack, mouseX, mouseY, partialTicks)
        }
    }
}
