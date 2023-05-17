/*
author: Ahmed Salem
 */

package com.aieis.cctind.registry;


/* TODO: Find Alternative Method as this is poorly extendable

    The reason it exists is because I don't know how to use an integer id for items (if it is at all possible)
 */
public class TACResourceLocations {

//    private static final Gson GSON_INSTANCE = Util.make(() -> {
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
//        builder.registerTypeAdapter(GripType.class, JsonDeserializers.GRIP_TYPE);
//        return builder.create();
//    });
//
//    protected class DefaultGunData {
//        int id;
//        GunItem gunItem;
//        Gun gun;
//
//        DefaultGunData(int nid, GunItem item, Gun ngun) {
//            id = nid;
//            gunItem = item;
//            gun = ngun;
//        }
//    }
//
//    protected Map<String, DefaultGunData> prepare()
//    {
//        Map<String, DefaultGunData> map = Maps.newHashMap();
//        GunMod.LOGGER.info("YO_DATA_GUN");
//        AtomicInteger idn = new AtomicInteger();
//        ForgeRegistries.ITEMS.getValues().stream().filter(item -> item instanceof GunItem).forEach(item ->
//        {
//            ResourceLocation id = item.getRegistryName();
//            if(id != null)
//            {
//                ResourceLocation resourceLocation = new ResourceLocation(String.format("%s:guns/%s.json", id.getNamespace(), id.getPath()));
//                try(IResource resource = (new ResourceManager()).getResource(resourceLocation); InputStream is = resource.getInputStream();
//                    Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
//                {
//                    Gun gun = JSONUtils.fromJson(GSON_INSTANCE, reader, Gun.class);
//                    if(gun != null && Validator.isValidObject(gun))
//                    {
//                        int n = idn.getAndIncrement();
//                        map.put((id.toString(), new DefaultGunData(n, (GunItem) item, gun));
//                    }
//                    else
//                    {
//                        int n = idn.getAndIncrement();
//                        GunMod.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default gun data", resourceLocation);
//                        map.put((id.toString(), new DefaultGunData(n, (GunItem) item, new Gun()));
//                    }
//                }
//                catch(InvalidObjectException e)
//                {
//                    GunMod.LOGGER.error("Missing required properties for {}", resourceLocation);
//                    e.printStackTrace();
//                }
//                catch(IOException e)
//                {
//                    GunMod.LOGGER.error("Couldn't parse data file {}", resourceLocation);
//                }
//                catch(IllegalAccessException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        });
//        return map;
//    }
//
//    public static final Map<String, DefaultGunData> GUN_DATA = prepare();
//

    public static final String[] RESOURCE_LOCATIONS = {
            "tac:m1911",
            "tac:m1894",
            "tac:m1851",
            "tac:magnumbox",
            "tac:m1928",
            "tac:mosin",
            "tac:ak47",
            "tac:box_308-block",
            "tac:box45",
            "tac:m60",
            "tac:m1917",
            "tac:glock_17",
            "tac:dp28",
            "tac:m16a1",
            "tac:mk18",
            "tac:sti2011",
            "tac:ak74",
            "tac:m92fs",
            "tac:nato_556_box_block",
            "tac:9mm_box_block",
            "tac:ar_15_p",
            "tac:ar_15_hellmouth",
            "tac:micro_uzi",
            "tac:vector45",
            "tac:walther_ppk",
            "tac:mosberg590",
            "tac:db_long",
            "tac:db_short",
            "tac:m4",
            "tac:m24",
            "tac:m1911_nether",
            "tac:ppsh_41",
            "tac:qbz_95",
            "tac:springfield_1903",
            "tac:aa_12",
            "tac:x95r",
            "tac:fr_f2",
            "tac:smle_iii",
            "tac:m870_classic",
            "tac:mg3",
            "tac:mg42",
            "tac:ar_10",
            "tac:m1a1_smg",
            "tac:mk14",
            "tac:spas_12",
            "tac:deagle_357",
            "tac:hk_mp5a5",
            "tac:sten_mk_ii",
            "tac:glock_18",
            "tac:m1873",
            "tac:cz75",
            "tac:cz75_auto",
            "tac:vz61",
            "tac:qsz92g1",
            "tac:kar98",
            "tac:hk416_a5",
            "tac:type81_x",
            "tac:pkp_penchenberg",
            "tac:mp7",
            "tac:m82a2",
            "tac:ai_awp",
            "tac:rpg7",
            "tac:rpk",
            "tac:fn_fal",
            "tac:de_lisle",
            "tac:m1_garand",
            "tac:sig_mcx_spear",
            "tac:mp9",
            "tac:sks",
            "tac:sks_tactical",
            "tac:m1014",
            "tac:m249",
            "tac:m79",
            "tac:mgl_40mm",
            "tac:mk23",
            "tac:qbz_191",
            "tac:c96",
            "tac:sten_mk_ii_oss",
            "tac:espadon",
            "tac:m16a4",
            "tac:scar_h",
            "tac:scar_l",
            "tac:mk47",
            "tac:tti_g34",
            "tac:mk18_mod1",
            "tac:spr15",
    };


    public static String get(int id) {
        return RESOURCE_LOCATIONS[id];
    }
    public static int get_id(String res) {
        for (int i = 0; i < RESOURCE_LOCATIONS.length; i++) if (res.equals(RESOURCE_LOCATIONS[i])) return i;
        return -1;
    }
}
