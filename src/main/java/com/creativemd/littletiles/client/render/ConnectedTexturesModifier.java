package com.creativemd.littletiles.client.render;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ConnectedTexturesModifier {
	
	private static Class connectedProperties;
	private static Method match;
	private static Method matchMeta;
	
	static {
		try {
			connectedProperties = Class.forName("net.optifine.ConnectedProperties");
			match = ReflectionHelper.findMethod(connectedProperties, "matchesBlockId", "matchesBlockId", int.class);
			matchMeta = ReflectionHelper.findMethod(connectedProperties, "matchesBlock", "matchesBlock", int.class, int.class);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean matches(Object properties, IBlockAccess world, BlockPos pos) {
		try {
			TileEntityLittleTiles te = BlockTile.loadTe(world, pos);
			if (te != null) {
				for (LittleTile tile : te.getTiles()) {
					if (tile instanceof LittleTileBlock && (Boolean) match.invoke(properties, Block.getStateId(((LittleTileBlock) tile).getBlockState())))
						return true;
				}
				return false;
			}
			return (boolean) match.invoke(properties, Block.getStateId(world.getBlockState(pos)));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean matches(Object properties, IBlockAccess world, BlockPos pos, int metadata) {
		try {
			TileEntityLittleTiles te = BlockTile.loadTe(world, pos);
			if (te != null) {
				for (LittleTile tile : te.getTiles()) {
					if (tile instanceof LittleTileBlock && (Boolean) matchMeta.invoke(properties, Block.getStateId(((LittleTileBlock) tile).getBlockState()), metadata))
						return true;
				}
				return false;
			}
			return (boolean) matchMeta.invoke(properties, Block.getStateId(world.getBlockState(pos)), metadata);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isNeighbour(IBlockAccess world, IBlockState state, BlockPos pos) {
		TileEntityLittleTiles te = BlockTile.loadTe(world, pos);
		if (te != null) {
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);
			for (LittleTile tile : te.getTiles()) {
				if (tile instanceof LittleTileBlock && ((LittleTileBlock) tile).getBlock() == block && ((LittleTileBlock) tile).getMeta() == meta)
					return true;
			}
		}
		return false;
	}
	
	public static boolean isFullCube(IBlockState state) {
		return state.getBlock() instanceof BlockTile;
	}
}
