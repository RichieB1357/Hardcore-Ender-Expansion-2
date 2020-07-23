package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.addY
import chylex.hee.system.util.color.IRandomColor.Companion.IRandomColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.readVec
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import chylex.hee.system.util.writeVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import kotlin.math.pow
import kotlin.math.sqrt

class EnderEyeSpawnerParticles(private val entity: EntityBossEnderEye) : INBTSerializable<TagCompound>{
	companion object{
		private const val PARTICLE_LIST_TAG = "Particles"
		private const val X_TAG = "X"
		private const val Y_TAG = "Y"
		private const val Z_TAG = "Z"
		private const val DELAY_TAG = "Delay"
		private const val ORIG_DISTANCE_XZ_TAG = "OrigDistXZ"
		
		private val PARTICLE_TICK = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = IRandomColor { RGB(nextInt(160, 220), nextInt(0, 30), nextInt(20, 50)) }, lifespan = 7..19, scale = 0.666F),
			maxRange = 256.0,
			hideOnMinimalSetting = false
		)
		
		class ParticleData(private val point: Vec3d) : IFxData{
			override fun write(buffer: PacketBuffer) = buffer.use {
				writeVec(point)
			}
		}
		
		val FX_PARTICLE = object : IFxHandler<ParticleData>{
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				PARTICLE_TICK.spawn(Point(readVec(), 2), rand)
			}
		}
	}
	
	private class ParticleInstance(pos: Vec3d, delay: Int, private var originalDistanceXZ: Double) : INBTSerializable<TagCompound>{
		constructor() : this(Vec3d.ZERO, 0, 0.0)
		
		var pos = pos
			private set
		
		var delay = delay
			private set
		
		fun tick(entity: EntityBossEnderEye): Boolean{
			if (delay > 0){
				--delay
				return false
			}
			
			val dir = entity.lookPosVec.subtract(pos).normalize()
			val distSq = entity.getDistanceSq(pos)
			
			pos = pos.add(dir.scale(0.04 + (0.08 * (distSq - 1.0).coerceAtLeast(0.0).pow(0.33))))
			
			if (entity.rng.nextInt(3) == 0){
				val progress = sqrt(square(pos.x - entity.posX) + square(pos.z - entity.posZ)) / originalDistanceXZ
				val progressCurvePoint = when{
					progress < 0.3 -> progress / 0.3
					progress > 0.7 -> (1.0 - progress) / 0.3
					else           -> 1.0
				}
				
				PacketClientFX(FX_PARTICLE, ParticleData(pos.addY(sqrt(progressCurvePoint) * 6.0))).sendToAllAround(entity.world, pos, 256.0)
			}
			
			return distSq < square(0.7)
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putDouble(X_TAG, pos.x)
			putDouble(Y_TAG, pos.y)
			putDouble(Z_TAG, pos.z)
			putInt(DELAY_TAG, delay)
			putDouble(ORIG_DISTANCE_XZ_TAG, originalDistanceXZ)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			pos = Vec3d(
				getDouble(X_TAG),
				getDouble(Y_TAG),
				getDouble(Z_TAG)
			)
			
			delay = getInt(DELAY_TAG)
			originalDistanceXZ = getDouble(ORIG_DISTANCE_XZ_TAG)
		}
	}
	
	// Manager
	
	private val particles = mutableListOf<ParticleInstance>()
	
	fun add(start: BlockPos){
		val center = Vec3d(start.x + 0.5, start.y.toDouble(), start.z + 0.5)
		var delay = 0
		
		for(particle in particles){
			if (center.squareDistanceTo(particle.pos) < square(0.8)){
				delay = particle.delay + 15
			}
		}
		
		particles.add(ParticleInstance(center, delay, sqrt(square(entity.posX - center.x) + square(entity.posZ - center.z))))
	}
	
	fun tick(){
		particles.removeAll { it.tick(entity) }
	}
	
	override fun serializeNBT() = TagCompound().apply {
		putList(PARTICLE_LIST_TAG, NBTObjectList.of(particles.map(ParticleInstance::serializeNBT)))
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		particles.clear()
		particles.addAll(getListOfCompounds(PARTICLE_LIST_TAG).map { ParticleInstance().apply { deserializeNBT(it) } })
	}
}
