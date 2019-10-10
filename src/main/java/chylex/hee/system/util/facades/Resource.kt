package chylex.hee.system.util.facades
import chylex.hee.HEE
import net.minecraft.util.ResourceLocation

object Resource{
	private const val VANILLA = "minecraft"
	
	fun Vanilla(path: String) = ResourceLocation(VANILLA, path)
	fun Custom(path: String)  = ResourceLocation(HEE.ID, path)
	
	fun isVanilla(location: ResourceLocation) = location.namespace == VANILLA
	fun isCustom(location: ResourceLocation)  = location.namespace == HEE.ID
}