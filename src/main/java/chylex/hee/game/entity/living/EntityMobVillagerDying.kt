package chylex.hee.game.entity.living
import chylex.hee.game.particle.ParticleGrowingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.system.migration.vanilla.EntityAgeable
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntityVillager
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readTag
import chylex.hee.system.util.use
import chylex.hee.system.util.writeTag
import com.mojang.datafixers.Dynamic
import net.minecraft.entity.EntityType
import net.minecraft.entity.merchant.villager.VillagerData
import net.minecraft.entity.villager.IVillagerDataHolder
import net.minecraft.nbt.NBTDynamicOps
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random

class EntityMobVillagerDying(type: EntityType<EntityMobVillagerDying>, world: World) : EntityAgeable(type, world), IVillagerDataHolder, IEntityAdditionalSpawnData{
	constructor(world: World) : this(ModEntities.VILLAGER_DYING, world)
	
	private class DecayParticlePos private constructor(private val heightMp: Float) : IOffset{
		companion object{
			private const val HALF_SIZE = 0.3F
			
			val ADULT = DecayParticlePos(1F)
			val CHILD = DecayParticlePos(0.5F)
		}
		
		override fun next(out: MutableOffsetPoint, rand: Random){
			if (rand.nextInt(5) == 0){
				out.x = rand.nextFloat(-HALF_SIZE, HALF_SIZE)
				out.y = rand.nextFloat(1F, 1.25F) * heightMp * (if (rand.nextBoolean()) -1 else 1)
				out.z = rand.nextFloat(-HALF_SIZE, HALF_SIZE)
			}
			else{
				val facing = rand.nextItem(Facing4)
				
				val offsetFull = HALF_SIZE + rand.nextFloat(0F, 0.3F)
				val offsetPerpendicular = rand.nextFloat(-HALF_SIZE, HALF_SIZE)
				
				out.x = (facing.xOffset * offsetFull) + (facing.zOffset * offsetPerpendicular)
				out.y = rand.nextFloat(-1.1F, 1.1F) * heightMp
				out.z = (facing.zOffset * offsetFull) + (facing.xOffset * offsetPerpendicular)
			}
		}
	}
	
	var villager: VillagerData? = null
		private set
	
	init{
		isInvulnerable = true
		setNoGravity(true)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		experienceValue = 0
	}
	
	fun copyVillagerDataFrom(villager: EntityVillager){
		setGrowingAge(villager.growingAge)
		this.villager = villager.villagerData
		
		renderYawOffset = villager.renderYawOffset
		rotationYawHead = villager.rotationYawHead
		limbSwing = villager.limbSwing
	}
	
	override fun getVillagerData(): VillagerData{
		return villager!!
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writeTag(villagerData.serialize(NBTDynamicOps.INSTANCE) as TagCompound)
		writeFloat(renderYawOffset)
		writeFloat(rotationYawHead)
		writeFloat(limbSwing)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		villager = VillagerData(Dynamic(NBTDynamicOps.INSTANCE, buffer.readTag()))
		
		renderYawOffset = readFloat()
		rotationYawHead = readFloat()
		limbSwing = readFloat()
		
		prevRenderYawOffset = renderYawOffset
		prevRotationYawHead = rotationYawHead
	}
	
	override fun tick(){
		firstUpdate = false
		onDeathUpdate()
	}
	
	override fun onDeathUpdate(){
		if (world.isRemote && deathTime < 66){
			if (deathTime == 0){
				ModSounds.MOB_VILLAGER_TOTEM_DYING.playClient(posVec, SoundCategory.HOSTILE, volume = 1.25F)
			}
			
			val isChild = isChild
			
			ParticleSpawnerCustom(
				type = ParticleGrowingSpot,
				data = ParticleGrowingSpot.Data(color = RGB(rand.nextInt(20).toUByte()), lifespan = 71 - deathTime),
				pos = if (isChild) DecayParticlePos.CHILD else DecayParticlePos.ADULT
			).spawn(Point(this, heightMp = 0.5F, amount = if (isChild) 4 else 12), rand)
		}
		
		if (++deathTime == 71){
			remove()
		}
	}
	
	override fun remove(){
		if (world.isRemote && isAlive){
			ParticleSpawnerCustom(
				type = ParticleSmokeCustom,
				data = ParticleSmokeCustom.Data(scale = 1.66F),
				pos = InBox(this, 0.25F)
			).spawn(Point(this, heightMp = 0.5F, amount = 100), rand)
			
			ModSounds.MOB_VILLAGER_TOTEM_DEATH.playClient(posVec, SoundCategory.HOSTILE, volume = 2F, pitch = if (isChild) 1.6F else 1.1F)
		}
		
		super.remove()
	}
	
	override fun processInteract(player: EntityPlayer, hand: Hand) = true
	override fun canBeLeashedTo(player: EntityPlayer) = false
	
	override fun createChild(ageable: EntityAgeable): EntityAgeable? = null
	override fun ageUp(growthSeconds: Int, updateForcedAge: Boolean){}
	
	override fun attackable() = false
	override fun canBeCollidedWith() = false
	override fun canBeHitWithPotion() = false
	
	override fun canDespawn(distanceToClosestPlayerSq: Double) = false
	override fun preventDespawn() = true
}
