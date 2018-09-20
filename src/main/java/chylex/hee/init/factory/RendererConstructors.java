package chylex.hee.init.factory;
import chylex.hee.game.render.entity.RenderEntityItemNoBob;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import java.util.HashMap;
import java.util.Map;

public final class RendererConstructors{
	private static final Map<Class<? extends Render>, IRenderFactory> all = new HashMap<>();
	
	static{
		all.put(RenderEntityItemNoBob.class, RenderEntityItemNoBob::new);
	}
	
	public static <T extends Entity, R extends Render<? super T>> IRenderFactory get(Class<R> cls){
		return (IRenderFactory<T>)all.get(cls);
	}
}