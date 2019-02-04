package chylex.hee.game.world.structure
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.MutableBlockPos
import java.util.Random

interface IStructureWorld{
	val rand: Random
	
	fun getState(pos: BlockPos): IBlockState
	fun setState(pos: BlockPos, state: IBlockState)
	
	fun addTrigger(pos: BlockPos, trigger: IStructureTrigger)
	
	fun finalize()
	
	// Utilities
	
	@JvmDefault fun getBlock(pos: BlockPos): Block{
		return getState(pos).block
	}
	
	@JvmDefault fun setBlock(pos: BlockPos, block: Block){
		setState(pos, block.defaultState)
	}
	
	@JvmDefault fun setAir(pos: BlockPos){
		setState(pos, Blocks.AIR.defaultState)
	}
	
	@JvmDefault fun isAir(pos: BlockPos): Boolean{
		return getState(pos).material === Material.AIR
	}
	
	@JvmDefault fun placeBlock(pos: BlockPos, picker: IBlockPicker){
		setState(pos, picker.pick(rand))
	}
	
	@JvmDefault fun placeCube(pos1: BlockPos, pos2: BlockPos, picker: IBlockPicker){
		val (x1, y1, z1) = pos1.min(pos2)
		val (x2, y2, z2) = pos1.max(pos2)
		
		val mut = MutableBlockPos()
		
		for(x in x1..x2) for(y in y1..y2) for(z in z1..z2){
			setState(mut.setPos(x, y, z), picker.pick(rand))
		}
	}
	
	@JvmDefault fun placeCubeHollow(pos1: BlockPos, pos2: BlockPos, picker: IBlockPicker){
		val (x1, y1, z1) = pos1.min(pos2)
		val (x2, y2, z2) = pos1.max(pos2)
		
		val mut1 = MutableBlockPos()
		val mut2 = MutableBlockPos()
		
		placeCube(mut1.setPos(x1, y1, z1), mut2.setPos(x2, y1, z2), picker)
		placeCube(mut1.setPos(x1, y2, z1), mut2.setPos(x2, y2, z2), picker)
		
		placeCube(mut1.setPos(x1, y1 + 1, z1), mut2.setPos(x2, y2 - 1, z1), picker)
		placeCube(mut1.setPos(x1, y1 + 1, z2), mut2.setPos(x2, y2 - 1, z2), picker)
		
		placeCube(mut1.setPos(x1, y1 + 1, z1 + 1), mut2.setPos(x1, y2 - 1, z2 - 1), picker)
		placeCube(mut1.setPos(x2, y1 + 1, z1 + 1), mut2.setPos(x2, y2 - 1, z2 - 1), picker)
	}
}