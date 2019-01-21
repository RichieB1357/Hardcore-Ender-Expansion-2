package chylex.hee.game.mechanics.energy
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.entity.TileEntityEnergyCluster.LeakType
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.nextInt
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class ProximityHandler(private val cluster: TileEntityEnergyCluster) : INBTSerializable<NBTTagCompound>{
	private companion object{
		private const val AFFECTED_TAG = "Affected"
		private const val LEAK_TICKS_TAG = "TicksToLeak"
		
		private const val REFRESH_INTERVAL_TICKS = 20 * 3
		private const val INSTABILITY_INTERVAL_TICKS = 20 * 15
		
		private const val MIN_CLUSTERS_FOR_PROXIMITY = 2
		
		private fun nextLeakInterval(rand: Random, proximityClusterCount: Int): Int{
			return rand.nextInt(500, 900) - (rand.nextInt(25, 40) * min(8, proximityClusterCount - MIN_CLUSTERS_FOR_PROXIMITY))
		}
	}
	
	var affectedByProximity = false
		private set
	
	private var lastProximityClusterCount = 0
	private var ticksToRefresh = 0
	private var ticksToLeak = 0
	
	private val instabilityPerTrigger: UShort
		get(){
			val level = cluster.energyLevel.floating.value
			val capacity = cluster.energyBaseCapacity.floating.value
			
			return (4F + (3F * level / capacity) + capacity.pow(1.1F)).ceilToInt().toUShort()
		}
	
	private fun recalculateProximityClusterCount(){
		val (myChunkX, myChunkZ) = ChunkPos(cluster.pos)
		
		lastProximityClusterCount = cluster.world.loadedTileEntityList.count {
			it is TileEntityEnergyCluster &&
			it !== cluster &&
			it.currentHealth.affectedByProximity &&
			ChunkPos(it.pos).let { other -> abs(myChunkX - other.x) <= 2 && abs(myChunkZ - other.z) <= 2 }
		}
	}
	
	fun reset(){
		affectedByProximity = false
		ticksToRefresh = 0
	}
	
	fun tick(){
		if (--ticksToRefresh < 0){
			ticksToRefresh = REFRESH_INTERVAL_TICKS
			
			recalculateProximityClusterCount()
			val newProximityStatus = lastProximityClusterCount >= MIN_CLUSTERS_FOR_PROXIMITY
			
			if (affectedByProximity != newProximityStatus){
				affectedByProximity = newProximityStatus
				ticksToLeak = nextLeakInterval(cluster.world.rand, lastProximityClusterCount)
			}
		}
		
		if (!affectedByProximity){
			return
		}
		
		val world = cluster.world
		
		if (world.totalWorldTime % INSTABILITY_INTERVAL_TICKS == 0L){
			val pos = cluster.pos
			
			with(Instability.get(world)){
				resetActionMultiplier(pos)
				triggerAction(instabilityPerTrigger, pos)
			}
		}
		
		if (--ticksToLeak < 0){
			ticksToLeak = nextLeakInterval(world.rand, lastProximityClusterCount)
			cluster.leakEnergy(maxOf(Units(1), cluster.baseLeakSize * 0.75F), LeakType.PROXIMITY)
		}
	}
	
	override fun serializeNBT() = NBTTagCompound().apply {
		setBoolean(AFFECTED_TAG, affectedByProximity)
		setInteger(LEAK_TICKS_TAG, ticksToLeak)
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		affectedByProximity = getBoolean(AFFECTED_TAG)
		ticksToLeak = getInteger(LEAK_TICKS_TAG)
	}
}