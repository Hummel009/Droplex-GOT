package got.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class GOTModelArrynHeavyChestplate extends GOTModelBiped {
	private final ModelRenderer BodyLayer_r1;
	private final ModelRenderer BodyLayer_r2;
	private final ModelRenderer BodyLayer_r3;

	public GOTModelArrynHeavyChestplate() {
		textureWidth = 128;
		textureHeight = 128;

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotationAngle(bipedLeftArm, -0.1745F, 0.0F, 0.0F);	
		bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 0, 53, -1.0F, -1.0F, -2.0F, 4, 11, 4, 0.0F));
		bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 26, 62, -1.0F, 8.0F, -2.0F, 4, 2, 4, 0.2F ));
		bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 26, 62, -1.0F, 4.0F, -2.0F, 1, 2, 4, 0.3F));
		bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 52, 37, -1.0F, -1.0F, -2.0F, 4, 8, 4, 0.2F));

		BodyLayer_r1 = new ModelRenderer(this);
		BodyLayer_r1.setRotationPoint(7.0F, 9.0F, 8.5F);
		BodyLayer_r1.mirror = true;
		bipedLeftArm.addChild(BodyLayer_r1);
		setRotationAngle(BodyLayer_r1, 0.0F, 0.0F, -0.3927F);
		BodyLayer_r1.cubeList.add(new ModelBox(BodyLayer_r1, 50, 58, -3.0F, -11.0F, -11.5F, 3, 1, 6, 0.0F));
		BodyLayer_r1.cubeList.add(new ModelBox(BodyLayer_r1, 11, 62, 0.0F, -14.0F, -11.5F, 1, 3, 6, 0.1F));
		BodyLayer_r1.cubeList.add(new ModelBox(BodyLayer_r1, 48, 28, -4.0F, -13.6F, -11.0F, 5, 3, 5, 0.0F));

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		setRotationAngle(bipedRightArm, -0.1745F, 0.0F, 0.0F);
		bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 0, 53, -3.0F, -1.0F, -2.0F, 4, 11, 4, 0.0F));
		bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 26, 62, -3.0F, 8.0F, -2.0F, 4, 2, 4, 0.2F));
		bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 26, 62, -3.0F, 4.0F, -2.0F, 1, 2, 4, 0.3F));
		bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 52, 37, -3.0F, -1.0F, -2.0F, 4, 8, 4, 0.2F));

		BodyLayer_r2 = new ModelRenderer(this);
		BodyLayer_r2.setRotationPoint(7.0F, 9.0F, 8.5F);
		BodyLayer_r2.mirror = true;
		bipedRightArm.addChild(BodyLayer_r2);
		setRotationAngle(BodyLayer_r2, 0.0F, 0.0F, 0.3927F);	
		BodyLayer_r2.cubeList.add(new ModelBox(BodyLayer_r2, 50, 58, -13.0F, -6.0F, -11.5F, 3, 1, 6, 0.0F));
		BodyLayer_r2.cubeList.add(new ModelBox(BodyLayer_r2, 11, 62, -14.0F, -9.0F, -11.5F, 1, 3, 6, 0.1F));
		BodyLayer_r2.cubeList.add(new ModelBox(BodyLayer_r2, 48, 28, -14.0F, -8.6F, -11.0F, 5, 3, 5, 0.0F));

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.cubeList.add(new ModelBox(bipedBody, 30, 34, -4.0F, 0.0F, -2.0F, 7, 7, 3, 0.22F));
		bipedBody.cubeList.add(new ModelBox(bipedBody, 0, 16, -4.0F, 0.0F, -2.0F, 8, 11, 5, 0.22F));

		BodyLayer_r3 = new ModelRenderer(this);
		BodyLayer_r3.setRotationPoint(-4.0F, 11.0F, 8.5F);
		bipedBody.addChild(BodyLayer_r3);
		setRotationAngle(BodyLayer_r3, -0.3927F, 0.0F, 0.0F);
		BodyLayer_r3.cubeList.add(new ModelBox(BodyLayer_r3, 79, 13, 7.0F, -18.2F, -6.0F, 0, 19, 7, 0.0F));
		BodyLayer_r3.cubeList.add(new ModelBox(BodyLayer_r3, 79, 13, 1.0F, -18.2F, -6.0F, 0, 19, 7, 0.0F));
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