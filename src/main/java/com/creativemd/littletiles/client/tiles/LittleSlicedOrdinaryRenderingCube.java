package com.creativemd.littletiles.client.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.model.CreativeBakedQuad;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

public class LittleSlicedOrdinaryRenderingCube extends LittleRenderingCube {
	
	protected LittleDynamicCube dynamicCube;
	
	public LittleSlicedOrdinaryRenderingCube(CubeObject cube, LittleTileSlicedOrdinaryBox box, Block block, int meta) {
		super(cube, box, block, meta);
		dynamicCube = new LittleDynamicCube(this, box.slice, box.getSize());
	}
	
	@Override
	public CubeObject offset(BlockPos pos) {
		return new LittleSlicedOrdinaryRenderingCube(new CubeObject(minX - pos.getX(), minY - pos.getY(), minZ - pos.getZ(), maxX - pos.getX(), maxY - pos.getY(), maxZ - pos.getZ(), this), (LittleTileSlicedOrdinaryBox) this.box, block, meta);
	}
	
	@Override
	public void renderCubeLines(double x, double y, double z, float red, float green, float blue, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		LittleTileSize size = box.getSize();
		
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			if (box.slice.shouldRenderSide(facing, size))
				renderFaceLine(dynamicCube, facing, facing.getAxis() == box.slice.axis, red, green, blue, alpha);
		}
		
		GlStateManager.popMatrix();
	}
	
	public static void renderFaceLine(LittleDynamicCube dynamicCube, EnumFacing facing, boolean isTraingle, float red, float green, float blue, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
		
		Vector3f vec = new Vector3f();
		EnumFaceDirection face = EnumFaceDirection.getFacing(facing);
		
		for (int i = 0; i < 4; i++) {
			
			vec = dynamicCube.get(facing, face.getVertexInformation(i), vec);
			
			if (i == 0)
				bufferbuilder.pos(vec.x, vec.y, vec.z).color(red, green, blue, 0.0F).endVertex();
			bufferbuilder.pos(vec.x, vec.y, vec.z).color(red, green, blue, alpha).endVertex();
		}
		
		tessellator.draw();
	}
	
	@Override
	public void renderCubePreview(double x, double y, double z, ILittleTile iTile) {
		
		Vec3d color = ColorUtils.IntToVec(this.color);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
		
		GlStateManager.color((float) color.xCoord, (float) color.yCoord, (float) color.zCoord, (float) (Math.sin(System.nanoTime() / 200000000D) * 0.2 + 0.5) * iTile.getPreviewAlphaFactor());
		
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		
		LittleTileSize size = box.getSize();
		
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			if (box.slice.shouldRenderSide(facing, size))
				renderFace(dynamicCube, facing, facing.getAxis() == box.slice.axis, box.slice);
		}
		
		GlStateManager.popMatrix();
	}
	
	public static void renderFace(LittleDynamicCube cube, EnumFacing facing, boolean isTraingle, LittleSlice slice) {
		GL11.glBegin(GL11.GL_POLYGON);
		
		Vec3i normal = facing.getDirectionVec();
		GlStateManager.glNormal3f(normal.getX(), normal.getY(), normal.getZ());
		
		Vector3f vec = new Vector3f();
		EnumFaceDirection face = EnumFaceDirection.getFacing(facing);
		
		for (int i = 0; i < 4; i++) {
			vec = cube.get(facing, face.getVertexInformation(i), vec);
			
			GlStateManager.glVertex3f(vec.x, vec.y, vec.z);
		}
		
		GlStateManager.glEnd();
	}
	
	public boolean intersectsWithFace(EnumFacing facing, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, BlockPos offset) {
		switch (facing.getAxis()) {
		case X:
			return maxY > dynamicCube.defaultCube.minY - offset.getY() && minY < dynamicCube.defaultCube.maxY - offset.getY() && maxZ > dynamicCube.defaultCube.minZ - offset.getZ() && minZ < dynamicCube.defaultCube.maxZ - offset.getZ();
		case Y:
			return maxX > dynamicCube.defaultCube.minX - offset.getX() && minX < dynamicCube.defaultCube.maxX - offset.getX() && maxZ > dynamicCube.defaultCube.minZ - offset.getZ() && minZ < dynamicCube.defaultCube.maxZ - offset.getZ();
		case Z:
			return maxX > dynamicCube.defaultCube.minX - offset.getX() && minX < dynamicCube.defaultCube.maxX - offset.getX() && maxY > dynamicCube.defaultCube.minY - offset.getY() && minY < dynamicCube.defaultCube.maxY - offset.getY();
		}
		return false;
	}
	
	@Override
	public List<BakedQuad> getBakedQuad(IBlockAccess world, @Nullable BlockPos pos, BlockPos offset, IBlockState state, IBakedModel blockModel, EnumFacing facing, BlockRenderLayer layer, long rand, boolean overrideTint, int defaultColor) {
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		//LittleTileSize size = box.getSize();
		
		if (!box.slice.shouldRenderSide(facing, dynamicCube.defaultCube.getSize()))
			return Collections.emptyList();
		
		List<BakedQuad> blockQuads = getBakedQuad(world, blockModel, state, facing, pos, layer, rand);
		if (blockQuads.isEmpty())
			return Collections.emptyList();
		
		List<BakedQuad> quads = new ArrayList<>();
		
		boolean isTraingle = facing.getAxis() == box.slice.axis;
		Vector3f vec = new Vector3f();
		
		int color = this.color != -1 ? this.color : defaultColor;
		
		for (int i = 0; i < blockQuads.size(); i++) {
			BakedQuad oldQuad = blockQuads.get(i);
			
			int index = 0;
			int uvIndex = index + oldQuad.getFormat().getUvOffsetById(0) / 4;
			float tempMinX = Float.intBitsToFloat(oldQuad.getVertexData()[index]);
			float tempMinY = Float.intBitsToFloat(oldQuad.getVertexData()[index + 1]);
			float tempMinZ = Float.intBitsToFloat(oldQuad.getVertexData()[index + 2]);
			
			float tempU = Float.intBitsToFloat(oldQuad.getVertexData()[uvIndex]);
			float tempV = Float.intBitsToFloat(oldQuad.getVertexData()[uvIndex + 1]);
			
			boolean uvInverted = false;
			
			index = 1 * oldQuad.getFormat().getIntegerSize();
			uvIndex = index + oldQuad.getFormat().getUvOffsetById(0) / 4;
			if (tempMinX != Float.intBitsToFloat(oldQuad.getVertexData()[index])) {
				if (tempU != Float.intBitsToFloat(oldQuad.getVertexData()[uvIndex]))
					uvInverted = Axis.X != RotationUtils.getUAxisFromFacing(facing);
				else
					uvInverted = Axis.X != RotationUtils.getVAxisFromFacing(facing);
			} else if (tempMinY != Float.intBitsToFloat(oldQuad.getVertexData()[index + 1])) {
				if (tempU != Float.intBitsToFloat(oldQuad.getVertexData()[uvIndex]))
					uvInverted = Axis.Y != RotationUtils.getUAxisFromFacing(facing);
				else
					uvInverted = Axis.Y != RotationUtils.getVAxisFromFacing(facing);
			} else {
				if (tempU != Float.intBitsToFloat(oldQuad.getVertexData()[uvIndex]))
					uvInverted = Axis.Z != RotationUtils.getUAxisFromFacing(facing);
				else
					uvInverted = Axis.Z != RotationUtils.getVAxisFromFacing(facing);
			}
			
			index = 2 * oldQuad.getFormat().getIntegerSize();
			float tempMaxX = Float.intBitsToFloat(oldQuad.getVertexData()[index]);
			float tempMaxY = Float.intBitsToFloat(oldQuad.getVertexData()[index + 1]);
			float tempMaxZ = Float.intBitsToFloat(oldQuad.getVertexData()[index + 2]);
			
			float minX = Math.min(tempMinX, tempMaxX);
			float minY = Math.min(tempMinY, tempMaxY);
			float minZ = Math.min(tempMinZ, tempMaxZ);
			float maxX = Math.max(tempMinX, tempMaxX);
			float maxY = Math.max(tempMinY, tempMaxY);
			float maxZ = Math.max(tempMinZ, tempMaxZ);
			
			//Check if it is intersecting, otherwise there is no need to render it
			if (!intersectsWithFace(facing, minX, minY, minZ, maxX, maxY, maxZ, offset))
				continue;
			
			float sizeX = maxX - minX;
			float sizeY = maxY - minY;
			float sizeZ = maxZ - minZ;
			
			BakedQuad quad = new CreativeBakedQuad(blockQuads.get(i), this, color, overrideTint && (defaultColor == -1 || blockQuads.get(i).hasTintIndex()) && color != -1, facing);
			
			uvIndex = quad.getFormat().getUvOffsetById(0) / 4;
			float u1 = Float.intBitsToFloat(quad.getVertexData()[uvIndex]);
			float v1 = Float.intBitsToFloat(quad.getVertexData()[uvIndex + 1]);
			uvIndex = 2 * quad.getFormat().getIntegerSize() + quad.getFormat().getUvOffsetById(0) / 4;
			float u2 = Float.intBitsToFloat(quad.getVertexData()[uvIndex]);
			float v2 = Float.intBitsToFloat(quad.getVertexData()[uvIndex + 1]);
			
			float sizeU;
			float sizeV;
			if (uvInverted) {
				sizeU = RotationUtils.getVFromFacing(facing, tempMinX, tempMinY, tempMinZ) < RotationUtils.getVFromFacing(facing, tempMaxX, tempMaxY, tempMaxZ) ? u2 - u1 : u1 - u2;
				sizeV = RotationUtils.getUFromFacing(facing, tempMinX, tempMinY, tempMinZ) < RotationUtils.getUFromFacing(facing, tempMaxX, tempMaxY, tempMaxZ) ? v2 - v1 : v1 - v2;
			} else {
				sizeU = RotationUtils.getUFromFacing(facing, tempMinX, tempMinY, tempMinZ) < RotationUtils.getUFromFacing(facing, tempMaxX, tempMaxY, tempMaxZ) ? u2 - u1 : u1 - u2;
				sizeV = RotationUtils.getVFromFacing(facing, tempMinX, tempMinY, tempMinZ) < RotationUtils.getVFromFacing(facing, tempMaxX, tempMaxY, tempMaxZ) ? v2 - v1 : v1 - v2;
			}
			
			EnumFaceDirection direction = EnumFaceDirection.getFacing(facing);
			
			for (int k = 0; k < 4; k++) {
				VertexInformation vertex = direction.getVertexInformation(k);
				
				index = k * quad.getFormat().getIntegerSize();
				
				float x = facing.getAxis() == Axis.X ? dynamicCube.defaultCube.getVertexInformationPosition(vertex.xIndex) - offset.getX() : MathHelper.clamp(dynamicCube.defaultCube.getVertexInformationPosition(vertex.xIndex) - offset.getX(), minX, maxX);
				float y = facing.getAxis() == Axis.Y ? dynamicCube.defaultCube.getVertexInformationPosition(vertex.yIndex) - offset.getY() : MathHelper.clamp(dynamicCube.defaultCube.getVertexInformationPosition(vertex.yIndex) - offset.getY(), minY, maxY);
				float z = facing.getAxis() == Axis.Z ? dynamicCube.defaultCube.getVertexInformationPosition(vertex.zIndex) - offset.getZ() : MathHelper.clamp(dynamicCube.defaultCube.getVertexInformationPosition(vertex.zIndex) - offset.getZ(), minZ, maxZ);
				
				vec.set(x, y, z);
				
				dynamicCube.sliceVector(facing, vec, offset);
				
				x = vec.x;
				y = vec.y;
				z = vec.z;
				
				float oldX = Float.intBitsToFloat(quad.getVertexData()[index]);
				float oldY = Float.intBitsToFloat(quad.getVertexData()[index + 1]);
				float oldZ = Float.intBitsToFloat(quad.getVertexData()[index + 2]);
				
				quad.getVertexData()[index] = Float.floatToIntBits(x + offset.getX());
				quad.getVertexData()[index + 1] = Float.floatToIntBits(y + offset.getY());
				quad.getVertexData()[index + 2] = Float.floatToIntBits(z + offset.getZ());
				
				if (keepVU)
					continue;
				
				uvIndex = index + quad.getFormat().getUvOffsetById(0) / 4;
				
				float uOffset;
				float vOffset;
				if (uvInverted) {
					uOffset = ((RotationUtils.getVFromFacing(facing, oldX, oldY, oldZ) - RotationUtils.getVFromFacing(facing, x, y, z)) / RotationUtils.getVFromFacing(facing, sizeX, sizeY, sizeZ)) * sizeU;
					vOffset = ((RotationUtils.getUFromFacing(facing, oldX, oldY, oldZ) - RotationUtils.getUFromFacing(facing, x, y, z)) / RotationUtils.getUFromFacing(facing, sizeX, sizeY, sizeZ)) * sizeV;
				} else {
					uOffset = ((RotationUtils.getUFromFacing(facing, oldX, oldY, oldZ) - RotationUtils.getUFromFacing(facing, x, y, z)) / RotationUtils.getUFromFacing(facing, sizeX, sizeY, sizeZ)) * sizeU;
					vOffset = ((RotationUtils.getVFromFacing(facing, oldX, oldY, oldZ) - RotationUtils.getVFromFacing(facing, x, y, z)) / RotationUtils.getVFromFacing(facing, sizeX, sizeY, sizeZ)) * sizeV;
				}
				
				quad.getVertexData()[uvIndex] = Float.floatToRawIntBits(Float.intBitsToFloat(quad.getVertexData()[uvIndex]) - uOffset);
				quad.getVertexData()[uvIndex + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(quad.getVertexData()[uvIndex + 1]) - vOffset);
			}
			quads.add(quad);
		}
		return quads;
	}
}
