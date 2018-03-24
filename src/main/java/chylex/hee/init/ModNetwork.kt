package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.network.BaseClientPacket
import chylex.hee.network.BaseServerPacket
import chylex.hee.network.IPacket
import gnu.trove.impl.Constants.DEFAULT_CAPACITY
import gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR
import gnu.trove.map.hash.TByteObjectHashMap
import gnu.trove.map.hash.TObjectByteHashMap
import io.netty.buffer.Unpooled
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLEventChannel
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import net.minecraftforge.fml.relauncher.Side.CLIENT
import net.minecraftforge.fml.relauncher.Side.SERVER
import java.util.function.Supplier

object ModNetwork{
	fun initialize(){
		if (::network.isInitialized){
			throw UnsupportedOperationException("cannot initialize ModNetwork multiple times")
		}
		
		network = NetworkRegistry.INSTANCE.newEventDrivenChannel(HardcoreEnderExpansion.ID)
		network.register(this)
		
		for((cls, constructor) in ModPackets.getAllPackets()){
			val id = mapPacketIdToConstructor.size().toByte()
			mapPacketIdToConstructor.put(id, constructor)
			mapPacketClassToId.put(cls, id)
		}
	}
	
	// Internal
	
	private const val MISSING_ID: Byte = -1
	
	private lateinit var network: FMLEventChannel
	
	private val mapPacketIdToConstructor = TByteObjectHashMap<Supplier<IPacket>>()
	private val mapPacketClassToId = TObjectByteHashMap<Class<out IPacket>>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, MISSING_ID)
	
	// Packet receiving
	
	@SubscribeEvent
	fun onClientPacket(e: ClientCustomPacketEvent){
		readPacket(e.packet).handle(CLIENT, HardcoreEnderExpansion.proxy.getClientSidePlayer()!!)
	}
	
	@SubscribeEvent
	fun onServerPacket(e: ServerCustomPacketEvent){
		readPacket(e.packet).handle(SERVER, (e.handler as NetHandlerPlayServer).player)
	}
	
	// Packet sending
	
	fun sendToServer(packet: BaseServerPacket){
		network.sendToServer(writePacket(packet))
	}
	
	fun sendToAll(packet: BaseClientPacket){
		network.sendToAll(writePacket(packet))
	}
	
	fun sendToPlayer(packet: BaseClientPacket, player: EntityPlayerMP){
		network.sendTo(writePacket(packet), player)
	}
	
	fun sendToDimension(packet: BaseClientPacket, dimension: Int){
		network.sendToDimension(writePacket(packet), dimension)
	}
	
	fun sendToAllAround(packet: BaseClientPacket, point: TargetPoint){
		network.sendToAllAround(writePacket(packet), point)
	}
	
	// Packet wrapping
	
	private fun readPacket(packet: FMLProxyPacket): IPacket{
		val buffer = packet.payload()
		val id = buffer.readByte()
		
		val constructor = mapPacketIdToConstructor[id] ?: throw IllegalArgumentException("unknown packet id: $id")
		return constructor.get().also { it.read(PacketBuffer(buffer.slice())) }
	}
	
	private fun writePacket(packet: IPacket): FMLProxyPacket{
		val id = mapPacketClassToId[packet::class.java]
		
		if (id == MISSING_ID){
			throw IllegalArgumentException("packet is not registered: ${packet.javaClass.simpleName}")
		}
		
		return PacketBuffer(Unpooled.buffer()).let {
			it.writeByte(id.toInt())
			packet.write(it)
			FMLProxyPacket(it, HardcoreEnderExpansion.ID)
		}
	}
}