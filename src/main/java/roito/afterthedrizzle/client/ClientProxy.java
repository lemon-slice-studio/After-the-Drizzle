package roito.afterthedrizzle.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import roito.afterthedrizzle.client.color.season.BiomeColorsHandler;
import roito.afterthedrizzle.client.render.SeatEntityRenderer;
import roito.afterthedrizzle.common.CommonProxy;
import roito.afterthedrizzle.common.block.BlocksRegistry;
import roito.afterthedrizzle.common.entity.EntityTypesRegistry;

public class ClientProxy extends CommonProxy
{
    public static final ResourceLocation LIGHT_RAIN_TEXTURES = new ResourceLocation("afterthedrizzle:textures/environment/rain_light.png");
    public static final ResourceLocation NORMAL_RAIN_TEXTURES = new ResourceLocation("afterthedrizzle:textures/environment/rain_normal.png");
    public static final ResourceLocation HEAVY_RAIN_TEXTURES = new ResourceLocation("afterthedrizzle:textures/environment/rain_heavy.png");
    public static final ResourceLocation LIGHT_SNOW_TEXTURES = new ResourceLocation("afterthedrizzle:textures/environment/snow_light.png");
    public static final ResourceLocation NORMAL_SNOW_TEXTURES = new ResourceLocation("afterthedrizzle:textures/environment/snow_normal.png");
    public static final ResourceLocation HEAVY_SNOW_TEXTURES = new ResourceLocation("afterthedrizzle:textures/environment/snow_heavy.png");

    @Override
    public World getClientWorld()
    {
        return Minecraft.getInstance().world;
    }

    @Override
    public PlayerEntity getClientPlayer()
    {
        return Minecraft.getInstance().player;
    }

    public static void initBiomeColors()
    {
        BiomeColors.GRASS_COLOR = BiomeColorsHandler.GRASS_COLOR;
        BiomeColors.FOLIAGE_COLOR = BiomeColorsHandler.FOLIAGE_COLOR;
    }

    public static void registerEntityRenderer()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesRegistry.SEAT_ENTITY, SeatEntityRenderer::new);
    }

    public static void registerRenderType()
    {
        registerCutoutType(BlocksRegistry.CHRYSANTHEMUM);
        registerCutoutType(BlocksRegistry.ZINNIA);
        registerCutoutType(BlocksRegistry.HYACINTH);
        registerCutoutType(BlocksRegistry.TEA_PLANT);
        registerCutoutType(BlocksRegistry.RICE_SEEDLING);
        registerCutoutType(BlocksRegistry.WILD_TEA_PLANT);
        registerCutoutType(BlocksRegistry.GRASS_BLOCK_WITH_HOLE);
        registerCutoutType(BlocksRegistry.BAMBOO_GLASS_DOOR);
        registerCutoutType(BlocksRegistry.DRINK_MAKER);
        registerCutoutType(BlocksRegistry.RICE_PLANT);
        registerCutoutType(BlocksRegistry.OAK_TRELLIS);
        registerCutoutType(BlocksRegistry.BIRCH_TRELLIS);
        registerCutoutType(BlocksRegistry.DARK_OAK_TRELLIS);
        registerCutoutType(BlocksRegistry.ACACIA_TRELLIS);
        registerCutoutType(BlocksRegistry.SPRUCE_TRELLIS);
        registerCutoutType(BlocksRegistry.JUNGLE_TRELLIS);
        registerCutoutType(BlocksRegistry.GRAPE);
    }

    private static void registerCutoutType(Block block)
    {
        RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
    }
}
