package chylex.hee.game.mechanics.potion.brewing.modifiers
import chylex.hee.game.mechanics.potion.brewing.IBrewingModifier
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object BrewIncreaseLevel : IBrewingModifier{
	override val ingredient = Items.GLOWSTONE_DUST!!
	
	override fun check(input: ItemStack): Boolean{
		return PotionBrewing.unpack(input)?.canIncreaseLevel == true
	}
	
	override fun apply(input: ItemStack): ItemStack{
		return PotionBrewing.unpack(input)!!.withIncreasedLevel
	}
}