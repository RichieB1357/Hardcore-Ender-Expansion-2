package chylex.hee.game.world.util
import chylex.hee.system.migration.vanilla.BlockStairs
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.facades.Rotation4
import chylex.hee.system.util.nextItem
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.state.properties.StairsShape.INNER_LEFT
import net.minecraft.state.properties.StairsShape.INNER_RIGHT
import net.minecraft.state.properties.StairsShape.OUTER_LEFT
import net.minecraft.state.properties.StairsShape.OUTER_RIGHT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import java.util.Random

data class Transform(val rotation: Rotation, val mirror: Boolean){
	companion object{
		val NONE = Transform(Rotation.NONE, mirror = false)
		val ALL = booleanArrayOf(false, true).flatMap { mirror -> Rotation4.map { rotation -> Transform(rotation, mirror) } }
		
		fun random(rand: Random) = rand.nextItem(ALL)
	}
	
	private val mirroring = if (mirror) Mirror.FRONT_BACK else Mirror.NONE
	
	val reverse
		get() = when(rotation){
			Rotation.NONE ->
				Transform(Rotation.NONE, mirror)
			
			Rotation.CLOCKWISE_90 ->
				Transform(if (mirror) Rotation.CLOCKWISE_90 else Rotation.COUNTERCLOCKWISE_90, mirror)
			
			Rotation.CLOCKWISE_180 ->
				Transform(Rotation.CLOCKWISE_180, mirror)
			
			Rotation.COUNTERCLOCKWISE_90 ->
				Transform(if (mirror) Rotation.COUNTERCLOCKWISE_90 else Rotation.CLOCKWISE_90, mirror)
		}
	
	fun applyTo(target: Transform): Transform{
		return Transform(target.rotation.add(rotation), target.mirror xor mirror)
	}
	
	private fun unfuckStairMirror(state: BlockState): BlockState{
		val transformed = state.rotate(rotation).mirror(mirroring)
		
		return when(transformed[BlockStairs.SHAPE]){
			INNER_LEFT -> transformed.with(BlockStairs.SHAPE, INNER_RIGHT)
			INNER_RIGHT -> transformed.with(BlockStairs.SHAPE, INNER_LEFT)
			OUTER_LEFT -> if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) transformed.with(BlockStairs.SHAPE, OUTER_RIGHT) else transformed
			OUTER_RIGHT -> if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) transformed.with(BlockStairs.SHAPE, OUTER_LEFT) else transformed
			else -> transformed
		}
	}
	
	operator fun invoke(facing: Direction): Direction{
		return mirroring.mirror(rotation.rotate(facing))
	}
	
	operator fun invoke(state: BlockState): BlockState{
		return if (mirror && state.block is BlockStairs)
			unfuckStairMirror(state) // UPDATE 1.14 (check if stairs still need unfucking)
		else
			state.rotate(rotation).mirror(mirroring)
	}
	
	operator fun invoke(entity: Entity){
		entity.rotationYaw = entity.getRotatedYaw(rotation)
		entity.rotationYaw = entity.getMirroredYaw(mirroring)
	}
	
	operator fun invoke(tile: TileEntity){
		tile.rotate(rotation)
		tile.mirror(mirroring)
	}
	
	operator fun invoke(size: Size): Size{
		return size.rotate(rotation)
	}
	
	operator fun invoke(pos: BlockPos, size: Size): BlockPos{
		val (x, y, z) = pos
		
		val maxX = size.maxX
		val maxZ = size.maxZ
		
		val transformedX: Int
		val transformedZ: Int
		
		when(rotation){
			Rotation.NONE ->
			{ transformedX = if (mirror) maxX - x else x; transformedZ = z }
			
			Rotation.CLOCKWISE_90 ->
			{ transformedX = if (mirror) z else maxZ - z; transformedZ = x }
			
			Rotation.CLOCKWISE_180 ->
			{ transformedX = if (mirror) x else maxX - x; transformedZ = maxZ - z }
			
			Rotation.COUNTERCLOCKWISE_90 ->
			{ transformedX = if (mirror) maxZ - z else z; transformedZ = maxX - x }
		}
		
		return Pos(transformedX, y, transformedZ)
	}
}
