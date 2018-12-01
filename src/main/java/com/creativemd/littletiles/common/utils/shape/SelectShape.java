package com.creativemd.littletiles.common.utils.shape;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.BlockTile.TEResult;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SelectShape {
	
	public static LinkedHashMap<String, SelectShape> shapes = new LinkedHashMap<>();
	
	public static SelectShape tileShape = new SelectShape("tile") {
		
		@Override
		public boolean shouldBeRegistered() {
			return false;
		}
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
			
		}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return true;
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return getBoxes(player, nbt, result, context);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			return new ArrayList<>();
		}
		
		@Override
		public LittleBoxes getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			TEResult te = BlockTile.loadTeAndTile(player.world, result.getBlockPos(), player);
			
			LittleBoxes boxes;
			if (te.isComplete()) {
				boxes = new LittleBoxes(te.te.getPos(), te.te.getContext());
				boxes.add(te.tile.box.copy());
			} else
				boxes = new LittleBoxes(result.getBlockPos(), context);
			
			return boxes;
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
			
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			
		}
	};
	
	public static BasicSelectShape CUBE = new BasicSelectShape("cube") {
		
		@Override
		public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side, LittleGridContext context) {
			LittleTileVec offset = new LittleTileVec(side);
			offset.scale((int) (thickness - 1) / 2);
			vec.sub(offset);
			if ((thickness & 1) == 0 && side.getAxisDirection() == AxisDirection.NEGATIVE)
				vec.sub(side);
			
			LittleTileBox box = new LittleTileBox(vec, new LittleTileSize(thickness, thickness, thickness));
			// box.makeItFitInsideBlock();
			return box;
		}
		
	};
	
	public static BasicSelectShape BAR = new BasicSelectShape("bar") {
		
		@Override
		public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side, LittleGridContext context) {
			LittleTileBox box = CUBE.getBox(vec, thickness, side, context);
			
			switch (side.getAxis()) {
			case X:
				box.minX = 0;
				box.maxX = context.size;
				break;
			case Y:
				box.minY = 0;
				box.maxY = context.size;
				break;
			case Z:
				box.minZ = 0;
				box.maxZ = context.size;
				break;
			}
			
			return box;
		}
		
	};
	
	public static BasicSelectShape PLANE = new BasicSelectShape("plane") {
		
		@Override
		public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side, LittleGridContext context) {
			LittleTileBox box = CUBE.getBox(vec, thickness, side, context);
			
			switch (side.getAxis()) {
			case X:
				box.minY = 0;
				box.maxY = context.size;
				box.minZ = 0;
				box.maxZ = context.size;
				break;
			case Y:
				box.minX = 0;
				box.maxX = context.size;
				box.minZ = 0;
				box.maxZ = context.size;
				break;
			case Z:
				box.minX = 0;
				box.maxX = context.size;
				box.minY = 0;
				box.maxY = context.size;
				break;
			}
			
			return box;
		}
		
	};
	
	public static SelectShape defaultShape = CUBE;
	
	public static SelectShape getShape(String name) {
		SelectShape shape = SelectShape.shapes.get(name);
		return shape == null ? SelectShape.defaultShape : shape;
	}
	
	public final String key;
	
	public SelectShape(String name) {
		if (shouldBeRegistered())
			shapes.put(name, this);
		this.key = name;
	}
	
	public boolean shouldBeRegistered() {
		return true;
	}
	
	public abstract LittleBoxes getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	/** @return if the shape has been selected (information will then be send to the
	 *         server) */
	public abstract boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	/** @return if the shape has been selected (information will then be send to the
	 *         server) */
	public abstract boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	public abstract void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context);
	
	public abstract LittleBoxes getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	public abstract void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context);
	
	@SideOnly(Side.CLIENT)
	public abstract List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context);
	
	@SideOnly(Side.CLIENT)
	public abstract void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context);
	
	public void rotate(Rotation rotation, NBTTagCompound nbt) {
		
	}
	
	public void flip(Axis axis, NBTTagCompound nbt) {
		
	}
	
	public abstract static class BasicSelectShape extends SelectShape {
		
		public BasicSelectShape(String name) {
			super(name);
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
		}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return true;
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			LittleBoxes boxes = new LittleBoxes(result.getBlockPos(), context);
			LittleTilePos vec = new LittleTilePos(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(RotationUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.contextVec.vec.sub(result.sideHit);
			boxes.add(getBox(vec.getRelative(new LittleTilePos(result.getBlockPos(), context)).getVec(context), Math.max(1, nbt.getInteger("thick")), result.sideHit, context));
			return boxes;
		}
		
		@Override
		public LittleBoxes getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			LittleBoxes boxes = new LittleBoxes(result.getBlockPos(), context);
			LittleTilePos vec = new LittleTilePos(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(RotationUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.contextVec.vec.sub(result.sideHit);
			boxes.add(getBox(vec.getRelative(new LittleTilePos(result.getBlockPos(), context)).getVec(context), Math.max(1, nbt.getInteger("thick")), result.sideHit, context));
			return boxes;
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			list.add("thickness: " + Math.max(1, nbt.getInteger("thick")));
		}
		
		public abstract LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side, LittleGridContext context);
		
		@Override
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			List<GuiControl> controls = new ArrayList<>();
			controls.add(new GuiLabel("Size:", 0, 6));
			controls.add(new GuiSteppedSlider("thickness", 35, 5, 68, 10, Math.max(1, nbt.getInteger("thick")), 1, context.size));
			return controls;
		}
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
			GuiSteppedSlider thickness = (GuiSteppedSlider) gui.get("thickness");
			nbt.setInteger("thick", (int) thickness.value);
		}
		
	}
	
	public static class DragSelectShape extends SelectShape {
		
		private final DragShape shape;
		
		public DragSelectShape(DragShape shape) {
			super("drag" + shape.key);
			this.shape = shape;
		}
		
		public LittleTilePos first;
		
		public LittleBoxes getBoxes(EntityPlayer player, NBTTagCompound nbt, LittleTilePos min, LittleTilePos max, boolean preview, LittleGridContext context) {
			min.ensureBothAreEqual(max);
			LittleTilePos offset = new LittleTilePos(min.pos, min.getContext());
			LittleTileBox box = new LittleTileBox(new LittleTileBox(min.getRelative(offset).getVec(context)), new LittleTileBox(max.getRelative(offset).getVec(context)));
			return shape.getBoxes(new LittleBoxes(offset.pos, context), box.getMinVec(), box.getMaxVec(), player, nbt, preview, min, max);
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			LittleTilePos vec = new LittleTilePos(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(RotationUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.contextVec.vec.sub(result.sideHit);
			if (first == null) {
				LittleBoxes boxes = new LittleBoxes(result.getBlockPos(), context);
				boxes.add(new LittleTileBox(vec.getRelative(new LittleTilePos(result.getBlockPos(), context)).getVec(context)));
				return boxes;
			}
			return getBoxes(player, nbt, first, vec, true, context);
		}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			if (first != null)
				return true;
			first = new LittleTilePos(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(RotationUtils.get(result.sideHit.getAxis(), result.hitVec)))
				first.contextVec.vec.sub(result.sideHit);
			return false;
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
			first = null;
		}
		
		@Override
		public LittleBoxes getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			LittleTilePos vec = new LittleTilePos(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(RotationUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.contextVec.vec.sub(result.sideHit);
			LittleBoxes boxes = getBoxes(player, nbt, first, vec, false, context);
			first = null;
			return boxes;
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			shape.addExtraInformation(nbt, list);
		}
		
		@Override
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			return shape.getCustomSettings(nbt, context);
		}
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
			shape.saveCustomSettings(gui, nbt, context);
		}
		
		@Override
		public void flip(Axis axis, NBTTagCompound nbt) {
			shape.flip(nbt, axis);
		}
		
		@Override
		public void rotate(Rotation rotation, NBTTagCompound nbt) {
			shape.rotate(nbt, rotation);
		}
		
	}
	
}
