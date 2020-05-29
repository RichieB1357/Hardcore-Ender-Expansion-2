package chylex.hee.client.render.territory.components
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d

class SkyCubeStatic(
	override val texture: ResourceLocation = DEFAULT_TEXTURE,
	override val color: Vec3d = DEFAULT_COLOR,
	override val alpha: Float = DEFAULT_ALPHA,
	override val rescale: Double = DEFAULT_RESCALE,
	override val distance: Double = DEFAULT_DISTANCE
) : SkyCubeBase()
