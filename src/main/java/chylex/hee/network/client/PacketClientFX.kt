package chylex.hee.network.client
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.entity.item.EntityItemRevitalizationSubstance
import chylex.hee.game.entity.living.enderman.EndermanTeleportHandler
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.item.ItemCompost
import chylex.hee.game.item.ItemRevitalizationSubstance
import chylex.hee.game.item.ItemTableLink
import chylex.hee.game.mechanics.scorching.ScorchingHelper
import chylex.hee.game.mechanics.table.TableParticleHandler
import chylex.hee.game.world.util.Teleporter
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.Debug
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class PacketClientFX<T : IFxData>() : BaseClientPacket(){
	private companion object{
		private val RAND = Random()
		
		private val HANDLERS = arrayOf(
			Teleporter.FX_TELEPORT,
			ScorchingHelper.FX_BLOCK_BREAK,
			ScorchingHelper.FX_ENTITY_HIT,
			TableParticleHandler.FX_PROCESS_PEDESTALS,
			TableParticleHandler.FX_DRAIN_CLUSTER,
			IBlockDeathFlowerDecaying.FX_HEAL,
			BlockDragonEggOverride.FX_BREAK,
			BlockEnderGooPurified.FX_PLACE,
			TileEntityTablePedestal.FX_ITEM_UPDATE,
			ItemAbstractEnergyUser.FX_CHARGE,
			ItemCompost.FX_USE,
			ItemRevitalizationSubstance.FX_FAIL,
			ItemTableLink.FX_USE,
			EntityFallingObsidian.FX_FALL,
			EntityItemCauldronTrigger.FX_RECIPE_FINISH,
			EntityItemIgneousRock.FX_BLOCK_SMELT,
			EntityItemIgneousRock.FX_ENTITY_BURN,
			EntityItemRevitalizationSubstance.FX_REVITALIZE_GOO,
			EntityProjectileSpatialDash.FX_EXPIRE,
			EndermanTeleportHandler.FX_TELEPORT_FAIL,
			EndermanTeleportHandler.FX_TELEPORT_OUT_OF_WORLD
		)
	}
	
	// Instance
	
	constructor(handler: IFxHandler<T>, data: T) : this(){
		this.handler = handler
		this.data = data
	}
	
	private lateinit var handler: IFxHandler<T>
	private lateinit var data: IFxData
	
	private var buffer: ByteBuf? = null
	
	override fun write(buffer: ByteBuf){
		buffer.writeInt(HANDLERS.indexOf(handler))
		data.write(buffer)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun read(buffer: ByteBuf){
		val index = buffer.readInt()
		
		if (index == -1){
			if (Debug.enabled){
				throw IndexOutOfBoundsException("could not find FX handler")
			}
		}
		else{
			this.handler = HANDLERS[index] as IFxHandler<T>
			this.buffer = buffer.slice()
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		buffer?.let { handler.handle(it, player.world, RAND) }
	}
}
