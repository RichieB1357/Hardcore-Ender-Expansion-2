package chylex.hee.game.block
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.FAIL
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.PASSTHROUGH
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.SUCCESS
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.damage.CombinedDamage
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_MAGIC
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ARMOR_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.NUDITY_DANGER
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.ParticleTeleport.Data
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.FLAG_NONE
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setState
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.INVISIBLE
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockCorruptedEnergy(builder: BlockSimple.Builder) : BlockSimple(builder){
	companion object{
		private const val MIN_LEVEL = 0
		private const val MAX_LEVEL = 15 // UPDATE: extend to 20
		
		private const val MAX_TICK_RATE = 5
		private const val MIN_TICK_RATE = 1
		
		val LEVEL = PropertyInteger.create("level", MIN_LEVEL, MAX_LEVEL)!!
		
		private val DAMAGE_PART_NORMAL = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
		private val DAMAGE_PART_MAGIC = Damage(MAGIC_TYPE, NUDITY_DANGER, RAPID_DAMAGE(2))
		
		private val PARTICLE_CORRUPTION = ParticleSpawnerCustom(
			type = ParticleTeleport,
			data = Data(minLifespan = 8, maxLifespan = 12, minScale = 2.5F, maxScale = 5.0F),
			pos = InBox(0.75F),
			mot = InBox(0.05F),
			hideOnMinimalSetting = false
		)
		
		private fun tickRateForLevel(level: Int): Int{
			return (MAX_TICK_RATE - (level / 2)).coerceAtLeast(MIN_TICK_RATE)
		}
		
		private fun isEntityTolerant(entity: EntityLivingBase): Boolean{
			return false // TODO
		}
	}
	
	init{
		needsRandomTick = true // just to be safe
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, LEVEL)
	
	// Utility methods
	
	enum class SpawnResult{
		SUCCESS, PASSTHROUGH, FAIL
	}
	
	fun spawnCorruptedEnergy(world: World, pos: BlockPos, level: Int): SpawnResult{
		if (level < MIN_LEVEL){
			return FAIL
		}
		else if (level > MAX_LEVEL){
			return spawnCorruptedEnergy(world, pos, MAX_LEVEL)
		}
		
		val currentState = pos.getState(world)
		val currentBlock = currentState.block
		var updateFlags = FLAG_SYNC_CLIENT
		
		if (currentBlock === this){
			if (level - currentState.getValue(LEVEL) < 3 || world.rand.nextBoolean()){
				return FAIL
			}
			
			updateFlags = FLAG_NONE
		}
		else if (currentBlock === ModBlocks.ENERGY_CLUSTER){
			if (world.rand.nextInt(100) < 5 * level){
				pos.getTile<TileEntityEnergyCluster>(world)?.deteriorateCapacity(level)
			}
			
			return PASSTHROUGH
		}
		else if (!currentBlock.isAir(currentState, world, pos)){
			return if (currentState.isNormalCube)
				FAIL
			else
				PASSTHROUGH
		}
		
		pos.setState(world, defaultState.withProperty(LEVEL, level), updateFlags)
		return SUCCESS
	}
	
	// Tick handling
	
	override fun tickRate(world: World): Int{
		return MAX_TICK_RATE
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		world.scheduleUpdate(pos, this, tickRateForLevel(state.getValue(LEVEL)))
	}
	
	override fun randomTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		if (!world.isUpdateScheduled(pos, this)){
			pos.setAir(world)
		}
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		if (world.isRemote){
			return
		}
		
		val level = state.getValue(LEVEL)
		val remainingFacings = Facing6.toMutableList()
		
		repeat(rand.nextInt(3, 5).coerceAtMost(level)){
			val facing = remainingFacings.removeAt(rand.nextInt(remainingFacings.size))
			val adjacentPos = pos.offset(facing)
			
			val spreadDecrease = if (rand.nextInt(3) == 0) 0 else 1
			
			if (spawnCorruptedEnergy(world, adjacentPos, level - spreadDecrease) == PASSTHROUGH){
				spawnCorruptedEnergy(world, adjacentPos.offset(facing), level - spreadDecrease - 1)
			}
		}
		
		if (rand.nextInt(4) != 0){
			val decreaseToLevel = level - rand.nextInt(1, 2)
			
			if (decreaseToLevel < MIN_LEVEL){
				pos.setAir(world)
				return
			}
			
			pos.setState(world, state.withProperty(LEVEL, decreaseToLevel), FLAG_NONE) // does not call onBlockAdded for the same Block instance
		}
		
		world.scheduleUpdate(pos, this, tickRateForLevel(level))
	}
	
	// Interactions
	
	override fun isAir(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean{
		return true
	}
	
	override fun canCollideCheck(state: IBlockState, hitIfLiquid: Boolean): Boolean{ // actually used for raytracing, not entity collisions
		return false
	}
	
	override fun onEntityCollidedWithBlock(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (entity is EntityLivingBase && !isEntityTolerant(entity)){
			CombinedDamage(
				DAMAGE_PART_NORMAL to 0.75F,
				DAMAGE_PART_MAGIC to (0.75F + state.getValue(LEVEL) / 10F)
			).dealTo(entity, TITLE_MAGIC)
		}
	}
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	override fun randomDisplayTick(state: IBlockState, world: World, pos: BlockPos, rand: Random){
		val amount = rand.nextInt(0, 2)
		
		if (amount > 0){
			PARTICLE_CORRUPTION.spawn(Point(pos, amount), rand) // TODO figure out how to show particles outside randomDisplayTick range
		}
	}
	
	// General
	
	override fun getMetaFromState(state: IBlockState): Int = state.getValue(LEVEL)
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(LEVEL, meta)
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB? = NULL_AABB
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	override fun isFullCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
	override fun getRenderType(state: IBlockState): EnumBlockRenderType = INVISIBLE
	
	// Debugging
	// override fun getBlockLayer(): BlockRenderLayer = CUTOUT
}