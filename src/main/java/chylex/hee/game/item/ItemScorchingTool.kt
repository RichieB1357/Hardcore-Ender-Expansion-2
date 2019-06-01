package chylex.hee.game.item
import chylex.hee.game.block.util.IBlockHarvestToolCheck
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.repair.RepairInstance
import chylex.hee.game.item.util.CustomToolMaterial
import chylex.hee.game.mechanics.ScorchingFortune
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.ParticleFlameCustom.Data
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.isNotEmpty
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.CriticalHitEvent
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import net.minecraftforge.fml.common.eventhandler.Event.Result.DENY
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Random

class ItemScorchingTool(private val toolClass: String) : ItemTool(CustomToolMaterial.SCORCHING, emptySet()), ICustomRepairBehavior{
	companion object{
		private val PARTICLE_MINING = ParticleSpawnerCustom(
			type = ParticleFlameCustom,
			data = Data(maxAge = 6),
			pos = InBox(0.7F)
		)
		
		private fun PARTICLE_HITTING(target: Entity) = ParticleSpawnerCustom(
			type = ParticleFlameCustom,
			data = Data(maxAge = 4),
			pos = Constant(0.2F, UP) + InBox(target, 0.4F)
		)
		
		@JvmStatic
		val FX_BLOCK_BREAK = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_MINING.spawn(Point(pos, 15), rand)
			}
		}
		
		@JvmStatic
		val FX_ENTITY_HIT = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				PARTICLE_HITTING(entity).spawn(Point(entity, heightMp = 0.5F, amount = 20), rand)
			}
		}
	}
	
	init{
		setHarvestLevel(toolClass, toolMaterial.harvestLevel)
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	// Mining behavior
	
	private fun getHeldScorchingTool(player: EntityPlayer): ItemStack?{
		return player.getHeldItem(MAIN_HAND).takeIf { it.item === this }
	}
	
	private fun isBlockValid(state: IBlockState): Boolean{
		if (!ScorchingFortune.canSmelt(state)){
			return false
		}
		
		val block = state.block
		
		if (block is IBlockHarvestToolCheck){
			return block.canHarvestUsing(toolClass, toolMaterial.harvestLevel)
		}
		
		val harvestTool = block.getHarvestTool(state)
		return harvestTool == null || (toolClass == harvestTool && toolMaterial.harvestLevel >= block.getHarvestLevel(state))
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: IBlockState): Float{
		return if (isBlockValid(state))
			efficiency
		else
			0.25F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: IBlockState, pos: BlockPos, entity: EntityLivingBase): Boolean{
		if (!world.isRemote && state.getBlockHardness(world, pos) != 0F){
			if (isBlockValid(state)){
				stack.damageItem(1, entity)
				PacketClientFX(FX_BLOCK_BREAK, FxBlockData(pos)).sendToAllAround(world, pos, 32.0)
			}
			else{
				stack.damageItem(2, entity)
			}
		}
		
		return true
	}
	
	@SubscribeEvent(priority = LOWEST)
	fun onBreakSpeed(e: BreakSpeed){
		if (e.entity.world.isRemote && getHeldScorchingTool(e.entityPlayer) != null && isBlockValid(e.state)){
			PARTICLE_MINING.spawn(Point(e.pos, 5), itemRand)
		}
	}
	
	@SubscribeEvent
	fun onHarvestDrops(e: HarvestDropsEvent){
		if (e.harvester?.let(::getHeldScorchingTool) != null && isBlockValid(e.state)){ // TODO not checking drops.isNotEmpty to support Vines, is that a problem?
			val fortuneStack = ScorchingFortune.createSmeltedStack(e.state, itemRand)
			
			if (fortuneStack.isNotEmpty){
				e.drops.clear()
				e.drops.add(fortuneStack)
			}
		}
	}
	
	// Hitting behavior
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		target.setFire(1)
		PacketClientFX(FX_ENTITY_HIT, FxEntityData(target)).sendToAllAround(target, 32.0)
		
		stack.damageItem(1, attacker)
		return true
	}
	
	@SubscribeEvent
	fun onCriticalHit(e: CriticalHitEvent){
		if (getHeldScorchingTool(e.entityPlayer) != null){
			e.result = DENY
		}
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return toRepair.isItemDamaged && (repairWith.item === toolMaterial.repairItemStack.item || repairWith.item === ModItems.INFERNIUM)
	}
	
	override fun onRepairUpdate(instance: RepairInstance) = with(instance){
		if (ingredient.item === toolMaterial.repairItemStack.item){
			repairFully()
		}
		else{
			repairPercent(22)
		}
		
		repairCost = repairCost * 2 + 1
	}
}
