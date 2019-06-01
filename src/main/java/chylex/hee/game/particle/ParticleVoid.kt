package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.ParticleTexture
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.system.util.addY
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetTowards
import chylex.hee.system.util.scale
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.max
import kotlin.math.min

object ParticleVoid : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
		init{
			particleTexture = ParticleTexture.PIXEL
			
			val color = rand.nextFloat(0.25F, 0.35F)
			val motMp = rand.nextFloat(1F, 3F) * 0.001F
			
			particleRed = color
			particleGreen = color
			particleBlue = color
			particleAlpha = 0F
			
			particleScale = rand.nextFloat(0.25F, 0.4F)
			
			particleMaxAge = (30F / rand.nextFloat(0.3F, 1F)).ceilToInt()
			
			motionVec = Vec3d(
				rand.nextFloat(-1.0, 1.0),
				rand.nextFloat(-1.0, 1.0),
				rand.nextFloat(-1.0, 1.0)
			).normalize().scale(motMp).addY(rand.nextFloat(-0.005, 0.005))
			
			val instance = TerritoryInstance.fromPos(posX.floorToInt(), posZ.floorToInt())
			val center = instance?.centerPoint
			
			if (center != null){
				val posVec = Vec3d(posX, posY, posZ)
				val voidFactor = TerritoryVoid.getVoidFactor(world, posVec)
				
				if (voidFactor >= TerritoryVoid.INSTANT_DEATH_FACTOR){
					setExpired()
				}
				else{
					motionVec = motionVec.offsetTowards(center.add(rand.nextVector(64.0)).subtract(posVec).normalize(), voidFactor * 0.15)
				}
			}
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			motionVec = motionVec.scale(0.996)
			
			if (particleAge >= particleMaxAge - 3){
				particleAlpha = max(0F, particleAlpha - 0.25F)
			}
			else{
				particleAlpha = min(1F, particleAlpha + 0.4F)
			}
		}
		
		override fun getFXLayer() = 1
		override fun setParticleTextureIndex(index: Int){}
	}
}