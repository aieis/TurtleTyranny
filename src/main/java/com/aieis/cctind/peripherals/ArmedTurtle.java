package com.aieis.cctind.peripherals;

import com.aieis.cctind.registry.TACResourceLocations;
import com.mojang.authlib.GameProfile;
import com.tac.guns.Config;
import com.tac.guns.client.handler.RecoilHandler;
import com.tac.guns.common.Gun;
import com.tac.guns.common.ProjectileManager;
import com.tac.guns.common.SpreadTracker;
import com.tac.guns.entity.ProjectileEntity;
import com.tac.guns.event.GunFireEvent;
import com.tac.guns.init.ModEnchantments;
import com.tac.guns.interfaces.IProjectileFactory;
import com.tac.guns.item.GunItem;
import com.tac.guns.network.PacketHandler;
import com.tac.guns.network.message.*;
import com.tac.guns.util.GunEnchantmentHelper;
import com.tac.guns.util.GunModifierHelper;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.aieis.cctind.CCTInd.mod_id;

class ProjectileEntityExtra extends ProjectileEntity
{
    private int ticks = 0;

    public ProjectileEntityExtra(EntityType<? extends Entity> entityType, World worldIn) {
        super(entityType, worldIn);
    }

    public ProjectileEntityExtra(EntityType<? extends Entity> entityType, World worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun, float randP, float randY) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun, randP, randY);
    }

    @Override
    public void tick() {
        super.tick();
        LiveLogger.live_log("Ticking " + String.valueOf(ticks++));
    }
}
class LiveLogger
{
    public static void live_log(String str)
    {
        PlayerEntity pe = Minecraft.getInstance().player;
        pe.displayClientMessage((new TranslationTextComponent(str)).withStyle(TextFormatting.GREEN), true);
    }
}

public class ArmedTurtle {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, mod_id);

    private static <T extends ProjectileEntity> RegistryObject<EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, World, T> function)
    {
        EntityType<T> type = EntityType.Builder.of(function::apply, EntityClassification.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> null)
                .build(id);
        return REGISTER.register(id, () -> type);
    }

    public static Gun make_clock() {
        Gun gun = new Gun();
        Gun.Projectile projectile = gun.getProjectile();
        CompoundNBT nbt = projectile.serializeNBT();
        nbt.putString("Item", "tac:9mm_round");
        nbt.putBoolean("Visible", false);
        nbt.putDouble("Damage", 5.0);
        nbt.putDouble("Size", 0.1);
        nbt.putDouble("Speed", 19.0);
        nbt.putDouble("Life", 12);
        projectile.deserializeNBT(nbt);

        Gun.General general = gun.getGeneral();
        nbt = general.serializeNBT();
        nbt.putInt("Rate", 400);
        int[] rate_s = {1};
        nbt.putIntArray("RateSelector", rate_s);
        nbt.putString("GripType", "tac:one_handed_m1911");
        nbt.putDouble("RecoilAngle", 9.25);
        nbt.putDouble("HorizontalRecoilAngle", 12.75);
        nbt.putDouble("RecoilKick", 2.05);
        nbt.putDouble("RecoilAdsReduction", 0.245);
        nbt.putDouble("WeaponRecoilOffset", 0.9525);
        nbt.putDouble("CameraRecoilModifier", 5.5);
        nbt.putDouble("RecoilDuration", 0.215);
        nbt.putDouble("WeightKilo", 0.0);
        nbt.putDouble("Spread", 1.3);
        nbt.putDouble("FirstShotSpread", 0.25);
        nbt.putDouble("HipFireInaccuracy", 2.35);
        nbt.putDouble("ProjToMinAccuracy", 3);
        nbt.putDouble("MsToAccuracyReset", 620);
        nbt.putDouble("MovementInaccuracy", 0.225);
        general.deserializeNBT(nbt);

        Gun.Reloads rel = gun.getReloads();
        nbt = rel.serializeNBT();
        nbt.putBoolean("MagFed", true);
        nbt.putInt("MaxAmmo", 17);
        nbt.putInt("ReloadMagTimer", 25);
        nbt.putInt("AdditionalReloadEmptyMagTimer", 7);
        int[] maapoc = {6, 10, 14};
        nbt.putIntArray("MaxAdditionalAmmoPerOC", maapoc);
        nbt.putInt("PreReloadPauseTicks", 0);
        rel.deserializeNBT(nbt);

        Gun.Sounds sound = gun.getSounds();
        nbt = sound.serializeNBT();
        nbt.putString("Fire", "tac:item.glock17_fire");
        nbt.putString("ReloadNormal", "tac:animation.glock17_reload_normal");
        nbt.putString("ReloadEmpty", "tac:animation.glock17_reload_empty");
        nbt.putString("Draw", "tac:animation.glock17_draw");
        nbt.putString("Inspect", "tac:animation.glock17_inspect");
        nbt.putString("Cock", "tac:item.pistol.cock");
        nbt.putString("SilencedFire", "tac:item.glock17_fire_s");
        sound.deserializeNBT(nbt);

        return gun;
    }
    public static final RegistryObject<EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);

    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = (entity) -> {
        return entity.getSoundSource() == SoundCategory.HOSTILE && !((List) Config.COMMON.aggroMobs.exemptEntities.get()).contains(entity.getType().getRegistryName().toString());
    };
    public static void handleShoot(MessageShoot message, ServerPlayerEntity player, float randP, float randY) {
        if (!player.isSpectator()) {
            World world = player.level;
            ItemStack heldItem = player.getItemInHand(Hand.MAIN_HAND);
            if (!(heldItem.getItem() instanceof GunItem) || !Gun.hasAmmo(heldItem) && !player.isCreative()) {
                world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, 0.8F);

            } else {
                GunItem item = (GunItem)heldItem.getItem();
                Gun modifiedGun = item.getModifiedGun(heldItem);
                if (modifiedGun != null) {
                    if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem))) {
                        //return;
                    }

                    player.yRot = message.getRotationYaw();
                    player.xRot = message.getRotationPitch();
                    if (!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) {
                        SpreadTracker.get(player).update(player, item);
                    }

                    int count = modifiedGun.getGeneral().getProjectileAmount();
                    Gun.Projectile projectileProps = modifiedGun.getProjectile();
                    ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

                    for(int i = 0; i < count; ++i) {
                        IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileProps.getItem());
                        ProjectileEntity projectileEntity = factory.create(world, player, heldItem, item, modifiedGun, 0, 0);
                        projectileEntity.setWeapon(heldItem);
                        projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                        world.addFreshEntity(projectileEntity);
                        spawnedProjectiles[i] = projectileEntity;
                        LiveLogger.live_log("Bullet Pos: " + projectileEntity.getX() + " " + projectileEntity.getY() + " " + projectileEntity.getZ());
                        projectileEntity.tick();
                        ProjectileEntity.createExplosion(projectileEntity, 5, false);


                        CompoundNBT compound = new CompoundNBT();
                        projectileEntity.addAdditionalSaveData(compound);
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

    public static PlayerEntity create_server_player(double x, double y, double z) {
        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getAllLevels().iterator().next();
        GameProfile profile = new GameProfile(null, "Armed Turtle");
        FakePlayer player = new FakePlayer(world, profile);
        player.setPos(-130, 69, -72);
        return player;
    }


    public static void prepare_player_inventory(PlayerEntity player, String gun_resource, int ammo_count) {
        //ItemStack stack = new ItemStack((GunItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation(gun_resource)));
        GunItem gunItem = (GunItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation(gun_resource));
        ItemStack stack = new ItemStack(gunItem);
        Gun gun = ((GunItem) stack.getItem()).getGun();
        Gun ngun = make_clock();
        CompoundNBT tag = ngun.serializeNBT();
        gun.deserializeNBT(tag);
        tag = stack.getOrCreateTag();
        tag.putInt("AmmoCount", 20);

        Item addAmmo = ForgeRegistries.ITEMS.getValue(gun.getProjectile().getItem());
        player.inventory.clearContent();
        player.inventory.setItem(0, stack);
        player.inventory.selected = 0;
        player.inventory.setItem(1, new ItemStack(addAmmo, 64));
        player.setItemInHand(Hand.MAIN_HAND, stack);

    }

    public static TurtlePlayer create_turtle_player(ItemStack item, ITurtleAccess turtle, BlockPos position, Direction direction) {
        final TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition(turtle, position, direction);
        ItemStack stack = item;//item.copy();

        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        Item addAmmo = ForgeRegistries.ITEMS.getValue(gun.getProjectile().getItem());
        turtlePlayer.loadInventory(stack);
        turtlePlayer.inventory.setItem(1, new ItemStack(addAmmo, 64));
        turtlePlayer.setItemInHand(Hand.MAIN_HAND, stack);

        if (!Gun.hasAmmo(stack)) {
            ItemStack ammo = Gun.findAmmo(turtlePlayer, gun.getProjectile().getItem())[0];
            if (!ammo.isEmpty()) {
                CompoundNBT tag = stack.getTag();
                int amount = Math.min(ammo.getCount(), gun.getReloads().getReloadAmount());
                if (tag != null) {
                    int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(stack, gun);
                    amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                    tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);
                }
            }
        }
        return turtlePlayer;
    }

    public static void fire(PlayerEntity player, ItemStack heldItem, Vector3i position) {
            if (heldItem.getItem() instanceof GunItem) {
            if (Gun.hasAmmo(heldItem) || player.isCreative()) {

                PacketHandler.getPlayChannel().sendToServer(new MessageShooting(true));
                float dist = Math.abs(player.zza) / 2.5F + Math.abs(player.xxa) / 1.25F + (player.getDeltaMovement().y > 0.0 ? 0.5F : 0.0F);
                PacketHandler.getPlayChannel().sendToServer(new MessageUpdateMoveInacc(dist));

                GunItem gunItem = (GunItem) heldItem.getItem();
                Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem))) {
                    return;
                }

                float rpm = (float)modifiedGun.getGeneral().getRate();
                RecoilHandler.get().lastRandPitch = RecoilHandler.get().lastRandPitch;
                RecoilHandler.get().lastRandYaw = RecoilHandler.get().lastRandYaw;

                int ammo_count = 0;
                CompoundNBT tag = heldItem.getTag();
                if (tag != null) {
                    ammo_count = tag.getInt("AmmoCount");
                }
                MessageShoot msg = new MessageShootGeneric(TACResourceLocations.get_id(ForgeRegistries.ITEMS.getKey(heldItem.getItem()).toString()), ammo_count, position, player.getViewYRot(1.0F), player.getViewXRot(1.0F), RecoilHandler.get().lastRandPitch, RecoilHandler.get().lastRandYaw);
                //PacketHandlerExtra.getPlayChannel().sendToServer(msg);
                handleShoot(msg, (ServerPlayerEntity) player, RecoilHandler.get().lastRandPitch, RecoilHandler.get().lastRandYaw);

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
                //LiveLogger.live_log("Player Pos: " + player.getX() + " " + player.getY() + " " + player.getZ());

            }
        }
    }
}
