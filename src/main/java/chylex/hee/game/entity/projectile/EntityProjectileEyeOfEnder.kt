package chylex.hee.game.entity.projectile
import chylex.hee.game.particle.ParticleGlitter
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IOffset.InSphere
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.render.util.IColor
import chylex.hee.game.render.util.LerpedFloat
import chylex.hee.game.render.util.RGB
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.lookDirVec
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.setPos
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.BlockLiquid
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fluids.IFluidBlock
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.sin

class EntityProjectileEyeOfEnder : Entity, IEntityAdditionalSpawnData{
	private companion object{
		private const val TICK_BEGIN_GLITTER = 30
		private const val TICK_BEGIN_MOVEMENT = 40
		private const val TICK_DROP_NO_TARGET = 50
		
		private const val TICK_FULL_Y_MOVEMENT = 100
		private const val TICK_END_MOVEMENT = 400
		private const val TICK_DESTROY_NO_ENERGY_MIN = 440
		private const val TICK_DESTROY_NO_ENERGY_MAX = TICK_DESTROY_NO_ENERGY_MIN + 100
		
		private const val TICK_REACHED_TARGET_SKIP = 500
		private const val TICK_DROP_REACHED_TARGET = 580
		
		private val PARTICLE_SMOKE = ParticleSpawnerVanilla(
			type = SMOKE_NORMAL,
			pos = Constant(0.1F, UP) + InBox(0.15F),
			mot = InBox(0.075F),
			ignoreRangeLimit = true,
			hideOnMinimalSetting = false
		)
		
		private val PARTICLE_GLITTER_TICK = ParticleSpawnerCustom(
			type = ParticleGlitter,
			data = GlitterDataTick,
			pos = InBox(0.15F),
			mot = Constant(0.025F, DOWN) + InBox(0.02F)
		)
		
		private val PARTICLE_GLITTER_DESTROY = ParticleSpawnerCustom(
			type = ParticleGlitter,
			data = GlitterDataDestroy,
			pos = InSphere(0.35F),
			mot = InBox(0.04F),
			maxRange = 64.0
		)
		
		private object GlitterDataTick : ParticleGlitter.Data(maxAgeMultiplier = 3..5){
			override fun nextColor(rand: Random): IColor{
				return if (rand.nextInt(3) == 0)
					RGB(rand.nextInt(76, 128), rand.nextInt(64, 76), rand.nextInt(128, 192))
				else
					RGB(rand.nextInt(51, 76), rand.nextInt(64, 166), rand.nextInt(76, 102))
			}
		}
		
		private object GlitterDataDestroy : ParticleGlitter.Data(maxAgeMultiplier = 1..2){
			override fun nextColor(rand: Random): IColor{
				return if (rand.nextInt(3) == 0)
					RGB(rand.nextInt(102, 153), rand.nextInt(64, 76), rand.nextInt(153, 216))
				else
					RGB(rand.nextInt(90, 115), rand.nextInt(76, 178), rand.nextInt(102, 128))
			}
		}
		
		private fun shouldFloatAbove(state: IBlockState): Boolean{
			return state.material.blocksMovement() || state.block.let { it is IFluidBlock || it is BlockLiquid }
		}
	}
	
	// Instance
	
	constructor(world: World) : super(world){
		setSize(0.5F, 1F)
	}
	
	constructor(thrower: EntityLivingBase, targetPos: BlockPos?) : this(thrower.world){
		this.posVec = thrower.lookPosVec.add(0.0, -(height * 0.5), 0.0).add(thrower.lookDirVec.scale(1.5))
		this.targetPos = targetPos
	}
	
	val renderBob = LerpedFloat(nextRenderBobOffset)
	
	private val posVecWithOffset
		get() = posVec.add(0.0, 0.1 + renderBob.currentValue, 0.0)
	
	private val nextRenderBobOffset
		get() = 0.35F + (sin(timer * 0.15F) * 0.25F) // 0.35 offset for bounding box
	
	private val targetVecXZ
		get() = targetPos?.let { Vec3d(it.x + 0.5 - posX, 0.0, it.z + 0.5 - posZ) } ?: Vec3d.ZERO
	
	private var targetPos: BlockPos? = null
	private var timer = 0
	private var speed = 0F
	
	private var prevPos = BlockPos.ORIGIN
	private var targetY = 0.0
	
	// Initialization
	
	override fun entityInit(){}
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writePos(targetPos ?: BlockPos.ORIGIN)
		writeShort(timer)
		writeFloat(speed)
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		targetPos = buffer.readPos().takeIf { it != BlockPos.ORIGIN }
		timer = buffer.readShort().toInt()
		speed = buffer.readFloat()
	}
	
	// Behavior
	
	override fun onUpdate(){
		lastTickPosX = posX
		lastTickPosY = posY
		lastTickPosZ = posZ
		super.onUpdate()
		
		if (ticksExisted == 1){
			motionVec = targetVecXZ.normalize().scale(0.27)
		}
		
		++timer
		rotationYaw += 5F
		
		if (world.isRemote){
			renderBob.update(nextRenderBobOffset)
			
			if (timer == 1){
				PARTICLE_SMOKE.spawn(Point(posVecWithOffset, 8), rand)
			}
			else if (timer > TICK_BEGIN_GLITTER && targetPos != null){
				PARTICLE_GLITTER_TICK.spawn(Point(posVecWithOffset, 3), rand)
			}
		}
		
		if (targetPos == null){
			if (!world.isRemote && timer > TICK_DROP_NO_TARGET){
				dropEye()
			}
		}
		else if (timer > TICK_BEGIN_MOVEMENT){
			val pos = Pos(this)
			
			if (prevPos != pos){
				prevPos = pos
				updateTargetAltitude()
			}
			
			moveTowardsTarget()
		}
	}
	
	private fun updateTargetAltitude(){
		val perpendicular = Vec3d(-(motionZ * 3.0), 0.0, motionX * 3.0)
		val step = motionVec.scale(4.0)
		
		val parallelStarts = arrayOf(
			posVec,
			posVec.subtract(perpendicular),
			posVec.add(perpendicular)
		)
		
		val checkedBlocks = HashSet<BlockPos>(36, 1F)
		
		parallelStarts.flatMapTo(checkedBlocks){
			start -> (0..11).map { world.getHeight(Pos(start.add(step.scale(it.toDouble())))) }
		}
		
		if (checkedBlocks.isEmpty()){
			targetY = posY
			return
		}
		
		val averageY = checkedBlocks
			.asSequence()
			.map { pos -> 1 + (pos.offsetUntil(DOWN, 0..(pos.y)){ shouldFloatAbove(it.getState(world)) } ?: pos).y }
			.sortedDescending()
			.take(1 + (checkedBlocks.size / 4))
			.average()
		
		targetY = averageY + 2.5
	}
	
	private fun moveTowardsTarget(){
		val ySpeedMp: Float
		
		if (targetVecXZ.lengthSquared() < square(12.0)){
			if (speed > 0F){
				speed -= 0.025F
			}
			
			ySpeedMp = speed
			
			if (timer < TICK_REACHED_TARGET_SKIP){
				timer = TICK_REACHED_TARGET_SKIP
			}
			else if (!world.isRemote && timer > TICK_DROP_REACHED_TARGET){
				dropEye()
			}
		}
		else{
			if (timer <= TICK_END_MOVEMENT && speed < 1F){
				speed += 0.02F
			}
			else if (timer > TICK_END_MOVEMENT && speed > 0.25F){
				speed -= 0.015F
			}
			
			if (speed > 0.7F && targetY - posY > 4.0){
				speed -= 0.05F
			}
			
			ySpeedMp = if (timer < TICK_FULL_Y_MOVEMENT) speed else 1F
			
			if (!world.isRemote && timer > TICK_DESTROY_NO_ENERGY_MIN && timer > rand.nextInt(TICK_DESTROY_NO_ENERGY_MIN, TICK_DESTROY_NO_ENERGY_MAX)){
				setDead()
			}
		}
		
		val (newX, _, newZ) = posVec.add(motionVec.scale(speed.toDouble()))
		val newY = posY + (targetY - posY) * 0.03 * ySpeedMp
		setPosition(newX, newY, newZ)
	}
	
	private fun dropEye(){
		EntityItem(world, posX, posY + nextRenderBobOffset - 0.25, posZ, ItemStack(Items.ENDER_EYE)).apply {
			setDefaultPickupDelay()
			world.spawnEntity(this)
		}
		
		setDead()
	}
	
	override fun setDead(){
		super.setDead()
		
		if (world.isRemote){
			val pos = posVecWithOffset
			
			PARTICLE_SMOKE.spawn(Point(pos, 18), rand)
			PARTICLE_GLITTER_DESTROY.spawn(Point(pos, 50), rand)
			SoundEvents.ENTITY_ENDEREYE_DEATH.playClient(pos, SoundCategory.NEUTRAL)
		}
	}
	
	override fun canBeAttackedWithItem(): Boolean{
		return false
	}
	
	@SideOnly(Side.CLIENT)
	override fun setPositionAndRotationDirect(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, posRotationIncrements: Int, teleport: Boolean){}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		targetPos?.let { setPos("Target", it) }
		setShort("Timer", timer.toShort())
		setFloat("Speed", speed)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		targetPos = getPosOrNull("Target")
		timer = getShort("Timer").toInt()
		speed = getFloat("Speed")
	}
}