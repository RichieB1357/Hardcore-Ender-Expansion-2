package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.system.migration.vanilla.BlockFlowingFluid
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class FluidStructureTrigger(private val block: BlockFlowingFluid) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		world.setState(pos, transform(block.defaultState))
	}
	
	override fun realize(world: IWorld, pos: BlockPos, transform: Transform){
		world.pendingFluidTicks.scheduleTick(pos, block.fluid, 0)
	}
}
