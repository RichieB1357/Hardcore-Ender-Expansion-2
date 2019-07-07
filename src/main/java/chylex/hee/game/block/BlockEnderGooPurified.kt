package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidEnderGooPurified
import chylex.hee.game.block.info.Materials
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.mechanics.potion.PotionBase.Companion.INFINITE_DURATION_THRESHOLD
import chylex.hee.game.mechanics.potion.PotionPurity.MIN_DURATION
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientPotionDuration
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.get
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

open class BlockEnderGooPurified : BlockAbstractGoo(FluidEnderGooPurified, Materials.PURIFIED_ENDER_GOO){
	companion object{
		private val PARTICLE_DATA = ParticleSmokeCustom.Data(color = RGB(210, 148, 237))
		
		private val PARTICLE_STATIONARY = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = PARTICLE_DATA,
			pos = Constant(0.4F, UP) + InBox(0.5F, 0.1F, 0.5F),
			mot = InBox(0.005F, 0.01F, 0.005F)
		)
		
		private val PARTICLE_FLOWING = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = PARTICLE_DATA,
			pos = Constant(0.1F, DOWN) + InBox(0.4F, 0.1F, 0.4F),
			mot = InBox(0.01F, 0.01F, 0.01F)
		)
		
		private val PARTICLE_PLACE = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = PARTICLE_DATA,
			pos = Constant(0.2F, DOWN) + InBox(0.55F, 0.1F, 0.55F),
			mot = Constant(0.025F, UP) + InBox(0.01F, 0.01F, 0.01F)
		)
		
		@JvmStatic
		val FX_PLACE = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_PLACE.spawn(Point(pos, rand.nextInt(5, 6)), rand)
			}
		}
		
		private const val MAX_COLLISION_TICK_COUNTER = 20 * 50
		private const val MIN_DURATION_REDUCTION = 300
		private const val MAX_DURATION_REDUCTION = 1200
		
		// Status effects
		
		private fun updateGooEffects(entity: EntityLivingBase, totalTicks: Int){
			val currentTime = entity.world.totalWorldTime
			val rand = entity.rng
			
			if (totalTicks > 25 && currentTime % 40L == 0L){
				val effect = rand.nextItemOrNull(entity.activePotionEffects.filter { it.potion.isBadEffect && it.duration in MIN_DURATION..INFINITE_DURATION_THRESHOLD })
				
				if (effect != null){
					effect.duration = max(MIN_DURATION, effect.duration - (effect.duration * 0.2F).floorToInt().coerceIn(MIN_DURATION_REDUCTION, MAX_DURATION_REDUCTION))
					
					if (entity is EntityPlayer){
						PacketClientPotionDuration(effect.potion, effect.duration).sendToPlayer(entity)
					}
				}
			}
			
			if (totalTicks > 35 && currentTime % 93L == 0L){
				val healChance = when{
					totalTicks <  400 -> 0.4F
					totalTicks < 1000 -> 0.6F
					else              -> 0.75F
				}
				
				if (rand.nextFloat() < healChance){
					entity.heal(1F)
				}
			}
			
			if (totalTicks > 40 && currentTime % 116L == 0L && entity is EntityPlayer){
				val satisfactionChance = when{
					totalTicks <  400 -> 0.06F
					totalTicks < 1000 -> 0.12F
					else              -> 0.24F
				}
				
				if (rand.nextFloat() < satisfactionChance){
					entity.foodStats.addStats(1, 0F)
				}
			}
		}
	}
	
	// Initialization
	
	override val filledBucket
		get() = ModItems.PURIFIED_ENDER_GOO_BUCKET
	
	override val tickTrackingKey
		get() = "PurifiedGoo"
	
	init{
		tickRate = 16
	}
	
	// Behavior
	
	override fun onInsideGoo(entity: Entity){
		if (entity is EntityLivingBase){
			updateGooEffects(entity, trackTick(entity, MAX_COLLISION_TICK_COUNTER))
		}
	}
	
	override fun modifyMotion(entity: Entity, level: Int){
		val strength = ((quantaPerBlock - level) / quantaPerBlockFloat).pow(1.25F)
		
		entity.motionX *= 0.95 - (0.45 * strength)
		entity.motionZ *= 0.95 - (0.45 * strength)
	}
	
	override fun flowIntoBlock(world: World, pos: BlockPos, meta: Int){
		if (meta < 0){
			return
		}
		
		if (displaceIfPossible(world, pos)){
			pos.setState(world, this.with(LEVEL, meta))
			PacketClientFX(FX_PLACE, FxBlockData(pos)).sendToAllAround(world, pos, 16.0) // TODO optimize somehow?
		}
	}
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	override fun randomDisplayTick(state: IBlockState, world: World, pos: BlockPos, rand: Random){
		if (rand.nextInt(4) == 0){
			val particle = if (state[LEVEL] == 0)
				PARTICLE_STATIONARY
			else
				PARTICLE_FLOWING
			
			particle.spawn(Point(pos, 1), rand)
		}
	}
}