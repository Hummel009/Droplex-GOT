package got.client.render.animal;

import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class GOTRenderDragon8 extends GOTRenderDragon {
	@Override
	public ResourceLocation getEggTexture() {
		return new ResourceLocation("got:textures/entity/animal/" + getClass().getSimpleName().replace("GOTRender", "").toLowerCase(Locale.ROOT) + "/egg.png");
	}

	@Override
	public ResourceLocation getSaddleTexture() {
		return new ResourceLocation("got:textures/entity/animal/" + getClass().getSimpleName().replace("GOTRender", "").toLowerCase(Locale.ROOT) + "/saddle.png");
	}

	@Override
	public ResourceLocation getGlowTexture() {
		return new ResourceLocation("got:textures/entity/animal/" + getClass().getSimpleName().replace("GOTRender", "").toLowerCase(Locale.ROOT) + "/glow.png");
	}

	@Override
	public ResourceLocation getBodyTexture() {
		return new ResourceLocation("got:textures/entity/animal/" + getClass().getSimpleName().replace("GOTRender", "").toLowerCase(Locale.ROOT) + "/body.png");
	}
}