package chylex.hee.network.client
import chylex.hee.game.item.trinket.ITrinketItem
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class PacketClientTrinketBreak() : BaseClientPacket(){
	constructor(target: Entity, item: Item) : this(){
		this.entityId = target.entityId
		this.item = item
	}
	
	private var entityId: Int? = null
	private lateinit var item: Item
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(entityId!!)
		writeInt(Item.getIdFromItem(item))
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		entityId = readInt()
		item = Item.getItemById(readInt())
	}
	
	@SideOnly(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		entityId?.let(player.world::getEntityByID)?.let {
			if (it === player){
				Minecraft.getMinecraft().entityRenderer.displayItemActivation(ItemStack(item))
			}
			
			(item as? ITrinketItem)?.spawnClientTrinketBreakFX(it)
		}
	}
}