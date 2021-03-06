package cloud.lemonslice.afterthedrizzle.common.block;

import cloud.lemonslice.afterthedrizzle.common.capability.CapabilityWorldWeather;
import cloud.lemonslice.afterthedrizzle.common.config.ServerConfig;
import cloud.lemonslice.afterthedrizzle.common.environment.weather.DailyWeatherData;
import cloud.lemonslice.afterthedrizzle.common.environment.weather.WeatherType;
import cloud.lemonslice.silveroak.common.item.SilveroakItemsRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;

import java.util.List;

public class InstrumentShelterBlock extends NormalBlock
{
    private static final BooleanProperty THERMOMETER = BooleanProperty.create("thermometer");
    private static final BooleanProperty RAIN_GAUGE = BooleanProperty.create("rain_gauge");
    private static final BooleanProperty HYGROMETER = BooleanProperty.create("hygrometer");

    public InstrumentShelterBlock()
    {
        super("instrument_shelter", Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(0.6F).notSolid());
        this.setDefaultState(this.stateContainer.getBaseState().with(THERMOMETER, false).with(RAIN_GAUGE, false).with(HYGROMETER, false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
    {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (player.getHeldItem(handIn).getItem() == SilveroakItemsRegistry.THERMOMETER && !state.get(THERMOMETER))
        {
            player.getHeldItem(handIn).shrink(1);
            worldIn.setBlockState(pos, state.with(THERMOMETER, true));
            return ActionResultType.SUCCESS;
        }
//        else if (player.getHeldItem(handIn).getItem() == SilveroakItemsRegistry.RAIN_GAUGE && !state.get(RAIN_GAUGE))
//        {
//            player.getHeldItem(handIn).shrink(1);
//            worldIn.setBlockState(pos, state.with(RAIN_GAUGE, true));
//            return ActionResultType.SUCCESS;
//        }
        else if (player.getHeldItem(handIn).getItem() == SilveroakItemsRegistry.HYGROMETER && !state.get(HYGROMETER))
        {
            player.getHeldItem(handIn).shrink(1);
            worldIn.setBlockState(pos, state.with(HYGROMETER, true));
            return ActionResultType.SUCCESS;
        }
        else if (!worldIn.isRemote)
        {
            if (!ServerConfig.Weather.enable.get())
            {
                player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.disable"));
                return ActionResultType.SUCCESS;
            }
            boolean forecast = state.get(THERMOMETER) && state.get(HYGROMETER);
            boolean rain = state.get(RAIN_GAUGE);
            return worldIn.getCapability(CapabilityWorldWeather.WORLD_WEATHER).map(data ->
            {
                if (forecast)
                {
                    player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.start"));
                    List<WeatherType> today = data.getCurrentDay().getWeatherList();
                    TranslationTextComponent current = DailyWeatherData.getMainWeather(today, Math.toIntExact(worldIn.getDayTime() % 24000) / 1000).getTranslation();
                    player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.current", current));

                    TranslationTextComponent morning = DailyWeatherData.getMainWeather(today, 0).getTranslation();
                    TranslationTextComponent afternoon = DailyWeatherData.getMainWeather(today, 6).getTranslation();
                    TranslationTextComponent night = DailyWeatherData.getMainWeather(today, 12).getTranslation();
                    player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.today", morning, afternoon, night));

                    List<WeatherType> tomorrow = data.getWeatherData(1).getWeatherList();
                    morning = DailyWeatherData.getMainWeather(tomorrow, 0).getTranslation();
                    afternoon = DailyWeatherData.getMainWeather(tomorrow, 6).getTranslation();
                    night = DailyWeatherData.getMainWeather(tomorrow, 12).getTranslation();
                    player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.tomorrow", morning, afternoon, night));
                    player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.end"));
                }
                if (rain && data.getCurrentWeather().isRainy())
                {
                    int index = Math.toIntExact(worldIn.getDayTime() + 1000 % 24000) / 1000;

                }
                if (!forecast && !rain)
                {
                    player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.not_enough"));
                }
                return ActionResultType.SUCCESS;
            }).orElseGet(() ->
            {
                player.sendMessage(new TranslationTextComponent("info.afterthedrizzle.environment.weather.forecast.disable"));
                return ActionResultType.SUCCESS;
            });
        }
        else return ActionResultType.SUCCESS;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(THERMOMETER, RAIN_GAUGE, HYGROMETER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        List<ItemStack> list = super.getDrops(state, builder);
        if (state.get(THERMOMETER))
        {
            list.add(new ItemStack(SilveroakItemsRegistry.THERMOMETER));
        }
        if (state.get(RAIN_GAUGE))
        {
            list.add(new ItemStack(SilveroakItemsRegistry.RAIN_GAUGE));
        }
        if (state.get(HYGROMETER))
        {
            list.add(new ItemStack(SilveroakItemsRegistry.HYGROMETER));
        }
        return list;
    }
}
