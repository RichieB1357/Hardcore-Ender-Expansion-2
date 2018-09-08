package chylex.hee.game.mechanics.damage
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemArmor
import net.minecraft.potion.PotionEffect
import net.minecraft.world.EnumDifficulty.EASY
import net.minecraft.world.EnumDifficulty.HARD
import net.minecraft.world.EnumDifficulty.NORMAL
import net.minecraft.world.EnumDifficulty.PEACEFUL
import kotlin.math.min
import kotlin.math.nextUp

interface IDamageProcessor{
	fun setup(properties: DamageProperties.Writer){}
	fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float = amount
	fun afterDamage(target: Entity, properties: DamageProperties.Reader){}
	
	companion object{ // TODO make static fields in kotlin 1.3 and use default methods
		const val CANCEL_DAMAGE = 0F
		
		// Difficulty or game mode
		
		val PEACEFUL_EXCLUSION = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target.world.difficulty != PEACEFUL || target !is EntityPlayer)
					amount
				else
					CANCEL_DAMAGE
			}
		}
		
		val PEACEFUL_KNOCKBACK = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target.world.difficulty != PEACEFUL || target !is EntityPlayer)
					amount
				else
					0F.nextUp()
			}
		}
		
		val DIFFICULTY_SCALING = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target !is EntityPlayer){
					amount
				}
				else when(target.world.difficulty!!){
					PEACEFUL -> amount * 0.4F
					EASY     -> amount * 0.7F
					NORMAL   -> amount
					HARD     -> amount * 1.4F
				}
			}
		}
		
		val DEAL_CREATIVE = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				properties.setDealCreative()
			}
		}
		
		// Equipment
		
		fun ARMOR_PROTECTION(allowShield: Boolean) = object: IDamageProcessor{
			override fun setup(properties: DamageProperties.Writer){
				if (allowShield){
					properties.setAllowArmorAndShield()
				}
				else{
					properties.setAllowArmor()
				}
			}
		}
		
		val NUDITY_DANGER = object: IDamageProcessor{
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				val bodyCoverageFactor = target.armorInventoryList.sumBy {
					if (it.item is ItemArmor)
						2
					else if (!it.isEmpty)
						1
					else
						0
				}
				
				return amount * when(bodyCoverageFactor){
					0 -> 2.5F
					1 -> 2.3F
					2 -> 2.1F
					3 -> 1.9F
					4 -> 1.7F
					5 -> 1.5F
					6 -> 1.3F
					7 -> 1.15F
					else -> 1F
				}
			}
		}
		
		// Status effects
		
		val POTION_PROTECTION = object: IDamageProcessor{
			/**
			 * [EntityLivingBase.applyPotionDamageCalculations]
			 */
			override fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float{
				return if (target !is EntityLivingBase)
					amount
				else
					amount * (1F - (0.15F * min(5, target.getActivePotionEffect(MobEffects.RESISTANCE)?.amplifier?.plus(1) ?: 0)))
			}
		}
		
		fun STATUS(effect: PotionEffect) = object: IDamageProcessor{
			override fun afterDamage(target: Entity, properties: DamageProperties.Reader){
				if (target is EntityLivingBase){
					target.addPotionEffect(effect)
				}
			}
		}
	}
}
