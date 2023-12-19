package fr.bloctave.lmr.init

import fr.bloctave.lmr.block.HomeBlock
import fr.bloctave.lmr.util.setRegName
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraftforge.event.RegistryEvent
import thedarkcolour.kotlinforforge.forge.objectHolder

object LMBlocks {
	val HOME: Block by objectHolder("home")

	fun register(event: RegistryEvent.Register<Block>) = event.registry.register(
		HomeBlock(props(Material.WOOD).strength(2F, 5F).sound(SoundType.WOOD)).setRegName("home")
	)

	private fun props(material: Material): AbstractBlock.Properties = AbstractBlock.Properties.of(material)
}
