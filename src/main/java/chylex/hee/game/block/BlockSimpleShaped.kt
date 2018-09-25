package chylex.hee.game.block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

open class BlockSimpleShaped(builder: BlockSimple.Builder, private val aabb: AxisAlignedBB) : BlockSimple(builder){
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = aabb
	
	override fun isFullCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
}
