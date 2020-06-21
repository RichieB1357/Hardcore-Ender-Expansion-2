package chylex.hee.game.world.feature.basic.blobs.populators
import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.IBlobPopulator
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.math.RandomDouble
import java.util.Random

class BlobPopulatorShaveTop(
	private val height: RandomDouble
) : IBlobPopulator{
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator){
		val size = world.worldSize
		val shave = (size.maxY * height(rand)).floorToInt()
		
		for(pos in Pos(0, size.maxY - shave, 0).allInBoxMutable(size.maxPos)){
			world.markUnused(pos)
		}
	}
}
