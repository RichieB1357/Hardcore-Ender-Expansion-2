package chylex.hee.game.entity.living.controller
import chylex.hee.game.entity.lookPosVec
import chylex.hee.system.math.Quaternion
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.toPitch
import chylex.hee.system.math.toRadians
import chylex.hee.system.math.toYaw
import chylex.hee.system.migration.EntityLiving
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.controller.LookController
import net.minecraft.util.math.Vec3d
import kotlin.math.cos

class EntityLookSlerp(entity: EntityLiving, private val adjustmentSpeed: Float, maxInstantAngle: Float) : LookController(entity){
	private val maxInstantAngleCos = cos(maxInstantAngle.toRadians())
	
	override fun setLookPosition(x: Double, y: Double, z: Double, deltaYaw: Float, deltaPitch: Float){
		posX = x
		posY = y
		posZ = z
		isLooking = true
	}
	
	override fun setLookPositionWithEntity(entity: Entity, deltaYaw: Float, deltaPitch: Float){
		entity.lookPosVec.let { setLookPosition(it.x, it.y, it.z, deltaYaw, deltaPitch) }
	}
	
	override fun tick(){
		if (isLooking){
			isLooking = false
			
			val dir = mob.lookPosVec.directionTowards(Vec3d(posX, posY, posZ))
			
			if (Vec3d.fromPitchYaw(mob.rotationPitch, mob.rotationYawHead).dotProduct(dir) >= maxInstantAngleCos){
				mob.rotationYawHead = dir.toYaw()
				mob.rotationPitch = dir.toPitch()
			}
			else{
				val current = Quaternion.fromYawPitch(mob.rotationYawHead, mob.rotationPitch)
				val target = Quaternion.fromYawPitch(dir.toYaw(), dir.toPitch())
				
				val next = current.slerp(target, adjustmentSpeed)
				mob.rotationYawHead = next.rotationYaw
				mob.rotationPitch = next.rotationPitch
			}
		}
	}
}
