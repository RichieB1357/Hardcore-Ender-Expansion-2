package chylex.hee.init
import chylex.hee.HEE
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import net.minecraftforge.fml.common.network.NetworkRegistry

object ModGuiHandler : IGuiHandler{
	fun initialize(){
		NetworkRegistry.INSTANCE.registerGuiHandler(HEE, this)
	}
	
	// Types
	
	enum class GuiType(
		val createInterface: (player: EntityPlayer, Int, Int, Int) -> Any,
		val createContainer: (player: EntityPlayer, Int, Int, Int) -> Any
	){
		;
		
		fun open(player: EntityPlayer, x: Int = 0, y: Int = 0, z: Int = 0){
			player.openGui(HEE, ordinal, player.world, x, y, z)
		}
	}
	
	// Overrides
	
	override fun getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) =
		GuiType.values().getOrNull(id)?.createInterface?.invoke(player, x, y, z)
	
	override fun getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) =
		GuiType.values().getOrNull(id)?.createContainer?.invoke(player, x, y, z)
}