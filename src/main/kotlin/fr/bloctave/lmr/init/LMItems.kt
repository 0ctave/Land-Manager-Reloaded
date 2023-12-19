package fr.bloctave.lmr.init

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.item.AreaCreateItem
import fr.bloctave.lmr.util.setRegName
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent

object LMItems {
	fun register(event: RegistryEvent.Register<Item>) = event.registry.registerAll(
		AreaCreateItem(props().stacksTo(1)).setRegName("area_create"),
		blockItem(LMBlocks.HOME)
	)

	private fun props(): Item.Properties = Item.Properties().apply { tab(LandManager.group) }

	private fun blockItem(block: Block): Item = BlockItem(block, props()).setRegName(block.registryName!!.path)
}
