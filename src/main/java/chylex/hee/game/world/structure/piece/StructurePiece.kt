package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructurePiece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.world.RotatedStructureWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.with
import net.minecraft.block.BlockColored
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.Rotation

abstract class StructurePiece : IStructurePiece, IStructureGenerator{
	protected abstract val connections: Array<IStructurePieceConnection>
	
	final override fun generate(world: IStructureWorld){
		generate(world, MutableInstance(Rotation.NONE).apply { connections.forEach { useConnection(it, MutableInstance(Rotation.NONE)) } })
		
		for(connection in connections){
			world.setState(connection.offset, Blocks.WOOL.with(BlockColored.COLOR, EnumDyeColor.values()[connection.facing.index - 2]))
		}
	}
	
	protected abstract fun generate(world: IStructureWorld, instance: Instance)
	
	// TODO support rotation + mirror
	
	// Rotated connection
	
	private class RotatedStructurePieceConnection(private val original: IStructurePieceConnection, size: Size, rotation: Rotation) : IStructurePieceConnection{
		override val offset = RotatedStructureWorld.rotatePos(original.offset, size, rotation)
		override val facing = rotation.rotate(original.facing)!!
		
		override val isEvenWidth
			get() = original.isEvenWidth
		
		override fun canBeAttachedTo(target: IStructurePieceConnection): Boolean{
			return original.canBeAttachedTo((target as? RotatedStructurePieceConnection)?.original ?: target)
		}
		
		fun matches(other: IStructurePieceConnection): Boolean{
			return this === other || original === other
		}
		
		override fun toString(): String{
			return original.toString()
		}
	}
	
	// Frozen instance
	
	open inner class Instance private constructor(private val rotation: Rotation, private val availableConnections: List<RotatedStructurePieceConnection>) : IStructureGenerator{
		constructor(rotation: Rotation) : this(rotation, this@StructurePiece.connections.map { RotatedStructurePieceConnection(it, this@StructurePiece.size, rotation) }.toMutableList())
		
		final override val size = this@StructurePiece.size.rotate(rotation)
		
		val hasAvailableConnections
			get() = availableConnections.isNotEmpty()
		
		fun findAvailableConnections(): List<IStructurePieceConnection>{
			return availableConnections
		}
		
		fun findAvailableConnections(targetConnection: IStructurePieceConnection): List<IStructurePieceConnection>{
			val targetFacing = targetConnection.facing.opposite
			return availableConnections.filter { it.facing == targetFacing && it.canBeAttachedTo(targetConnection) }
		}
		
		fun isConnectionUsed(connection: IStructurePieceConnection): Boolean{
			return availableConnections.none { it.matches(connection) }
		}
		
		fun freeze(): Instance{
			return Instance(rotation, availableConnections.toList())
		}
		
		final override fun generate(world: IStructureWorld){
			this@StructurePiece.generate(RotatedStructureWorld(world, this@StructurePiece.size, rotation), this)
		}
	}
	
	// Mutable instance
	
	open inner class MutableInstance(rotation: Rotation) : Instance(rotation){
		private val availableConnections = findAvailableConnections() as MutableList<RotatedStructurePieceConnection> // ugly implementation detail
		private val usedConnections = mutableMapOf<RotatedStructurePieceConnection, MutableInstance>()
		
		fun useConnection(connection: IStructurePieceConnection, neighbor: MutableInstance){
			val index = availableConnections.indexOfFirst { it.matches(connection) }
			
			if (index != -1){
				availableConnections.removeAt(index).let { usedConnections[it] = neighbor }
			}
		}
		
		fun restoreConnection(neighbor: MutableInstance): Boolean{
			val used = usedConnections.filterValues { it === neighbor }.keys
			
			if (used.isEmpty()){
				return false
			}
			
			for(connection in used){
				availableConnections.add(connection)
				usedConnections.remove(connection)
			}
			
			return true
		}
		
		fun restoreAllConnections(): List<MutableInstance>{
			return usedConnections.values.toList().also {
				availableConnections.addAll(usedConnections.keys)
				usedConnections.clear()
			}
		}
	}
}
