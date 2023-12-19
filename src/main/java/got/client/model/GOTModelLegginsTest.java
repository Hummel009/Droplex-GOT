package got.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class GOTModelLegginsTest extends GOTModelBiped {


	public GOTModelLegginsTest() {
		textureWidth = 64;
		textureHeight = 32;

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		bipedRightLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F));
		bipedRightLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.7F));
			
		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(-3.8F, 0.0F, 0.0F);	
		bipedLeftLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F));
		bipedLeftLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.7F));
		
		bipedHeadwear.cubeList.clear();
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