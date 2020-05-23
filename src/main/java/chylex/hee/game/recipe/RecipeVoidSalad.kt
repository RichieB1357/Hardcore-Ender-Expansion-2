package chylex.hee.game.recipe
import chylex.hee.game.item.ItemVoidSalad.Type
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.nonEmptySlots
import com.google.common.collect.Iterators
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object RecipeVoidSalad : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return width == 3 && height >= 2
	}
	
	override fun matches(inv: CraftingInventory, world: World): Boolean{
		val bowlRow = findBowlRow(inv)
		
		return (
			bowlRow != null &&
			getStackInRowAndColumn(inv, bowlRow - 1, 0).let(::isValidFood) &&
			getStackInRowAndColumn(inv, bowlRow - 1, 1).item === ModItems.VOID_ESSENCE &&
			getStackInRowAndColumn(inv, bowlRow - 1, 2).let(::isValidFood) &&
			Iterators.size(inv.nonEmptySlots) == 4
		)
	}
	
	override fun getCraftingResult(inv: CraftingInventory): ItemStack{
		val bowlRow = findBowlRow(inv) ?: return ItemStack.EMPTY
		
		val isLeftVoidSalad = isSingleVoidSalad(getStackInRowAndColumn(inv, bowlRow - 1, 0))
		val isRightVoidSalad = isSingleVoidSalad(getStackInRowAndColumn(inv, bowlRow - 1, 2))
		
		return when{
			isLeftVoidSalad && isRightVoidSalad -> ItemStack(ModItems.VOID_SALAD).also { ModItems.VOID_SALAD.setSaladType(it, Type.MEGA) }
			isLeftVoidSalad || isRightVoidSalad -> ItemStack(ModItems.VOID_SALAD).also { ModItems.VOID_SALAD.setSaladType(it, Type.DOUBLE) }
			else                                -> ItemStack(ModItems.VOID_SALAD).also { ModItems.VOID_SALAD.setSaladType(it, Type.SINGLE) }
		}
	}
	
	private fun findBowlRow(inv: CraftingInventory): Int?{
		return (0 until inv.height).find { row -> getStackInRowAndColumn(inv, row, 1).item === Items.BOWL }
	}
	
	private fun isValidFood(stack: ItemStack): Boolean{
		return stack.item.isFood && (stack.item !== ModItems.VOID_SALAD || isSingleVoidSalad(stack))
	}
	
	private fun isSingleVoidSalad(stack: ItemStack): Boolean{
		return stack.item === ModItems.VOID_SALAD && ModItems.VOID_SALAD.getSaladType(stack) == Type.SINGLE
	}
}
