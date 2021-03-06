package chylex.hee.game.world.feature.basic.caves.impl
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.feature.basic.caves.ICaveCarver
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.random.nextFloat
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.pow

class CaveCarverEllipsoid(
	private val radiusMpX: Double = 1.0,
	private val radiusMpY: Double = 1.0,
	private val radiusMpZ: Double = 1.0,
	private val powerX: Double = 2.0,
	private val powerY: Double = 2.0,
	private val powerZ: Double = 2.0,
	private val maxRandomRadiusReduction: Float = 0F
) : ICaveCarver{
	constructor(
		radiusMpX: Double = 1.0,
		radiusMpY: Double = 1.0,
		radiusMpZ: Double = 1.0,
		powerXYZ: Double = 2.0,
		maxRandomRadiusReduction: Float = 0F
	) : this(radiusMpX, radiusMpY, radiusMpZ, powerXYZ, powerXYZ, powerXYZ, maxRandomRadiusReduction)
	
	override fun carve(world: SegmentedWorld, center: Vec3d, radius: Double, placer: IBlockPlacer): Boolean{
		val rand = world.rand
		
		val radiusX = radius * radiusMpX
		val radiusY = radius * radiusMpY
		val radiusZ = radius * radiusMpZ
		
		val carveCenter = Pos(center)
		var anySuccess = false
		
		for(offset in BlockPos.ZERO.allInCenteredBoxMutable(radiusX, radiusY, radiusZ)){
			val distX = (abs(offset.x) / radiusX).pow(powerX)
			val distY = (abs(offset.y) / radiusY).pow(powerY)
			val distZ = (abs(offset.z) / radiusZ).pow(powerZ)
			
			if (distX + distY + distZ <= 1F - rand.nextFloat(0F, maxRandomRadiusReduction) && placer.place(world, carveCenter.add(offset))){
				anySuccess = true
			}
		}
		
		return anySuccess
	}
}
