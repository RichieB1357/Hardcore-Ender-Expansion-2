package chylex.hee.proxy
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
	
	override fun onPreInit(){
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun onInit(){
		ClientCommandHandler.instance.registerCommand(HeeClientCommand)
	}
	
	@SubscribeEvent
	fun onModels(e: ModelRegistryEvent){
		fun register(vararg blocks: Block){
			for(itemblock in blocks.map(Item::getItemFromBlock)){
				ModelLoader.setCustomModelResourceLocation(itemblock, 0, ModelResourceLocation(itemblock.registryName!!, "inventory"))
			}
		}
		
		fun register(vararg items: Item){
			for(item in items){
				ModelLoader.setCustomModelResourceLocation(item, 0, ModelResourceLocation(item.registryName!!, "inventory"))
			}
		}
		
		register(
			ModBlocks.GLOOMROCK,
			ModBlocks.GLOOMROCK_BRICKS,
			ModBlocks.GLOOMROCK_BRICK_SLAB,
			ModBlocks.GLOOMROCK_BRICK_STAIRS,
			ModBlocks.GLOOMROCK_SMOOTH,
			ModBlocks.GLOOMROCK_SMOOTH_SLAB,
			ModBlocks.GLOOMROCK_SMOOTH_STAIRS,
			ModBlocks.GLOOMROCK_SMOOTH_RED,
			ModBlocks.GLOOMROCK_SMOOTH_ORANGE,
			ModBlocks.GLOOMROCK_SMOOTH_YELLOW,
			ModBlocks.GLOOMROCK_SMOOTH_GREEN,
			ModBlocks.GLOOMROCK_SMOOTH_CYAN,
			ModBlocks.GLOOMROCK_SMOOTH_BLUE,
			ModBlocks.GLOOMROCK_SMOOTH_PURPLE,
			ModBlocks.GLOOMROCK_SMOOTH_MAGENTA,
			ModBlocks.GLOOMROCK_SMOOTH_WHITE
		)
		
		register(
			ModItems.ETHEREUM,
			ModItems.ANCIENT_DUST,
			ModItems.ALTERATION_NEXUS
		)
	}
}
