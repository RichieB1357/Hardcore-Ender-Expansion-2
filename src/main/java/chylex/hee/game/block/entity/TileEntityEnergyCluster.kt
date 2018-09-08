package chylex.hee.game.block.entity
import chylex.hee.game.mechanics.energy.IClusterHealth
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.UNSTABLE
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_POSSIBLE_VALUE
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_REGEN_CAPACITY
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.isAnyPlayerWithinRange
import chylex.hee.system.util.setEnum
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import kotlin.math.pow

class TileEntityEnergyCluster : TileEntityBase(), ITickable{
	private companion object{
		const val DEFAULT_NOTIFY_FLAGS = FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER
		
		const val ENERGY_LEVEL_TAG = "EnergyLevel"
		const val ENERGY_CAPACITY_TAG = "EnergyCapacity"
		
		const val HEALTH_STATUS_TAG = "HealthStatus"
		const val HEALTH_OVERRIDE_TAG = "HealthOverride"
		
		const val INACTIVE_TAG = "Inactive"
	}
	
	// Properties (State)
	
	var energyLevel: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	var energyBaseCapacity: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	private var internalHealthStatus: HealthStatus by Notifying(HEALTHY, DEFAULT_NOTIFY_FLAGS)
	private var internalHealthOverride: HealthOverride? by Notifying(null, DEFAULT_NOTIFY_FLAGS)
	
	// Properties (Calculated)
	
	val currentHealth: IClusterHealth
		get() = internalHealthOverride ?: internalHealthStatus
	
	val energyRegenCapacity: IEnergyQuantity
		get() = minOf(energyBaseCapacity * currentHealth.regenCapacityMp, MAX_REGEN_CAPACITY)
	
	// Variables
	
	private var ticksToRegen = 20
	private var isInactive = false
	
	var breakWithoutExplosion = false
	
	// Methods
	
	fun drainEnergy(quantity: IEnergyQuantity): Boolean{
		if (energyLevel < quantity){
			return false
		}
		else if (energyLevel == MAX_POSSIBLE_VALUE){
			pos.breakBlock(world, false)
			return false
		}
		
		energyLevel -= quantity
		ticksToRegen = 20 + (40F / currentHealth.regenSpeedMp).ceilToInt()
		
		isInactive = false
		return true
	}
	
	// Overrides
	
	override fun update(){
		if (world.isRemote){
			return
		}
		
		if (isInactive){
			if (world.totalWorldTime % 80L == 0L){
				val activationRange = when(internalHealthStatus){
					UNSTABLE -> 48.0
					DAMAGED  -> 32.0
					else     -> 24.0
				}
				
				if (pos.isAnyPlayerWithinRange(world, activationRange)){
					isInactive = false
				}
			}
			
			return
		}
		
		if (energyLevel < energyRegenCapacity && --ticksToRegen < 0){
			energyLevel = minOf(energyRegenCapacity, energyLevel + Floating(((1 + energyBaseCapacity.floating.value).pow(0.004F) - 0.997F) * 0.5F) * currentHealth.regenAmountMp)
			ticksToRegen = (20F / currentHealth.regenSpeedMp).ceilToInt()
		}
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		setInteger(ENERGY_LEVEL_TAG, energyLevel.internal.value)
		setInteger(ENERGY_CAPACITY_TAG, energyBaseCapacity.internal.value)
		
		setEnum(HEALTH_STATUS_TAG, internalHealthStatus)
		setEnum(HEALTH_OVERRIDE_TAG, internalHealthOverride)
		
		if (isInactive){
			setBoolean(INACTIVE_TAG, true)
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		energyLevel = Internal(getInteger(ENERGY_LEVEL_TAG))
		energyBaseCapacity = Internal(getInteger(ENERGY_CAPACITY_TAG))
		
		internalHealthStatus = getEnum<HealthStatus>(HEALTH_STATUS_TAG) ?: HEALTHY
		internalHealthOverride = getEnum<HealthOverride>(HEALTH_OVERRIDE_TAG)
		
		isInactive = getBoolean(INACTIVE_TAG)
	}
	
	override fun hasFastRenderer(): Boolean = true
	override fun canRenderBreaking(): Boolean = false
}
