package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseTargetFiltered
import chylex.hee.system.util.AI_FLAG_NONE
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectExistingEntities
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase

class AITargetSwarmSwitch<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	rangeMultiplier: Float
) : AIBaseTargetFiltered<T>(entity, checkSight, easilyReachableOnly, targetClass, targetPredicate, mutexBits = AI_FLAG_NONE){
	private val rangeMultiplier = if (rangeMultiplier in 0F..1F) rangeMultiplier else throw IllegalArgumentException("rangeMultiplier must be between 0 and 1 (inclusive)")
	
	private var triggerRetarget = false
	
	fun triggerRetarget(){
		this.triggerRetarget = true
	}
	
	override fun findTarget(): T?{
		if (!triggerRetarget){
			return null
		}
		
		triggerRetarget = false
		
		val maxRange = targetDistance
		
		val world = entity.world
		val position = entity.posVec
		val currentTarget = entity.attackTarget
		
		val friendsInRange = world.selectExistingEntities.inRange(entity::class.java, position, maxRange * 2.0).filter { it != entity }
		val friendsAttackingCurrentTarget = friendsInRange.count { it.attackTarget === currentTarget }
		
		if (friendsAttackingCurrentTarget == 0){
			return null
		}
		
		val otherTargetsInRange = findSuitableTargets(rangeMultiplier).filter {
			it != currentTarget &&
			friendsInRange.count { friend -> friend.attackTarget === it } < friendsAttackingCurrentTarget
		}
		
		return entity.rng.nextItem(otherTargetsInRange.toList())
	}
}
