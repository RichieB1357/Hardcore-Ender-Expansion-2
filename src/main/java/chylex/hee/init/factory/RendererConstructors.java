package chylex.hee.init.factory;
import chylex.hee.client.render.entity.RenderEntityItemNoBob;
import chylex.hee.client.render.entity.RenderEntityMobAbstractEnderman;
import chylex.hee.client.render.entity.RenderEntityMobUndread;
import chylex.hee.client.render.entity.RenderEntityMobVillagerDying;
import chylex.hee.client.render.entity.RenderEntityNothing;
import chylex.hee.client.render.entity.RenderEntityProjectileEyeOfEnder;
import chylex.hee.client.render.entity.RenderEntityTokenHolder;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import java.util.HashMap;
import java.util.Map;

public final class RendererConstructors{
	private static final Map<Class<? extends Render>, IRenderFactory> all = new HashMap<>();
	
	static{
		all.put(RenderEntityItemNoBob.class, RenderEntityItemNoBob::new);
		all.put(RenderEntityTokenHolder.class, RenderEntityTokenHolder::new);
		
		all.put(RenderEntityMobAbstractEnderman.class, RenderEntityMobAbstractEnderman::new);
		all.put(RenderEntityMobUndread.class, RenderEntityMobUndread::new);
		all.put(RenderEntityMobVillagerDying.class, RenderEntityMobVillagerDying::new);
		
		all.put(RenderEntityProjectileEyeOfEnder.class, RenderEntityProjectileEyeOfEnder::new);
		
		all.put(RenderEntityNothing.class, RenderEntityNothing::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity, R extends Render<? super T>> IRenderFactory get(Class<R> cls){
		return (IRenderFactory<T>)all.get(cls);
	}
}
