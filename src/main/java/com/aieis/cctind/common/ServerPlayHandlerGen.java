package com.aieis.cctind.common;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.tac.guns.Config;
import com.tac.guns.GunMod;
import com.tac.guns.client.handler.MovementAdaptationsHandler;
import com.tac.guns.client.screen.UpgradeBenchScreen;
import com.tac.guns.common.*;
import com.tac.guns.common.container.UpgradeBenchContainer;
import com.tac.guns.entity.ProjectileEntity;
import com.tac.guns.event.GunFireEvent;
import com.tac.guns.init.ModBlocks;
import com.tac.guns.init.ModEnchantments;
import com.tac.guns.init.ModItems;
import com.tac.guns.interfaces.IProjectileFactory;
import com.tac.guns.item.GunItem;
import com.tac.guns.item.TransitionalTypes.TimelessGunItem;
import com.tac.guns.item.TransitionalTypes.wearables.ArmorRigItem;
import com.tac.guns.item.attachment.IAttachment.Type;
import com.tac.guns.network.PacketHandler;
import com.tac.guns.network.message.*;
import com.tac.guns.tileentity.FlashLightSource;
import com.tac.guns.tileentity.UpgradeBenchTileEntity;
import com.tac.guns.util.GunEnchantmentHelper;
import com.tac.guns.util.GunModifierHelper;
import com.tac.guns.util.WearableHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class ServerPlayHandlerGen {
    public static final Logger LOGGER = LogManager.getLogger("tac");
    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = (entity) -> {
        return entity.getSoundSource() == SoundCategory.HOSTILE && !((List)Config.COMMON.aggroMobs.exemptEntities.get()).contains(entity.getType().getRegistryName().toString());
    };
    private static final UUID speedUptId = UUID.fromString("923e4567-e89b-42d3-a456-556642440000");
    public ServerPlayHandlerGen() {
    }

    public static void handleShoot(PlayerEntity player, float yRot, float xRot, float randP, float randY) {
        if (!player.isSpectator()) {
            World world = player.level;
            ItemStack heldItem = player.getItemInHand(Hand.MAIN_HAND);
            if (!(heldItem.getItem() instanceof GunItem) || !Gun.hasAmmo(heldItem) && !player.isCreative()) {
                world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, 0.8F);
            } else {
                GunItem item = (GunItem)heldItem.getItem();
                Gun modifiedGun = item.getModifiedGun(heldItem);
                if (modifiedGun != null) {
                    player.yRot = yRot;
                    player.xRot = xRot;
                    if (!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) {
                        SpreadTracker.get(player).update(player, item);
                    }

                    int count = modifiedGun.getGeneral().getProjectileAmount();
                    Gun.Projectile projectileProps = modifiedGun.getProjectile();
                    ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

                    for(int i = 0; i < count; ++i) {
                        IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileProps.getItem());
                        ProjectileEntity projectileEntity = factory.create(world, player, heldItem, item, modifiedGun, randP, randY);
                        projectileEntity.setWeapon(heldItem);
                        projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                        world.addFreshEntity(projectileEntity);
                        spawnedProjectiles[i] = projectileEntity;
                        projectileEntity.tick();
                    }

                    if (!projectileProps.isVisible()) {
                        MessageBulletTrail messageBulletTrail = new MessageBulletTrail(spawnedProjectiles, projectileProps, player.getId(), projectileProps.getSize());
                        PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> {
                            return new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(), (Double)Config.COMMON.network.projectileTrackingRange.get(), player.level.dimension());
                        }), messageBulletTrail);
                    }

                    MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
                    double posY;
                    double posZ;
                    double posX;
                    if ((Boolean)Config.COMMON.aggroMobs.enabled.get()) {
                        double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, (Double)Config.COMMON.aggroMobs.range.get());
                        posX = player.getX();
                        posY = player.getY() + 0.5;
                        posZ = player.getZ();
                        AxisAlignedBB box = new AxisAlignedBB(posX - radius, posY - radius, posZ - radius, posX + radius, posY + radius, posZ + radius);
                        radius *= radius;
                        Iterator var26 = world.getEntitiesOfClass(LivingEntity.class, box, HOSTILE_ENTITIES).iterator();

                        while(var26.hasNext()) {
                            LivingEntity entity = (LivingEntity)var26.next();
                            double dx = posX - entity.getX();
                            double dy = posY - entity.getY();
                            double dz = posZ - entity.getZ();
                            if (dx * dx + dy * dy + dz * dz <= radius) {
                                entity.setLastHurtByMob((LivingEntity)((Boolean)Config.COMMON.aggroMobs.angerHostileMobs.get() ? player : entity));
                            }
                        }
                    }

                    boolean silenced = GunModifierHelper.isSilencedFire(heldItem);
                    ResourceLocation fireSound = silenced ? modifiedGun.getSounds().getSilencedFire() : modifiedGun.getSounds().getFire();
                    if (fireSound != null) {
                        posX = player.getX();
                        posY = player.getY() + (double)player.getEyeHeight();
                        posZ = player.getZ();
                        float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                        float pitch = 0.9F + world.random.nextFloat() * 0.125F;
                        double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, (Double)Config.SERVER.gunShotMaxDistance.get());
                        boolean muzzle = modifiedGun.getDisplay().getFlash() != null;
                        MessageGunSound messageSound = new MessageGunSound(fireSound, SoundCategory.PLAYERS, (float)posX, (float)posY, (float)posZ, volume, pitch, player.getId(), muzzle, false);
                        PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(posX, posY, posZ, radius, player.level.dimension());
                        PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> {
                            return targetPoint;
                        }), messageSound);
                    }

                    if (!player.isCreative()) {
                        CompoundNBT tag = heldItem.getOrCreateTag();
                        if (!tag.getBoolean("IgnoreAmmo")) {
                            int level = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)ModEnchantments.RECLAIMED.get(), heldItem);
                            if (level == 0 || player.level.random.nextInt(4 - MathHelper.clamp(level, 1, 2)) != 0) {
                                tag.putInt("AmmoCount", Math.max(0, tag.getInt("AmmoCount") - 1));
                            }
                        }
                    }
                }
            }
        }

    }

    public static void handleUnload(PlayerEntity player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundNBT tag = stack.getTag();
            GunItem gunItem = (GunItem)stack.getItem();
            Gun gun = gunItem.getModifiedGun(stack);
            if (tag != null && tag.contains("AmmoCount", 3)) {
                int count = tag.getInt("AmmoCount");
                tag.putInt("AmmoCount", 0);
                ResourceLocation id = gun.getProjectile().getItem();
                Item item = (Item)ForgeRegistries.ITEMS.getValue(id);
                if (item == null) {
                    return;
                }

                int maxStackSize = item.getMaxStackSize();
                int stacks = count / maxStackSize;

                int remaining;
                for(remaining = 0; remaining < stacks; ++remaining) {
                    spawnAmmo(player, new ItemStack(item, maxStackSize));
                }

                remaining = count % maxStackSize;
                if (remaining > 0) {
                    spawnAmmo(player, new ItemStack(item, remaining));
                }
            }

            ResourceLocation reloadSound = gun.getSounds().getCock();
            if (reloadSound != null) {
                MessageGunSound message = new MessageGunSound(reloadSound, SoundCategory.PLAYERS, (float)player.getX(), (float)player.getY() + 1.0F, (float)player.getZ(), 1.0F, 1.0F, player.getId(), false, true);
                PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> {
                    return new PacketDistributor.TargetPoint(player.getX(), player.getY() + 1.0, player.getZ(), 16.0, player.level.dimension());
                }), message);
            }
        }

    }

    private static void spawnAmmo(PlayerEntity player, ItemStack stack) {
        player.inventory.add(stack);
        if (stack.getCount() > 0) {
            player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack.copy()));
        }

    }

    public static void handleFireMode(PlayerEntity player) {
        ItemStack heldItem = player.getMainHandItem();

        try {
            if (heldItem.getItem() instanceof GunItem) {
                if (heldItem.getTag() == null) {
                    heldItem.getOrCreateTag();
                }

                GunItem gunItem = (GunItem)heldItem.getItem();
                Gun gun = gunItem.getModifiedGun(heldItem.getStack());
                int[] gunItemFireModes = heldItem.getTag().getIntArray("supportedFireModes");
                if (ArrayUtils.isEmpty(gunItemFireModes)) {
                    gunItemFireModes = gun.getGeneral().getRateSelector();
                    heldItem.getTag().putIntArray("supportedFireModes", gunItemFireModes);
                } else if (!Arrays.equals(gunItemFireModes, gun.getGeneral().getRateSelector())) {
                    heldItem.getTag().putIntArray("supportedFireModes", gun.getGeneral().getRateSelector());
                }

                int toCheck = ArrayUtils.indexOf(gunItemFireModes, heldItem.getTag().getInt("CurrentFireMode"));
                if (toCheck >= heldItem.getTag().getIntArray("supportedFireModes").length - 1) {
                    heldItem.getTag().remove("CurrentFireMode");
                    heldItem.getTag().putInt("CurrentFireMode", gunItemFireModes[0]);
                } else {
                    heldItem.getTag().remove("CurrentFireMode");
                    heldItem.getTag().putInt("CurrentFireMode", heldItem.getTag().getIntArray("supportedFireModes")[toCheck + 1]);
                }

                if (!(Boolean)Config.COMMON.gameplay.safetyExistence.get() && heldItem.getTag().getInt("CurrentFireMode") == 0 && gunItemFireModes.length > 2) {
                    heldItem.getTag().remove("CurrentFireMode");
                    heldItem.getTag().putInt("CurrentFireMode", heldItem.getTag().getIntArray("supportedFireModes")[1]);
                } else if (!(Boolean)Config.COMMON.gameplay.safetyExistence.get() && heldItem.getTag().getInt("CurrentFireMode") == 0) {
                    heldItem.getTag().remove("CurrentFireMode");
                    heldItem.getTag().putInt("CurrentFireMode", heldItem.getTag().getIntArray("supportedFireModes")[0]);
                }

                ResourceLocation fireModeSound = gun.getSounds().getCock();
                if (fireModeSound != null && player.isAlive()) {
                    MessageGunSound messageSound = new MessageGunSound(fireModeSound, SoundCategory.PLAYERS, (float)player.getX(), (float)(player.getY() + 1.0), (float)player.getZ(), 1.0F, 1.0F, player.getId(), false, false);
                    PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> {
                        return(ServerPlayerEntity) player;
                    }), messageSound);
                }
            }
        } catch (Exception var8) {
            GunMod.LOGGER.log(Level.ERROR, "Fire Mode check did not function properly");
        }

    }

    public static void EmptyMag(PlayerEntity player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            GunItem gunItem = (GunItem)heldItem.getItem();
            Gun gun = gunItem.getModifiedGun(heldItem.getStack());
            ResourceLocation fireModeSound = gun.getSounds().getCock();
            if (fireModeSound != null && player.isAlive()) {
                MessageGunSound messageSound = new MessageGunSound(fireModeSound, SoundCategory.PLAYERS, (float)player.getX(), (float)(player.getY() + 1.0), (float)player.getZ(), 1.2F, 0.75F, player.getId(), false, false);
                PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> {
                    return (ServerPlayerEntity) player;
                }), messageSound);
            }

            handleUnload(player);
        }

    }

    public static void handleFlashLight(ServerPlayerEntity player, int[] lookingRange) {
        if (player.getMainHandItem().getItem() instanceof GunItem && Gun.getAttachment(Type.SIDE_RAIL, player.getMainHandItem()) != null) {
            IWorld world = player.level;
            TileEntity tile = null;
            int[] var4 = lookingRange;
            int var5 = lookingRange.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                int itor = var4[var6];
                int x = lookingAt(player, itor).getX();
                int y = lookingAt(player, itor).getY();
                int z = lookingAt(player, itor).getZ();
                boolean createLight = false;

                for(int i = 0; i < 5; ++i) {
                    tile = world.getBlockEntity(new BlockPos(x, y, z));
                    if (tile instanceof FlashLightSource) {
                        createLight = true;
                        break;
                    }

                    if (!world.isEmptyBlock(new BlockPos(x, y, z))) {
                        int pX = (int)player.position().x();
                        int pY = (int)player.position().y();
                        int pZ = (int)player.position().z();
                        if (pX > x) {
                            ++x;
                        } else if (pX < x) {
                            --x;
                        }

                        if (pY > y) {
                            ++y;
                        } else if (pY < y) {
                            --y;
                        }

                        if (pZ > z) {
                            ++z;
                        } else if (pZ < z) {
                            --z;
                        }
                    } else if (world.isEmptyBlock(new BlockPos(x, y, z))) {
                        createLight = true;
                        break;
                    }
                }

                if (createLight) {
                    tile = world.getBlockEntity(new BlockPos(x, y, z));
                    if (tile instanceof FlashLightSource) {
                        FlashLightSource var10000 = (FlashLightSource)tile;
                        FlashLightSource.ticks = 0;
                    } else if (world.getBlockState(new BlockPos(x, y, z)).getBlock() != ModBlocks.FLASHLIGHT_BLOCK.get()) {
                        world.setBlock(new BlockPos(x, y, z), ((Block)ModBlocks.FLASHLIGHT_BLOCK.get()).defaultBlockState(), 3);
                    }

                    world.setBlock(new BlockPos(x, y, z), ((Block)ModBlocks.FLASHLIGHT_BLOCK.get()).defaultBlockState(), 3);
                }
            }
        }

    }

    protected static BlockPos lookingAt(PlayerEntity player, int rangeL) {
        return ((BlockRayTraceResult)player.pick((double)rangeL, 0.0F, false)).getBlockPos();
    }

    protected static RayTraceResult lookingAtEntity(PlayerEntity player, int rangeL) {
        return player.pick((double)rangeL, 0.0F, false);
    }

    private static void changeGunSpeedMod(ServerPlayerEntity entity, String name, double modifier) {
        AttributeModifier speedModifier = new AttributeModifier(speedUptId, name, modifier, Operation.MULTIPLY_TOTAL);
        ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstance.getModifier(speedUptId) != null) {
            attributeInstance.removeModifier(speedModifier);
        }

        attributeInstance.addPermanentModifier(speedModifier);
    }

    private static void removeGunSpeedMod(ServerPlayerEntity entity, String name, double modifier) {
        AttributeModifier speedModifier = new AttributeModifier(speedUptId, name, modifier, Operation.MULTIPLY_TOTAL);
        ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstance.getModifier(speedUptId) != null) {
            attributeInstance.removeModifier(speedModifier);
        }

    }

    public static void handleMovementUpdate(ServerPlayerEntity player, boolean handle) {
        if (player != null) {
            if (!player.isSpectator()) {
                if (player.isAlive()) {
                    ItemStack heldItem = player.getMainHandItem();
                    if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null && MovementAdaptationsHandler.get().isReadyToReset()) {
                        removeGunSpeedMod(player, "GunSpeedMod", 0.1);
                        MovementAdaptationsHandler.get().setReadyToReset(false);
                        MovementAdaptationsHandler.get().setReadyToUpdate(true);
                    }

                    player.onUpdateAbilities();
                    if (heldItem.getItem() instanceof TimelessGunItem) {
                        Gun gun = ((TimelessGunItem)heldItem.getItem()).getGun();
                        if (!MovementAdaptationsHandler.get().isReadyToUpdate() && MovementAdaptationsHandler.get().getPreviousWeight() == gun.getGeneral().getWeightKilo()) {
                            MovementAdaptationsHandler.get().setSpeed((float)player.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                        } else {
                            float speed = calceldGunWeightSpeed(gun, heldItem);
                            if (player.isSprinting() && speed > 0.094F) {
                                speed = Math.max(Math.min(speed, 0.12F), 0.075F);
                            } else if (player.isSprinting()) {
                                speed = Math.max(Math.min(speed, 0.12F), 0.075F) * 0.955F;
                            } else {
                                speed = Math.max(Math.min(speed, 0.1F), 0.075F);
                            }

                            changeGunSpeedMod(player, "GunSpeedMod", -((0.1 - (double)speed) * 10.0));
                            MovementAdaptationsHandler.get().setReadyToReset(true);
                            MovementAdaptationsHandler.get().setReadyToUpdate(false);
                            MovementAdaptationsHandler.get().setSpeed(speed);
                        }

                        player.onUpdateAbilities();
                        MovementAdaptationsHandler.get().setPreviousWeight(gun.getGeneral().getWeightKilo());
                    }
                }
            }
        }
    }

    public static float calceldGunWeightSpeed(Gun gun, ItemStack gunStack) {
        return 0.1F / (1.0F + (gun.getGeneral().getWeightKilo() * (1.0F + GunModifierHelper.getModifierOfWeaponWeight(gunStack)) + GunModifierHelper.getAdditionalWeaponWeight(gunStack) - GunEnchantmentHelper.getWeightModifier(gunStack)) / 2.0F * 0.0275F);
    }

    public static void handleGunID(ServerPlayerEntity player, boolean regenerate) {
        if (player.isAlive()) {
            if (NetworkGunManager.get() != null && NetworkGunManager.get().StackIds != null && player.getMainHandItem().getItem() instanceof TimelessGunItem && player.getMainHandItem().getTag() != null) {
                if (regenerate || !player.getMainHandItem().getTag().contains("ID")) {
                    UUID id;
                    do {
                        LOGGER.log(Level.INFO, "NEW UUID GEN FOR TAC GUN");
                        id = UUID.randomUUID();
                    } while(!NetworkGunManager.get().Ids.add(id));

                    player.getMainHandItem().getTag().putUUID("ID", id);
                    NetworkGunManager.get().StackIds.put(id, player.getMainHandItem());
                }

                initLevelTracking(player.getMainHandItem());
            }

        }
    }

    private static void initLevelTracking(ItemStack gunStack) {
        if (gunStack.getTag().get("level") == null) {
            gunStack.getTag().putInt("level", 1);
        }

        if (gunStack.getTag().get("levelDmg") == null) {
            gunStack.getTag().putFloat("levelDmg", 0.0F);
        }

    }

    public static void handleUpgradeBenchItem(MessageSaveItemUpgradeBench message, ServerPlayerEntity player) {
        if (!player.isSpectator()) {
            World world = player.level;
            ItemStack heldItem = player.getItemInHand(Hand.MAIN_HAND);
            TileEntity tileEntity = world.getBlockEntity(message.getPos());
            if (player.isCrouching()) {
                NetworkHooks.openGui(player, (INamedContainerProvider)tileEntity, tileEntity.getBlockPos());
                return;
            }

            world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, 0.8F);
            if (tileEntity != null) {
                if (!(((UpgradeBenchTileEntity)tileEntity).getItem(0).getItem() instanceof GunItem) && heldItem.getItem() instanceof GunItem) {
                    ((UpgradeBenchTileEntity)tileEntity).setItem(0, heldItem);
                    player.setItemInHand(Hand.MAIN_HAND, new ItemStack(Items.AIR));
                    NetworkHooks.openGui(player, (INamedContainerProvider)tileEntity, tileEntity.getBlockPos());
                    player.closeContainer();
                } else if (heldItem.getItem() == ModItems.MODULE.get() && ((UpgradeBenchTileEntity)tileEntity).getItem(1).getCount() < 3) {
                    if (((UpgradeBenchTileEntity)tileEntity).getItem(1).getItem() != ModItems.MODULE.get()) {
                        ((UpgradeBenchTileEntity)tileEntity).setItem(1, heldItem.copy());
                        ((UpgradeBenchTileEntity)tileEntity).getItem(1).setCount(1);
                    } else {
                        ((UpgradeBenchTileEntity)tileEntity).getItem(1).setCount(((UpgradeBenchTileEntity)tileEntity).getItem(1).getCount() + 1);
                    }

                    player.getItemInHand(Hand.MAIN_HAND).setCount(player.getItemInHand(Hand.MAIN_HAND).getCount() - 1);
                    NetworkHooks.openGui(player, (INamedContainerProvider)tileEntity, tileEntity.getBlockPos());
                    player.closeContainer();
                } else {
                    player.inventory.add(((UpgradeBenchTileEntity)tileEntity).getItem(0));
                    ((UpgradeBenchTileEntity)tileEntity).setItem(0, ItemStack.EMPTY);
                    NetworkHooks.openGui(player, (INamedContainerProvider)tileEntity, tileEntity.getBlockPos());
                    player.closeContainer();
                }
            }
        }

    }

    public static void handleUpgradeBenchApply(MessageUpgradeBenchApply message, ServerPlayerEntity player) {
        if (player.containerMenu instanceof UpgradeBenchContainer) {
            UpgradeBenchContainer workbench = (UpgradeBenchContainer)player.containerMenu;
            UpgradeBenchScreen.RequirementItem req = (UpgradeBenchScreen.RequirementItem)GunEnchantmentHelper.upgradeableEnchs.get(message.reqKey);
            if (workbench.getPos().equals(message.pos)) {
                ItemStack toUpdate = (ItemStack)workbench.getBench().getInventory().get(0);
                int currLevel = EnchantmentHelper.getItemEnchantmentLevel(req.enchantment, toUpdate);
                if (toUpdate.getTag() == null) {
                    return;
                }

                int currWeaponLevel = toUpdate.getTag().getInt("level");
                TimelessGunItem gunItem = (TimelessGunItem)toUpdate.getItem();
                if (workbench.getBench().getItem(1).getCount() >= req.getModuleCount()[currLevel] && currWeaponLevel >= req.getLevelReq()[currLevel] && gunItem.getGun().getGeneral().getUpgradeBenchMaxUses() > toUpdate.getTag().getInt("upgradeBenchUses")) {
                    if (currLevel > 0) {
                        Map<Enchantment, Integer> listNBT = EnchantmentHelper.deserializeEnchantments(toUpdate.getEnchantmentTags());
                        listNBT.replace(req.enchantment, currLevel + 1);
                        EnchantmentHelper.setEnchantments(listNBT, toUpdate);
                    } else {
                        toUpdate.enchant(req.enchantment, 1);
                    }

                    workbench.getBench().getItem(1).setCount(workbench.getBench().getItem(1).getCount() - req.getModuleCount()[currLevel]);
                    toUpdate.getTag().putInt("upgradeBenchUses", toUpdate.getTag().getInt("upgradeBenchUses") + 1);
                } else {
                    player.displayClientMessage(new TranslationTextComponent("Cannot apply enchants anymore"), true);
                }
            }
        }

    }

    public static void handleArmorFixApplication(ServerPlayerEntity player) {
        if (WearableHelper.PlayerWornRig(player) != null && !WearableHelper.isFullDurability(WearableHelper.PlayerWornRig(player))) {
            Rig rig = ((ArmorRigItem)WearableHelper.PlayerWornRig(player).getItem()).getRig();
            if (player.getMainHandItem().getItem().getRegistryName().equals(rig.getRepair().getItem())) {
                WearableHelper.tickRepairCurrentDurability(WearableHelper.PlayerWornRig(player));
                player.getMainHandItem().setCount(player.getMainHandItem().getCount() - 1);
                ResourceLocation repairSound = rig.getSounds().getRepair();
                if (repairSound != null && player.isAlive()) {
                    MessageGunSound messageSound = new MessageGunSound(repairSound, SoundCategory.PLAYERS, (float)player.getX(), (float)(player.getY() + 1.0), (float)player.getZ(), 1.0F, 1.0F, player.getId(), false, false);
                    PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> {
                        return player;
                    }), messageSound);
                }
            }
        }

    }

    public static void handleRigAmmoCount(ServerPlayerEntity player, ResourceLocation id) {
        if (WearableHelper.PlayerWornRig(player) != null) {
            ItemStack rig = WearableHelper.PlayerWornRig(player);
            if (rig != null) {
                PacketHandler.getPlayChannel().sendTo(new MessageRigInvToClient(rig, id), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        } else {
            PacketHandler.getPlayChannel().sendTo(new MessageRigInvToClient(), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }

    }
}
