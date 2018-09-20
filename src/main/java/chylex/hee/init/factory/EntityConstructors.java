package chylex.hee.init.factory;
import chylex.hee.game.entity.item.EntityItemIgneousRock;
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class EntityConstructors{
	private static final Map<Class<? extends Entity>, Function<World, ? extends Entity>> all = new HashMap<>();
	
	static{
		add(EntityItemIgneousRock.class, EntityItemIgneousRock::new);
		
		add(EntityProjectileSpatialDash.class, EntityProjectileSpatialDash::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> Function<World, T> get(Class<T> cls){
		return (Function<World, T>)all.get(cls);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends Entity> void add(Class<T> cls, Function<World, T> constructor){
		all.put(cls, constructor);
	}
	
	private EntityConstructors(){}
}