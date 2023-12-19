package got.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class GOTModelBootsTest extends GOTModelBiped {
	private final ModelRenderer RightBoot;
	private final ModelRenderer LeftBoot;

	public GOTModelBootsTest() {
		textureWidth = 64;
		textureHeight = 32;

		RightBoot = new ModelRenderer(this);
		RightBoot.setRotationPoint(-1.9F, 12.0F, 0.0F);
		RightBoot.cubeList.add(new ModelBox(RightBoot, 1, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.5F));

		LeftBoot = new ModelRenderer(this);
		LeftBoot.setRotationPoint(3.8F, 0.0F, 0.0F);
		RightBoot.addChild(LeftBoot);
		LeftBoot.cubeList.add(new ModelBox(LeftBoot, 1, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.5F));
		bipedHeadwear.cubeList.clear();
		bipedHead.cubeList.clear();
		bipedBody.cubeList.clear();
		bipedRightArm.cubeList.clear();
		bipedLeftArm.cubeList.clear();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}