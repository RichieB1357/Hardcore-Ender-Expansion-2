package chylex.hee.game.block
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInBox
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.isTopSolid
import chylex.hee.game.world.setAir
import chylex.hee.game.world.setBlock
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.system.MagicValues
import chylex.hee.system.facades.Facing4
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.EntityFallingBlock
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.EntitySelectionContext
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockDustyStoneUnstable(builder: BlockBuilder) : BlockDustyStone(builder){
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: EntityPlayer): Boolean{
		return player.getHeldItem(MAIN_HAND).let { EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0 || isPickaxeOrShovel(it) }
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){
		// TODO fx
		
		if (state.block === ModBlocks.DUSTY_STONE_DAMAGED){
			world.addEntity(EntityFallingBlock(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, state))
			
			for(facing in Facing4){
				val adjacentPos = pos.offset(facing)
				
				if (adjacentPos.getBlock(world) is BlockDustyStoneUnstable){
					doCrumbleTest(world, adjacentPos)
				}
			}
			
			if (pos.up().getBlock(world) is BlockDustyStoneUnstable){
				succeedCrumbleTest(world, pos.up())
			}
		}
		else if (state.block === ModBlocks.DUSTY_STONE_CRACKED){
			pos.setBlock(world, ModBlocks.DUSTY_STONE_DAMAGED)
			succeedCrumbleTest(world, pos)
		}
		else{
			pos.setBlock(world, ModBlocks.DUSTY_STONE_CRACKED)
			succeedCrumbleTest(world, pos)
		}
	}
	
	override fun getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape{
		return if (context is EntitySelectionContext && context.entity is EntityLivingBase)
			MagicValues.BLOCK_COLLISION_SHRINK_SHAPE
		else
			VoxelShapes.fullCube()
	}
	
	override fun causesSuffocation(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean{
		return false // prevents sliding off the block
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity){
		if (!world.isRemote && world.totalTime % 4L == 0L && !(entity.height <= 0.5F || (entity.height <= 1F && entity.width <= 0.5F)) && isNonCreative(entity)){
			if (!doCrumbleTest(world, pos)){
				return
			}
			
			for(facing in Facing4){
				val adjacentPos = pos.offset(facing)
				
				if (adjacentPos.getBlock(world) is BlockDustyStoneUnstable){
					doCrumbleTest(world, adjacentPos)
				}
			}
		}
	}
	
	override fun onFallenUpon(world: World, pos: BlockPos, entity: Entity, fallDistance: Float){
		if (!world.isRemote && entity is EntityLivingBase && fallDistance > 1.3F && isNonCreative(entity)){
			val rand = world.rand
			val aabb = entity.boundingBox
			val y = pos.y
			
			val minX = (aabb.minX + 0.001).floorToInt()
			val maxX = (aabb.maxX - 0.001).floorToInt()
			val minZ = (aabb.minZ + 0.001).floorToInt()
			val maxZ = (aabb.maxZ - 0.001).floorToInt()
			
			for(testPos in Pos(minX, y, minZ).allInBox(Pos(maxX, y, maxZ))){
				val state = testPos.getState(world)
				
				if (state.block !is BlockDustyStoneUnstable){
					continue
				}
				
				if (rand.nextBoolean()){
					world.playEvent(2001, testPos, getStateId(state))
					testPos.setAir(world)
				}
				else{
					doCrumbleTest(world, testPos)
				}
			}
			
			// POLISH would like to avoid damaging the player if all blocks break and accumulate fall distance, but the caller resets it
		}
		
		super.onFallenUpon(world, pos, entity, fallDistance)
	}
	
	private fun isNonCreative(entity: Entity): Boolean{
		return entity !is EntityPlayer || !entity.isCreative
	}
	
	private fun doCrumbleTest(world: World, pos: BlockPos): Boolean{
		for(offset in 1..8){
			val testPos = pos.down(offset)
			val isDustyStone = testPos.getBlock(world) is BlockDustyStoneUnstable
			
			if (!isDustyStone){
				if (!testPos.isTopSolid(world)){
					succeedCrumbleTest(world, testPos.up())
					return true
				}
				
				break
			}
		}
		
		return false
	}
	
	private fun succeedCrumbleTest(world: World, pos: BlockPos){
		val rand = world.rand
		val block = pos.getBlock(world)
		
		val delay = if (block === ModBlocks.DUSTY_STONE_DAMAGED)
			rand.nextInt(6, 10)
		else if (block === ModBlocks.DUSTY_STONE_CRACKED)
			rand.nextInt(9, 13)
		else
			rand.nextInt(12, 15)
		
		world.pendingBlockTicks.scheduleTick(pos, block, delay)
	}
}
