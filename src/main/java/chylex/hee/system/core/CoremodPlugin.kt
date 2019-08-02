package chylex.hee.system.core
import chylex.hee.system.core.transformers.TransformBlockChorusFlower
import chylex.hee.system.core.transformers.TransformChunk
import chylex.hee.system.core.transformers.TransformEntityMob
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions

@Name("Hardcore Ender Expansion Coremod")
@TransformerExclusions("chylex.hee.system.core", "kotlin.")
class CoremodPlugin : IFMLLoadingPlugin{
	override fun getASMTransformerClass() = arrayOf(
		TransformBlockChorusFlower::class.java.name,
		TransformChunk::class.java.name,
		TransformEntityMob::class.java.name
	)
	
	override fun getAccessTransformerClass() = null
	override fun getSetupClass(): String? = null
	override fun getModContainerClass() = null
	override fun injectData(data: MutableMap<String, Any>?){}
	
}
