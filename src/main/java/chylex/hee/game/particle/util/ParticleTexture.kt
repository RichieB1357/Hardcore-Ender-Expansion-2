package chylex.hee.game.particle.util
import chylex.hee.HEE
import chylex.hee.system.Resource
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
object ParticleTexture{
	lateinit var PIXEL: TextureAtlasSprite private set
	lateinit var STAR: TextureAtlasSprite private set
	
	private val TEX_PIXEL = Resource.Custom("particle/pixel")
	private val TEX_STAR = Resource.Custom("particle/star")
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		with(e.map){
			registerSprite(TEX_PIXEL)
			registerSprite(TEX_STAR)
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPost(e: TextureStitchEvent.Post){
		with(e.map){
			PIXEL = getAtlasSprite(TEX_PIXEL.toString())
			STAR = getAtlasSprite(TEX_STAR.toString())
		}
	}
}
