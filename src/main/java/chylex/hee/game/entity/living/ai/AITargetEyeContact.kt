package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseTargetFiltered
import chylex.hee.system.util.lookDirVec
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.inventory.EntityEquipmentSlot.HEAD
import net.minecraft.item.Item
import kotlin.math.abs
import kotlin.math.cos

class AITargetEyeContact<T : EntityLivingBase>(
	entity: EntityCreature,
	easilyReachableOnly: Boolean,
	targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	fieldOfView: Float,
	headRadius: Float,
	private val minStareTicks: Int
) : AIBaseTargetFiltered<T>(entity, true, easilyReachableOnly, targetClass, targetPredicate){
	private val fieldOfViewCos = cos((fieldOfView / 2F).toRadians())
	private val headRadiusSq = square(headRadius)
	private val headCenterOffset = headRadius * 0.5 // kinda arbitrary
	
	private val stareStarts = Object2LongArrayMap<T>(4).apply { defaultReturnValue(Long.MIN_VALUE) }
	
	override fun findTarget(): T?{
		val foundTargets = findSuitableTargets().filter(::isLookingIntoEyes).toList()
		
		if (foundTargets.isEmpty()){
			stareStarts.clear()
			return null
		}
		
		stareStarts.keys.retainAll(foundTargets)
		
		val currentTime = entity.world.totalWorldTime
		
		for(target in foundTargets){
			val stareStart = stareStarts.getLong(target)
			
			if (stareStart == Long.MIN_VALUE){
				stareStarts.put(target, currentTime) // kotlin indexer boxes the values
			}
			else if (currentTime - stareStart >= minStareTicks){
				stareStarts.clear()
				return target
			}
		}
		
		return null
	}
	
	private fun isLookingIntoEyes(target: T): Boolean{
		if (target.getItemStackFromSlot(HEAD).item === Item.getItemFromBlock(Blocks.PUMPKIN)){
			return false
		}
		
		val ownerLookDir = entity.lookDirVec
		val eyePosDiff = entity.lookPosVec.add(ownerLookDir.scale(headCenterOffset)).subtract(target.lookPosVec)
		val eyePosDiffNormalized = eyePosDiff.normalize()
		
		if (-ownerLookDir.dotProduct(eyePosDiffNormalized) < fieldOfViewCos){
			return false
		}
		
		val playerLookDot = target.lookDirVec.dotProduct(eyePosDiffNormalized)
		return abs(playerLookDot - 1.0) < (headRadiusSq / eyePosDiff.lengthSquared())
	}
}
