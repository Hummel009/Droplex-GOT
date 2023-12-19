package got.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class GOTModelHelmetTest extends GOTModelBiped {

	public GOTModelHelmetTest() {
		textureWidth = 64;
		textureHeight = 32;

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.75F));
		bipedHead.cubeList.add(new ModelBox(bipedHead, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 1.1F));
		bipedHead.cubeList.add(new ModelBox(bipedHead, 32, 21, -4.0F, -10.0F, -4.0F, 8, 3, 8, 0.0F));
		bipedHead.cubeList.add(new ModelBox(bipedHead, 34, 22, -3.5F, -11.3279F, -3.5076F, 7, 3, 7, -0.4F));
		bipedHead.cubeList.add(new ModelBox(bipedHead, 23, 27, -1.5F, -11.9279F, -1.5076F, 3, 2, 3, 0.0F));
		bipedHead.cubeList.add(new ModelBox(bipedHead, 11, 29, -0.5F, -12.9279F, -0.5076F, 1, 2, 1, -0.1F));
		bipedHeadwear.cubeList.clear();
		bipedBody.cubeList.clear();
		bipedRightArm.cubeList.clear();
		bipedLeftArm.cubeList.clear();
		bipedRightLeg.cubeList.clear();
		bipedLeftLeg.cubeList.clear();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}