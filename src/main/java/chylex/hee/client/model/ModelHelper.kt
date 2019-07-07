package chylex.hee.client.model
import chylex.hee.client.util.MC
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.subtractY
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.EnumHandSide.RIGHT
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.abs
import kotlin.math.pow

object ModelHelper{
	@SideOnly(Side.CLIENT)
	fun getHandPosition(player: EntityPlayer, hand: EnumHand): Vec3d{
		val yawOffsetMp = (if (player.primaryHand == RIGHT) 1 else -1) * (if (hand == MAIN_HAND) 1 else -1)
		
		if (player === MC.player && MC.settings.thirdPersonView == 0){
			val pitch = MathHelper.wrapDegrees(player.rotationPitch)
			val yaw = MathHelper.wrapDegrees(player.rotationYaw)
			val fov = MC.settings.fovSetting
			
			return player
				.lookPosVec
				.subtractY(0.1 + (pitch.coerceIn(-90F, 45F) * 0.0034))
				.add(Vec3.fromYaw(yaw + (yawOffsetMp * fov * 0.6F)).scale(0.25))
				.add(Vec3.fromYaw(yaw + 165F - (fov - 90F) / 2F).scale(abs(pitch.coerceIn(0F, 90F) / 90.0).pow(1.5) * (0.3 - abs(fov - 90.0) / 600F)))
			
			// TODO kinda weird and inaccurate, maybe use the camera transformations somehow?
		}
		else{
			val handOffset = if (player.isSneaking) 1.15 else 0.75
			
			return player
				.lookPosVec
				.subtractY(handOffset)
				.add(Vec3.fromYaw(player.renderYawOffset + yawOffsetMp * 39F).scale(0.52))
		}
	}
}