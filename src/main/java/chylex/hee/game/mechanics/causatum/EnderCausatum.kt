package chylex.hee.game.mechanics.causatum
import chylex.hee.game.mechanics.causatum.EnderCausatum.CausatumCapability.Provider
import chylex.hee.system.Resource
import chylex.hee.system.capability.CapabilityProvider
import chylex.hee.system.capability.PlayerCapabilityHandler
import chylex.hee.system.capability.PlayerCapabilityHandler.IPlayerPersistentCapability
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getCap
import chylex.hee.system.util.register
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable

object EnderCausatum{
	fun register(){
		CapabilityManager.INSTANCE.register<CausatumCapability>()
		PlayerCapabilityHandler.register(Handler)
	}
	
	fun getStage(player: EntityPlayer): CausatumStage{
		return Handler.retrieve(player).stage
	}
	
	fun triggerStage(player: EntityPlayer, newStage: CausatumStage, force: Boolean = false): Boolean{
		with(Handler.retrieve(player)){
			if (newStage > stage || force){
				stage = newStage
				return true
			}
		}
		
		return false
	}
	
	// Capability handling
	
	private object Handler : IPlayerPersistentCapability<CausatumCapability>{
		override val key = Resource.Custom("causatum")
		override fun provide(player: EntityPlayer) = Provider()
		override fun retrieve(player: EntityPlayer) = player.getCap(CAP_CAUSATUM)
	}
	
	@JvmStatic
	@CapabilityInject(CausatumCapability::class)
	private var CAP_CAUSATUM: Capability<CausatumCapability>? = null
	
	private class CausatumCapability private constructor() : INBTSerializable<TagCompound>{
		var stage = CausatumStage.S0_INITIAL
		
		override fun serializeNBT() = TagCompound().apply {
			setString("Stage", stage.key)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = with(nbt){
			stage = CausatumStage.fromKey(getString("Stage")) ?: stage
		}
		
		class Provider : CapabilityProvider<CausatumCapability, TagCompound>(CAP_CAUSATUM, CausatumCapability())
	}
}
