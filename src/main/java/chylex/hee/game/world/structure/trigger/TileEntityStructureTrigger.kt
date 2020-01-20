package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class TileEntityStructureTrigger(private val state: BlockState, private val nbt: TagCompound) : IStructureTrigger{
	constructor(state: BlockState, tile: TileEntity) : this(state, tile.serializeNBT())
	constructor(block: Block, nbt: TagCompound) : this(block.defaultState, nbt)
	constructor(block: Block, tile: TileEntity) : this(block, tile.serializeNBT())
	
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		world.setState(pos, transform(state))
	}
	
	override fun realize(world: IWorld, pos: BlockPos, transform: Transform){
		if (pos.getBlock(world) !== state.block){
			return
		}
		
		world.getChunk(pos).addTileEntity(pos, state.createTileEntity(world)!!.also {
			it.read(nbt)
			it.pos = pos
			transform(it)
		})
	}
}
