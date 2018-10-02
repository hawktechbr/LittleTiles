package com.creativemd.littletiles.common.tiles.preview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry.LittleStructurePreviewHandler;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.combine.AdvancedCombiner;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

public class LittlePreviews implements Iterable<LittleTilePreview> {
	
	protected List<LittleTilePreview> previews;
	public LittleGridContext context;
	
	public LittlePreviews(LittleGridContext context) {
		this.context = context;
		this.previews = new ArrayList<>();
	}
	
	protected LittlePreviews(LittlePreviews previews) {
		this.previews = new ArrayList<>(previews.previews);
		this.context = previews.context;
	}
	
	public boolean isAbsolute() {
		return false;
	}
	
	public BlockPos getBlockPos() {
		return null;
	}
	
	public boolean hasStructure() {
		return false;
	}
	
	public LittleStructure getStructure() {
		return null;
	}
	
	public NBTTagCompound getStructureData() {
		return null;
	}
	
	public LittleStructurePreviewHandler getStructureHandler() {
		return null;
	}
	
	public LittleGridContext getMinContext() {
		if (hasStructure()) {
			LittleGridContext context = LittleGridContext.max(this.context, getStructureHandler().getMinContext(this));
			if (hasChildren())
				for (LittlePreviews child : getChildren())
					context = LittleGridContext.max(context, child.getMinContext());
			return context;
		}
		return context;
	}
	
	public boolean hasChildren() {
		return false;
	}
	
	public List<? extends LittlePreviews> getChildren() {
		return null;
	}
	
	public void addChild(LittlePreviews child) {
		
	}
	
	public void getPlacePreviews(List<PlacePreviewTile> placePreviews, LittleTileBox overallBox, boolean fixed, LittleTileVec offset) {
		for (LittleTilePreview preview : this) {
			placePreviews.add(preview.getPlaceableTile(overallBox, fixed, offset, this));
		}
		
		if (hasStructure()) {
			for (PlacePreviewTile placePreviewTile : getStructureHandler().getSpecialTiles(context, this)) {
				if (!fixed)
					placePreviewTile.box.addOffset(offset);
				placePreviews.add(placePreviewTile);
			}
		}
		
		if (hasChildren()) {
			for (LittlePreviews child : getChildren()) {
				child.getPlacePreviews(placePreviews, overallBox, fixed, offset);
			}
		}
	}
	
	public void convertTo(LittleGridContext to) {
		for (LittleTilePreview preview : previews) {
			preview.convertTo(this.context, to);
		}
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				child.convertTo(to);
		this.context = to;
	}
	
	protected int getSmallestContext() {
		int size = LittleGridContext.minSize;
		for (LittleTilePreview preview : previews)
			size = Math.max(size, preview.getSmallestContext(context));
		return size;
	}
	
	public void convertToSmallest() {
		int size = getSmallestContext();
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				size = Math.max(child.getSmallestContext(), size);
			
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public LittlePreviews copy() {
		LittlePreviews previews = new LittlePreviews(context);
		previews.previews.addAll(this.previews);
		return previews;
	}
	
	protected LittleTilePreview getPreview(LittleTile tile) {
		LittleTilePreview preview = tile.getPreviewTile();
		LittleGridContext context = tile.getContext();
		if (this.context != context) {
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		return preview;
	}
	
	public LittleTilePreview addPreview(BlockPos pos, LittleTilePreview preview, LittleGridContext context) {
		if (this.context != context) {
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		previews.add(preview);
		return preview;
	}
	
	public LittleTilePreview addTile(LittleTile tile) {
		LittleTilePreview preview = getPreview(tile);
		previews.add(preview);
		return preview;
		
	}
	
	public LittleTilePreview addTile(LittleTile tile, LittleTileVec offset) {
		LittleTilePreview preview = getPreview(tile);
		preview.box.addOffset(offset);
		return addPreview(null, tile.getPreviewTile(), tile.getContext());
	}
	
	public void addTiles(List<LittleTile> tiles) {
		if (tiles.isEmpty())
			return;
		
		for (LittleTile tile : tiles) {
			addTile(tile);
		}
	}
	
	@Override
	public Iterator<LittleTilePreview> iterator() {
		return previews.iterator();
	}
	
	public static LittlePreviews getPreview(ItemStack stack, boolean allowLowResolution) {
		if (!stack.hasTagCompound())
			return new LittlePreviews(LittleGridContext.get());
		
		LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
		if (stack.getTagCompound().getTag("tiles") instanceof NBTTagInt) {
			LittlePreviews previews = new LittlePreviews(context);
			int tiles = stack.getTagCompound().getInteger("tiles");
			for (int i = 0; i < tiles; i++) {
				NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
				LittleTilePreview preview = LittleTilePreview.loadPreviewFromNBT(nbt);
				if (preview != null)
					previews.previews.add(preview);
			}
			
			if (stack.getTagCompound().hasKey("structure"))
				return new LittlePreviewsStructure(stack.getTagCompound().getCompoundTag("structure"), previews);
			return previews;
		} else {
			if (allowLowResolution && stack.getTagCompound().hasKey("pos")) {
				LittlePreviews previews = new LittlePreviews(context);
				NBTTagCompound tileData = new NBTTagCompound();
				LittleTile tile = new LittleTileBlock(LittleTiles.coloredBlock);
				tile.saveTileExtra(tileData);
				tileData.setString("tID", tile.getID());
				
				NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
				for (int i = 0; i < list.tagCount(); i++) {
					int[] array = list.getIntArrayAt(i);
					previews.previews.add(new LittleTilePreview(new LittleTileBox(array[0] * context.size, array[1] * context.size, array[2] * context.size, array[0] * context.size + context.maxPos, array[1] * context.size + context.maxPos, array[02] * context.size + context.maxPos), tileData));
				}
				return previews;
			}
			LittlePreviews previews = stack.getTagCompound().hasKey("structure") ? new LittlePreviewsStructure(stack.getTagCompound().getCompoundTag("structure"), context) : new LittlePreviews(context);
			previews = LittleNBTCompressionTools.readPreviews(previews, stack.getTagCompound().getTagList("tiles", 10));
			
			if (previews.hasStructure() && stack.getTagCompound().hasKey("children")) {
				NBTTagList list = stack.getTagCompound().getTagList("children", 10);
				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound child = list.getCompoundTagAt(i);
					previews.addChild(LittleNBTCompressionTools.readPreviews(stack.getTagCompound().hasKey("structure") ? new LittlePreviewsStructure(stack.getTagCompound().getCompoundTag("structure"), context) : new LittlePreviews(context), stack.getTagCompound().getTagList("tiles", 10)));
				}
			}
			
			return previews;
		}
	}
	
	public LittleTilePreview get(int index) {
		return previews.get(index);
	}
	
	protected Iterator<LittleTilePreview> allPreviewsIterator() {
		if (hasChildren())
			return new Iterator<LittleTilePreview>() {
				
				public int i = -1;
				public Iterator<LittleTilePreview> subIterator = previews.iterator();
				public List<? extends LittlePreviews> children = getChildren();
				
				@Override
				public boolean hasNext() {
					if (subIterator.hasNext())
						return true;
					
					i++;
					
					while (i < previews.size() && !subIterator.hasNext()) {
						subIterator = children.get(i).allPreviewsIterator();
						i++;
					}
					
					return subIterator.hasNext();
				}
				
				@Override
				public LittleTilePreview next() {
					return subIterator.next();
				}
				
				@Override
				public void remove() {
					subIterator.remove();
				}
			};
		return previews.iterator();
	}
	
	public Iterable<LittleTilePreview> allPreviews() {
		return new Iterable<LittleTilePreview>() {
			
			@Override
			public Iterator<LittleTilePreview> iterator() {
				return allPreviewsIterator();
			}
		};
	}
	
	public int size() {
		return previews.size();
	}
	
	public int totalSize() {
		if (!hasChildren())
			return size();
		int size = size();
		for (LittlePreviews child : getChildren()) {
			size += child.totalSize();
		}
		return size;
	}
	
	public void ensureContext(LittleGridContext context) {
		if (this.context.size < context.size)
			convertTo(context);
	}
	
	public boolean isEmpty() {
		return previews.isEmpty();
	}
	
	public void addWithoutCheckingPreview(LittleTilePreview preview) {
		previews.add(preview);
	}
	
	public LittleVolumes getVolumes() {
		LittleVolumes volumes = new LittleVolumes(context);
		volumes.addPreviews(this);
		return volumes;
	}
	
	public boolean isVolumeEqual(LittlePreviews previews) {
		return getVolumes().equals(previews.getVolumes());
	}
	
	public void combinePreviewBlocks() {
		HashMapList<BlockPos, LittleTilePreview> chunked = new HashMapList<>();
		for (int i = 0; i < previews.size(); i++) {
			chunked.add(previews.get(i).box.getMinVec().getBlockPos(context), previews.get(i));
		}
		previews.clear();
		AdvancedCombiner<LittleTilePreview> combiner = new AdvancedCombiner(new ArrayList<>());
		for (Iterator<ArrayList<LittleTilePreview>> iterator = chunked.values().iterator(); iterator.hasNext();) {
			ArrayList<LittleTilePreview> list = iterator.next();
			combiner.setCombinables(list);
			combiner.combine();
			previews.addAll(list);
		}
	}
	
	private void advancedScale(int from, int to) {
		for (LittleTilePreview preview : previews) {
			preview.convertTo(from, to);
		}
	}
	
	public static void advancedScale(LittlePreviews previews, int from, int to) {
		previews.advancedScale(from, to);
	}
}
