package chylex.hee.system.util
import com.google.common.collect.Collections2
import net.minecraft.util.EnumFacing
import net.minecraft.util.Rotation
import java.util.Random

object Facing4 : List<EnumFacing> by EnumFacing.HORIZONTALS.toList(){
	private val allPermutations = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
}

object Facing6 : List<EnumFacing> by EnumFacing.VALUES.toList()

object Rotation4 : List<Rotation> by Rotation.values().toList(){
	private val allPermutations = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
}
