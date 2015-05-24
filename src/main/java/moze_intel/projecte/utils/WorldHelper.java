package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityLootBall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

/**
 * Helper class for anything that touches a World.
 * Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class WorldHelper
{
	@SuppressWarnings("unchecked")
	public static final ImmutableList<? extends Class<? extends EntityLiving>> peacefuls = ImmutableList.of(
			EntitySheep.class, EntityPig.class, EntityCow.class,
			EntityMooshroom.class, EntityChicken.class, EntityBat.class,
			EntityVillager.class, EntitySquid.class, EntityOcelot.class,
			EntityWolf.class, EntityHorse.class, EntityRabbit.class
	);
	@SuppressWarnings("unchecked")
	public static final ImmutableList<? extends Class<? extends EntityLiving>> mobs = ImmutableList.of(
			EntityZombie.class, EntitySkeleton.class, EntityCreeper.class,
			EntitySpider.class, EntityEnderman.class, EntitySilverfish.class,
			EntityPigZombie.class, EntityGhast.class, EntityBlaze.class,
			EntitySlime.class, EntityWitch.class, EntityRabbit.class
	);

	public static Set<Class<? extends Entity>> interdictionBlacklist = Sets.newHashSet();

	public static boolean blacklistInterdiction(Class<? extends Entity> clazz)
	{
		if (!interdictionBlacklist.contains(clazz))
		{
			interdictionBlacklist.add(clazz);
			return true;
		}
		return false;
	}

	public static void createLootDrop(List<ItemStack> drops, World world, BlockPos pos)
	{
		createLootDrop(drops, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void createLootDrop(List<ItemStack> drops, World world, double x, double y, double z)
	{
		if (drops.isEmpty())
		{
			return;
		}

		ItemHelper.compactItemList(drops);

		if (ProjectEConfig.useLootBalls)
		{
			world.spawnEntityInWorld(new EntityLootBall(world, drops, x, y, z));
		}
		else
		{
			for (ItemStack drop : drops)
			{
				spawnEntityItem(world, drop, x, y, z);
			}
		}

	}

	public static List<TileEntity> getAdjacentTileEntities(World world, TileEntity tile)
	{
		List<TileEntity> list = Lists.newArrayList();
		for (EnumFacing e : EnumFacing.VALUES)
		{
			TileEntity t = world.getTileEntity(tile.getPos().offset(e));
			if (t != null)
			{
				list.add(t);
			}
		}
		return list;
	}

	public static List<ItemStack> getBlockDrops(World world, EntityPlayer player, IBlockState state, ItemStack stack, BlockPos pos)
	{
		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, stack) > 0 && state.getBlock().canSilkHarvest(world, pos, state, player))
		{
			return Lists.newArrayList(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
		}

		return state.getBlock().getDrops(world, pos, state, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));
	}

	/**
	 * Gets an AABB for AOE digging operations. The offset increases both the breadth and depth of the box.
	 */
	public static AxisAlignedBB getBroadDeepBox(BlockPos pos, EnumFacing direction, int offset)
	{
		switch (direction)
		{
			case EAST: return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ() - offset, pos.getX(), pos.getY() + offset, pos.getZ() + offset);
			case WEST: return new AxisAlignedBB(pos.getX(), pos.getY() - offset, pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
			case UP: return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ() - offset, pos.getX() + offset, pos.getY(), pos.getZ() + offset);
			case DOWN: return new AxisAlignedBB(pos.getX() - offset, pos.getY(), pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
			case SOUTH: return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset, pos.getZ());
			case NORTH: return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ(), pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
			default: return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * Returns in AABB that is always 3x3 orthogonal to the side hit, but varies in depth in the direction of the side hit
	 */
	public static AxisAlignedBB getDeepBox(BlockPos pos, EnumFacing direction, int depth)
	{
		switch (direction)
		{
			case EAST: return new AxisAlignedBB(pos.getX() - depth, pos.getY() - 1, pos.getZ() - 1, pos.getX(), pos.getY() + 1, pos.getZ() + 1);
			case WEST: return new AxisAlignedBB(pos.getX(), pos.getY() - 1, pos.getZ() - 1, pos.getX() + depth, pos.getY() + 1, pos.getZ() + 1);
			case UP: return new AxisAlignedBB(pos.getX() - 1, pos.getY() - depth, pos.getZ() - 1, pos.getX() + 1, pos.getY(), pos.getZ() + 1);
			case DOWN: return new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + depth, pos.getZ() + 1);
			case SOUTH: return new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - depth, pos.getX() + 1, pos.getY() + 1, pos.getZ());
			case NORTH: return new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + depth);
			default: return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * Gets an AABB for AOE digging operations. The charge increases only the breadth of the box.
	 * Y level remains constant. As such, a direction hit is unneeded.
	 */
	public static AxisAlignedBB getFlatYBox(BlockPos pos, int offset)
	{
		return new AxisAlignedBB(pos.getX() - offset, pos.getY(), pos.getZ() - offset, pos.getX() + offset, pos.getY(), pos.getZ() + offset);
	}

	public static Entity getNewEntityInstance(Class<? extends Entity> c, World world)
	{
		try
		{
			Constructor<? extends Entity> constr = c.getConstructor(World.class);
			Entity ent = constr.newInstance(world);

			if (ent instanceof EntitySkeleton)
			{
				if (world.rand.nextInt(2) == 0)
				{
					((EntitySkeleton) ent).setSkeletonType(1);
					ent.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
				}
				else
				{
					ent.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
				}
			}
			else if (ent instanceof EntityPigZombie)
			{
				ent.setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
			}
			else if (ent instanceof EntitySheep)
			{
				((EntitySheep) ent).setFleeceColor(EnumDyeColor.values()[MathUtils.randomIntInRange(0, 15)]);
			}
			else if (ent instanceof EntityVillager)
			{
				VillagerRegistry.setRandomProfession(((EntityVillager) ent), world.rand);
			}
			else if (ent instanceof EntityRabbit)
			{
				((EntityRabbit) ent).setRabbitType(world.rand.nextInt(6));
			}
			else if (ent instanceof EntityHorse)
			{
				((EntityHorse) ent).setHorseType(MathUtils.randomIntInRange(0, 2));
				if (((EntityHorse) ent).getHorseType() == 0)
				{
					((EntityHorse) ent).setHorseVariant(MathUtils.randomIntInRange(0, 6));
				}
			}

			return ent;
		}
		catch (Exception e)
		{
			PELogger.logFatal("Could not create new entity instance for: "+c.getCanonicalName());
			e.printStackTrace();
		}

		return null;
	}

	public static Entity getRandomEntity(World world, Entity toRandomize)
	{
		Class entClass = toRandomize.getClass();

		if (peacefuls.contains(entClass))
		{
			return getNewEntityInstance(CollectionHelper.getRandomListEntry(peacefuls, entClass), world);
		}
		else if (mobs.contains(entClass))
		{
			Entity ent = getNewEntityInstance(CollectionHelper.getRandomListEntry(mobs, entClass), world);
			if (ent instanceof EntityRabbit)
			{
				((EntityRabbit) ent).setRabbitType(99);
			}
			return ent;
		}
		else if (world.rand.nextInt(2) == 0)
		{
			return getNewEntityInstance(EntitySlime.class, world);
		}
		else
		{
			return getNewEntityInstance(EntitySheep.class, world);
		}
	}

	public static List<TileEntity> getTileEntitiesWithinAABB(World world, AxisAlignedBB bBox)
	{
		List<TileEntity> list = Lists.newArrayList();

		for (int i = (int) bBox.minX; i <= bBox.maxX; i++)
			for (int j = (int) bBox.minY; j <= bBox.maxY; j++)
				for (int k = (int) bBox.minZ; k <= bBox.maxZ; k++)
				{
					TileEntity tile = world.getTileEntity(new BlockPos(i, j, k));
					if (tile != null)
					{
						list.add(tile);
					}
				}

		return list;
	}

	/**
	 * Gravitates an entity, vanilla xp orb style, towards a position
	 * Code adapted from EntityXPOrb and OpenBlocks Vacuum Hopper, mostly the former
	 */
	public static void gravitateEntityTowards(Entity ent, double x, double y, double z)
	{
		double dX = x - ent.posX;
		double dY = y - ent.posY;
		double dZ = z - ent.posZ;
		double dist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

		double vel = 1.0 - dist / 15.0;
		if (vel > 0.0D)
		{
			vel *= vel;
			ent.motionX += dX / dist * vel * 0.05;
			ent.motionY += dY / dist * vel * 0.1;
			ent.motionZ += dZ / dist * vel * 0.05;
			ent.moveEntity(ent.motionX, ent.motionY, ent.motionZ);
		}
	}

	/**
	 * Recursively mines out a vein of the given Block, starting from the provided coordinates
	 */
	public static void harvestVein(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState target, List<ItemStack> currentDrops, int numMined)
	{
		if (numMined >= Constants.MAX_VEIN_SIZE)
		{
			return;
		}

		AxisAlignedBB b = new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

		for (int x = (int) b.minX; x <= b.maxX; x++)
			for (int y = (int) b.minY; y <= b.maxY; y++)
				for (int z = (int) b.minZ; z <= b.maxZ; z++)
				{
					BlockPos currentPos = new BlockPos(x, y, z);
					IBlockState currentState = world.getBlockState(currentPos);

					if (currentState == target)
					{
						currentDrops.addAll(getBlockDrops(world, player, currentState, stack, pos));
						world.setBlockToAir(pos);
						numMined++;
						harvestVein(world, player, stack, currentPos, target, currentDrops, numMined);
					}
				}
	}

	public static boolean isArrowInGround(EntityArrow arrow)
	{
		return ReflectionHelper.getArrowInGround(arrow);
	}

	/**
	 * Repels projectiles and mobs in the given AABB away from a given point
	 * If isSWRG is true, then the blacklist is not checked.
	 */
	public static void repelEntitiesInAABBFromPoint(World world, AxisAlignedBB effectBounds, double x, double y, double z, boolean isSWRG)
	{
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, effectBounds);

		for (Entity ent : list)
		{
			if (isSWRG || !interdictionBlacklist.contains(ent.getClass())) {
				// SWRG repels all, only the torch respects blacklist
				if ((ent instanceof EntityLiving) || (ent instanceof IProjectile))
				{
					if (ProjectEConfig.interdictionMode && !(ent instanceof IMob || ent instanceof IProjectile))
					{
						continue;
					}
					else
					{
						if (ent instanceof EntityArrow && ((EntityArrow) ent).onGround)
						{
							continue;
						}
						Vec3 p = new Vec3(x, y, z);
						Vec3 t = new Vec3(ent.posX, ent.posY, ent.posZ);
						double distance = p.distanceTo(t) + 0.1D;

						Vec3 r = new Vec3(t.xCoord - p.xCoord, t.yCoord - p.yCoord, t.zCoord - p.zCoord);

						ent.motionX += r.xCoord / 1.5D / distance;
						ent.motionY += r.yCoord / 1.5D / distance;
						ent.motionZ += r.zCoord / 1.5D / distance;
					}
				}
			}
		}
	}

	public static void spawnEntityItem(World world, ItemStack stack, BlockPos pos)
	{
		spawnEntityItem(world, stack, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void spawnEntityItem(World world, ItemStack stack, double x, double y, double z)
	{
		float f = world.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
		EntityItem entityitem;

		for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(entityitem))
		{
			int j1 = world.rand.nextInt(21) + 10;

			if (j1 > stack.stackSize)
				j1 = stack.stackSize;

			stack.stackSize -= j1;
			entityitem = new EntityItem(world, (double)((float) x + f), (double)((float) y + f1), (double)((float) z + f2), new ItemStack(stack.getItem(), j1, stack.getItemDamage()));
			float f3 = 0.05F;
			entityitem.motionX = (double)((float) world.rand.nextGaussian() * f3);
			entityitem.motionY = (double)((float) world.rand.nextGaussian() * f3 + 0.2F);
			entityitem.motionZ = (double)((float) world.rand.nextGaussian() * f3);

			if (stack.hasTagCompound())
			{
				entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
			}
		}

	}
}
