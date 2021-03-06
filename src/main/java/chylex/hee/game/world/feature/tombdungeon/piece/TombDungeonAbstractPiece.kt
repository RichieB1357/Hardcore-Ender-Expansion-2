package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.block.with
import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLoot
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.math.PosXZ
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.facades.Facing6
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.TileEntityChest
import chylex.hee.system.random.nextInt
import net.minecraft.block.Blocks
import net.minecraft.state.properties.SlabType
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import kotlin.math.pow

abstract class TombDungeonAbstractPiece : StructurePiece<TombDungeonLevel>(){
	protected abstract val isFancy: Boolean
	
	abstract val sidePathAttachWeight: Int
	abstract val secretAttachWeight: Int
	open val secretAttachY = 0
	
	override fun generate(world: IStructureWorld, instance: Instance){
		placeLayout(world)
		placeConnections(world, instance)
	}
	
	protected fun placeLayout(world: IStructureWorld){
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		world.placeCube(Pos(0, 0, 0), Pos(maxX, 0, maxZ), Single(ModBlocks.DUSTY_STONE))
		world.placeWalls(Pos(0, 1, 0), Pos(maxX, maxY, maxZ), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		
		if (size.x > 1 && size.z > 1){
			world.placeCube(Pos(1, maxY, 1), Pos(maxX - 1, maxY, maxZ - 1), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_CEILING else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		}
	}
	
	protected fun placeCobwebs(world: IStructureWorld, instance: Instance){
		placeCobwebs(world, instance.context?.let { (0.175F - (0.125F * (it.ordinal / 4F))).pow(1.5F) } ?: 0F)
	}
	
	protected open fun placeCobwebs(world: IStructureWorld, chancePerXZ: Float){
		if (chancePerXZ < 0.0001F){
			return
		}
		
		val rand = world.rand
		val cobwebs = mutableListOf<BlockPos>()
		
		repeat((chancePerXZ * size.x * size.z).floorToInt()){
			for(attempt in 1..3){
				val pos = Pos(
					rand.nextInt(0, size.maxX),
					rand.nextInt(0, size.maxY),
					rand.nextInt(0, size.maxZ)
				)
				
				if (world.isAir(pos) && (Facing6.any { !world.isAir(pos.offset(it)) } || cobwebs.any { it.distanceSqTo(pos) <= 3.1 })){
					world.setBlock(pos, ModBlocks.ANCIENT_COBWEB)
					cobwebs.add(pos)
					break
				}
			}
		}
	}
	
	protected fun placeCrumblingCeiling(world: IStructureWorld, instance: Instance, amount: Int){
		if (instance.context?.let { it === TombDungeonLevel.LAST } == true){
			return
		}
		
		val rand = world.rand
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		repeat(amount){
			val basePos = PosXZ(
				rand.nextInt(0, maxX),
				rand.nextInt(0, maxZ)
			)
			
			repeat(rand.nextInt(1, 4)){
				val testPos = basePos.add(
					rand.nextInt(-2, 2),
					rand.nextInt(-2, 2)
				)
				
				if (testPos.x >= 0 && testPos.z >= 0 && testPos.x <= maxX && testPos.z <= maxZ && world.isAir(testPos.withY(1))){
					world.setBlock(testPos.withY(1), ModBlocks.DUSTY_STONE_BRICK_SLAB)
					world.setState(testPos.withY(maxY), ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.TYPE, SlabType.TOP))
				}
			}
		}
	}
	
	protected fun placeChest(world: IStructureWorld, instance: Instance, pos: BlockPos, facing: Direction, secret: Boolean = false){
		val level = instance.context ?: TombDungeonLevel.FIRST
		val chest = TileEntityChest().apply { TombDungeonLoot.generate(this, world.rand, level, secret) }
		
		world.addTrigger(pos, TileEntityStructureTrigger(Blocks.CHEST.withFacing(facing), chest))
	}
}
