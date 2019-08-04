package chylex.hee.game.block
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.MIN_LEVEL
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.get
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockDeathFlowerDecaying : BlockEndPlant(), IBlockDeathFlowerDecaying{
	override fun createBlockState() = BlockStateContainer(this, LEVEL)
	
	override fun getMetaFromState(state: IBlockState) = state[LEVEL] - MIN_LEVEL
	override fun getStateFromMeta(meta: Int) = this.with(LEVEL, meta + MIN_LEVEL)
	
	override fun damageDropped(state: IBlockState) = state[LEVEL] - MIN_LEVEL
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_WITHERED
	
	override fun tickRate(world: World): Int{
		return implTickRate()
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		super.onBlockAdded(world, pos, state)
		implOnBlockAdded(world, pos)
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		super.updateTick(world, pos, state, rand)
		implUpdateTick(world, pos, state, rand)
	}
}
