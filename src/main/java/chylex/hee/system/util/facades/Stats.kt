package chylex.hee.system.util.facades
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.stats.Stat
import net.minecraft.stats.Stats

object Stats{
	fun harvestBlock(block: Block): Stat<Block> = Stats.BLOCK_MINED.get(block)
	fun craftItem(item: Item): Stat<Item> = Stats.ITEM_CRAFTED.get(item)
	fun useItem(item: Item): Stat<Item> = Stats.ITEM_USED.get(item)
	fun breakItem(item: Item): Stat<Item> = Stats.ITEM_BROKEN.get(item)
	
	val CAULDRON_FILLED get() = Stats.FILL_CAULDRON!!
	val CAULDRON_USED get() = Stats.USE_CAULDRON!!
	val FLOWER_POTTED get() = Stats.POT_FLOWER!!
	val OPEN_SHULKER_BOX get() = Stats.OPEN_SHULKER_BOX!!
}
