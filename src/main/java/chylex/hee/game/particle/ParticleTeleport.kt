package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.entity.Entity
import net.minecraft.world.World
import java.util.Random

object ParticleTeleport : IParticleMaker<ParticleDataColorLifespanScale>{
	private val rand = Random()
	
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data ?: DEFAULT_DATA.generate(rand))
	}
	
	fun Data(
		lifespan: IntRange = 35..50,
		scale: ClosedFloatingPointRange<Float> = (1.25F)..(1.45F)
	) = ParticleDataColorLifespanScale.Generator(DefaultColor, lifespan, scale)
	
	private object DefaultColor : IRandomColor{
		override fun next(rand: Random): IntColor{
			val blue = rand.nextFloat(0.4F, 1.0F)
			val green = blue * 0.3F
			val red = blue * 0.9F
			
			return RGB((red * 255F).floorToInt(), (green * 255F).floorToInt(), (blue * 255F).floorToInt())
		}
	}
	
	private val DEFAULT_DATA = Data()
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		private val initialScale = data.scale
		
		init{
			particleTextureIndexX = rand.nextInt(8)
			particleTextureIndexY = 0
			
			loadColor(data.color)
			
			maxAge = data.lifespan
		}
		
		override fun renderParticle(buffer: BufferBuilder, entity: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float){
			particleScale = initialScale * (1F - (age + partialTicks) / (maxAge + 1F))
			super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ)
		}
	}
}
