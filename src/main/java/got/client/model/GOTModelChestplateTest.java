package got.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class GOTModelChestplateTest extends GOTModelBiped {
	private final ModelRenderer RightArm_r1;
	private final ModelRenderer RightArm_r2;

	public GOTModelChestplateTest() {
		textureWidth = 64;
		textureHeight = 32;

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);	
		bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.5F));
		bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 0, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.8F));

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.49F));

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.mirror = true;
		bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.49F));
		
		RightArm_r1 = new ModelRenderer(this);
		RightArm_r1.setRotationPoint(1.0F, -0.5F, 0.0F);
		RightArm_r1.mirror = true;
		bipedLeftArm.addChild(RightArm_r1);
		setRotationAngle(RightArm_r1, 0.0F, 0.0F, 0.1309F);
		RightArm_r1.cubeList.add(new ModelBox(RightArm_r1, 40, 0, -2.75F, -2.5F, -3.0F, 6, 5, 6, 0.0F));

		RightArm_r2 = new ModelRenderer(this);
		RightArm_r2.setRotationPoint(-1.0F, -0.5F, 0.0F);
		bipedRightArm.addChild(RightArm_r2);
		setRotationAngle(RightArm_r2, 0.0F, 0.0F, -0.1309F);
		RightArm_r2.cubeList.add(new ModelBox(RightArm_r2, 40, 0, -3.25F, -2.5F, -3.0F, 6, 5, 6, 0.0F));
		bipedHeadwear.cubeList.clear();
		bipedHead.cubeList.clear();
		bipedRightLeg.cubeList.clear();
		bipedLeftLeg.cubeList.clear();
	}
	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}