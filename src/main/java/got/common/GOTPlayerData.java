package got.common;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import got.GOT;
import got.common.GOTDimension.DimensionRegion;
import got.common.block.table.GOTBlockCraftingTable;
import got.common.command.GOTCommandAdminHideMap;
import got.common.database.*;
import got.common.entity.dragon.GOTEntityDragon;
import got.common.entity.essos.gold.GOTEntityGoldenMan;
import got.common.entity.other.GOTEntityNPC;
import got.common.faction.*;
import got.common.fellowship.*;
import got.common.item.other.GOTItemArmor;
import got.common.item.weapon.GOTItemCrossbowBolt;
import got.common.network.*;
import got.common.quest.GOTMiniQuest;
import got.common.quest.GOTMiniQuestEvent;
import got.common.quest.GOTMiniQuestWelcome;
import got.common.quest.MiniQuestSelector;
import got.common.util.GOTLog;
import got.common.world.GOTWorldProvider;
import got.common.world.biome.GOTBiome;
import got.common.world.map.*;
import got.common.world.map.GOTWaypoint.Region;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import got.common.network.GOTPacketFireballTimer;

import java.util.*;

public class GOTPlayerData {
    public static int ticksUntilFT_max = 200;
    public UUID playerUUID;
    public boolean needsSave;
    public int pdTick;
    public Map<GOTFaction, Float> alignments = new EnumMap<>(GOTFaction.class);
    public Map<GOTFaction, GOTFactionData> factionDataMap = new EnumMap<>(GOTFaction.class);
    public GOTFaction viewingFaction;
    public Map<GOTDimension.DimensionRegion, GOTFaction> prevRegionFactions = new EnumMap<>(DimensionRegion.class);
    public boolean hideAlignment;
    public Set<GOTFaction> takenAlignmentRewards = EnumSet.noneOf(GOTFaction.class);
    public GOTFaction pledgeFaction;
    public int pledgeKillCooldown;
    public int pledgeBreakCooldown;
    public int pledgeBreakCooldownStart;
    public GOTFaction brokenPledgeFaction;
    public boolean hideOnMap;
    public boolean adminHideMap;
    public boolean showWaypoints = true;
    public boolean showCustomWaypoints = true;
    public boolean showHiddenSharedWaypoints = true;
    public boolean conquestKills = true;
    public List<GOTAchievement> achievements = new ArrayList<>();
    public GOTShields shield;
    public boolean friendlyFire;
    public boolean hiredDeathMessages = true;
    public ChunkCoordinates deathPoint;
    public int deathDim;
    public int alcoholTolerance;
    public List<GOTMiniQuest> miniQuests = new ArrayList<>();
    public List<GOTMiniQuest> miniQuestsCompleted = new ArrayList<>();
    public int completedMiniquestCount;
    public int completedBountyQuests;
    public UUID trackingMiniQuestID;
    public List<GOTFaction> bountiesPlaced = new ArrayList<>();
    public GOTWaypoint lastWaypoint;
    public GOTBiome lastBiome;
    public Map<GOTGuiMessageTypes, Boolean> sentMessageTypes = new EnumMap<>(GOTGuiMessageTypes.class);
    public GOTTitle.PlayerTitle playerTitle;
    public boolean femRankOverride;
    public int ftSinceTick;
    public GOTAbstractWaypoint targetFTWaypoint;
    public int ticksUntilFT;
    public UUID uuidToMount;
    public int uuidToMountTime;
    public long lastOnlineTime = -1L;
    public Set<GOTWaypoint.Region> unlockedFTRegions = EnumSet.noneOf(Region.class);
    public List<GOTCustomWaypoint> customWaypoints = new ArrayList<>();
    public List<GOTCustomWaypoint> customWaypointsShared = new ArrayList<>();
    public Set<CWPSharedKey> cwpSharedUnlocked = new HashSet<>();
    public Set<CWPSharedKey> cwpSharedHidden = new HashSet<>();
    public Map<GOTWaypoint, Integer> wpUseCounts = new EnumMap<>(GOTWaypoint.class);
    public Map<Integer, Integer> cwpUseCounts = new HashMap<>();
    public Map<CWPSharedKey, Integer> cwpSharedUseCounts = new HashMap<>();
    public int nextCwpID = 20000;
    public List<UUID> fellowshipIDs = new ArrayList<>();
    public List<GOTFellowshipClient> fellowshipsClient = new ArrayList<>();
    public List<GOTFellowshipInvite> fellowshipInvites = new ArrayList<>();
    public List<GOTFellowshipClient> fellowshipInvitesClient = new ArrayList<>();
    public UUID chatBoundFellowshipID;
    public boolean structuresBanned;
    public GOTPlayerQuestData questData = new GOTPlayerQuestData(this);
    public int siegeActiveTime;
    public boolean teleportedKW;
    public GOTCapes cape;
    public int balance;
    public boolean checkedMenu;
    public boolean askedForJaqen;
    public boolean tableSwitched;
    private int dragonFireballSinceTick;

    public GOTPlayerData(UUID uuid) {
        this.playerUUID = uuid;
        this.viewingFaction = GOTFaction.NORTH;
        this.ftSinceTick = GOTLevelData.getWaypointCooldownMax() * 20;
        this.dragonFireballSinceTick = GOTConfig.getDragonFireballCooldown * 20;
    }

    public static ArmorMaterial getBodyMaterial(EntityLivingBase entity) {
        ItemStack item = entity.getEquipmentInSlot(3);
        if (item == null || !(item.getItem() instanceof GOTItemArmor))
            return null;
        return ((GOTItemArmor) item.getItem()).getArmorMaterial();
    }

    public static ItemArmor.ArmorMaterial getFullArmorMaterial(EntityLivingBase entity) {
        ItemArmor.ArmorMaterial material = null;
        for (int i = 1; i <= 4; ++i) {
            ItemStack item = entity.getEquipmentInSlot(i);
            if (item == null || !(item.getItem() instanceof ItemArmor))
                return null;
            ItemArmor.ArmorMaterial itemMaterial = ((ItemArmor) item.getItem()).getArmorMaterial();
            if (material != null && itemMaterial != material)
                return null;
            material = itemMaterial;
        }
        return material;
    }

    public static ArmorMaterial getFullArmorMaterialWithoutHelmet(EntityLivingBase entity) {
        ArmorMaterial material = null;
        for (int i = 1; i <= 3; ++i) {
            ItemStack item = entity.getEquipmentInSlot(i);
            if (item == null || !(item.getItem() instanceof GOTItemArmor))
                return null;
            ArmorMaterial itemMaterial = ((GOTItemArmor) item.getItem()).getArmorMaterial();
            if (material != null && itemMaterial != material)
                return null;
            material = itemMaterial;
        }
        return material;
    }

    public static ArmorMaterial getHelmetMaterial(EntityLivingBase entity) {
        ItemStack item = entity.getEquipmentInSlot(4);
        if (item == null || !(item.getItem() instanceof GOTItemArmor))
            return null;
        return ((GOTItemArmor) item.getItem()).getArmorMaterial();
    }

    public static boolean isTimerAutosaveTick() {
        return MinecraftServer.getServer() != null && MinecraftServer.getServer().getTickCounter() % 200 == 0;
    }

    public void acceptFellowshipInvite(GOTFellowship fs, boolean respectSizeLimit) {
        UUID fsID = fs.getFellowshipID();
        GOTFellowshipInvite existingInvite = null;
        for (GOTFellowshipInvite invite : this.fellowshipInvites) {
            if (invite.fellowshipID.equals(fsID)) {
                existingInvite = invite;
                break;
            }
        }
        if (existingInvite != null) {
            EntityPlayer entityplayer = this.getPlayer();
            if (fs.isDisbanded()) {
                this.rejectFellowshipInvite(fs);
                if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                    GOTPacketFellowshipAcceptInviteResult resultPacket = new GOTPacketFellowshipAcceptInviteResult(fs, GOTPacketFellowshipAcceptInviteResult.AcceptInviteResult.DISBANDED);
                    GOTPacketHandler.networkWrapper.sendTo(resultPacket, (EntityPlayerMP) entityplayer);
                }
            } else {
                int limit = GOTConfig.fellowshipMaxSize;
                if (respectSizeLimit && limit >= 0 && fs.getPlayerCount() >= limit) {
                    this.rejectFellowshipInvite(fs);
                    if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                        GOTPacketFellowshipAcceptInviteResult resultPacket = new GOTPacketFellowshipAcceptInviteResult(fs, GOTPacketFellowshipAcceptInviteResult.AcceptInviteResult.TOO_LARGE);
                        GOTPacketHandler.networkWrapper.sendTo(resultPacket, (EntityPlayerMP) entityplayer);
                    }
                } else {
                    fs.addMember(this.playerUUID);
                    this.fellowshipInvites.remove(existingInvite);
                    this.markDirty();
                    this.sendFellowshipInviteRemovePacket(fs);
                    if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                        GOTPacketFellowshipAcceptInviteResult resultPacket = new GOTPacketFellowshipAcceptInviteResult(fs, GOTPacketFellowshipAcceptInviteResult.AcceptInviteResult.JOINED);
                        GOTPacketHandler.networkWrapper.sendTo(resultPacket, (EntityPlayerMP) entityplayer);
                        UUID inviterID = existingInvite.inviterID;
                        if (inviterID == null) {
                            inviterID = fs.getOwner();
                        }
                        EntityPlayer inviter = this.getOtherPlayer(inviterID);
                        if (inviter != null) {
                            fs.sendNotification(inviter, "got.gui.fellowships.notifyAccept", entityplayer.getCommandSenderName());
                        }
                    }
                }
            }
        }
    }

    public void addAchievement(GOTAchievement achievement) {
        if (this.hasAchievement(achievement) || this.isSiegeActive())
            return;
        this.achievements.add(achievement);
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            boolean canEarn = achievement.canPlayerEarn(entityplayer);
            this.sendAchievementPacket((EntityPlayerMP) entityplayer, achievement, canEarn);
            if (canEarn) {
                achievement.broadcastEarning(entityplayer);
                List<GOTAchievement> earnedAchievements = this.getEarnedAchievements(GOTDimension.GAME_OF_THRONES);
                int biomes = 0;
                for (GOTAchievement earnedAchievement : earnedAchievements) {
                    if (earnedAchievement.isBiomeAchievement) {
                        biomes++;
                    }
                }
                if (biomes >= 10) {
                    this.addAchievement(GOTAchievement.travel10);
                }
                if (biomes >= 20) {
                    this.addAchievement(GOTAchievement.travel20);
                }
                if (biomes >= 30) {
                    this.addAchievement(GOTAchievement.travel30);
                }
                if (biomes >= 40) {
                    this.addAchievement(GOTAchievement.travel40);
                }
                if (biomes >= 50) {
                    this.addAchievement(GOTAchievement.travel50);
                }
            }
        }
    }

    public void addAlignment(EntityPlayer entityplayer, GOTAlignmentValues.AlignmentBonus source, GOTFaction faction, double posX, double posY, double posZ) {
        this.addAlignment(entityplayer, source, faction, null, posX, posY, posZ);
    }

    public GOTAlignmentBonusMap addAlignment(EntityPlayer entityplayer, GOTAlignmentValues.AlignmentBonus source, GOTFaction faction, Entity entity) {
        return this.addAlignment(entityplayer, source, faction, null, entity);
    }

    public GOTAlignmentBonusMap addAlignment(EntityPlayer entityplayer, GOTAlignmentValues.AlignmentBonus source, GOTFaction faction, List<GOTFaction> forcedBonusFactions, double posX, double posY, double posZ) {
        float bonus = source.bonus;
        GOTAlignmentBonusMap factionBonusMap = new GOTAlignmentBonusMap();
        float prevMainAlignment = this.getAlignment(faction);
        float conquestBonus = 0.0F;
        if (source.isKill) {
            List<GOTFaction> killBonuses = faction.getBonusesForKilling();
            for (GOTFaction bonusFaction : killBonuses) {
                if (bonusFaction.isPlayableAlignmentFaction() && (bonusFaction.approvesWarCrimes || !source.isCivilianKill)) {
                    if (!source.killByHiredUnit) {
                        float mplier;
                        if (forcedBonusFactions != null && forcedBonusFactions.contains(bonusFaction)) {
                            mplier = 1.0F;
                        } else {
                            mplier = bonusFaction.getControlZoneAlignmentMultiplier(entityplayer);
                        }
                        if (mplier > 0.0F) {
                            float alignment = this.getAlignment(bonusFaction);
                            float factionBonus = Math.abs(bonus);
                            factionBonus *= mplier;
                            if (alignment >= bonusFaction.getPledgeAlignment() && !this.isPledgedTo(bonusFaction)) {
                                factionBonus *= 0.5F;
                            }
                            factionBonus = this.checkBonusForPledgeEnemyLimit(bonusFaction, factionBonus);
                            alignment += factionBonus;
                            this.setAlignment(bonusFaction, alignment);
                            factionBonusMap.put(bonusFaction, factionBonus);
                        }
                    }
                    if (bonusFaction == this.pledgeFaction) {
                        float conq = bonus;
                        if (source.killByHiredUnit) {
                            conq *= 0.25F;
                        }
                        conquestBonus = GOTConquestGrid.onConquestKill(entityplayer, bonusFaction, faction, conq);
                        this.getFactionData(bonusFaction).addConquest(Math.abs(conquestBonus));
                    }
                }
            }
            List<GOTFaction> killPenalties = faction.getPenaltiesForKilling();
            for (GOTFaction penaltyFaction : killPenalties) {
                if (penaltyFaction.isPlayableAlignmentFaction() && !source.killByHiredUnit) {
                    float mplier;
                    if (penaltyFaction == faction) {
                        mplier = 1.0F;
                    } else {
                        mplier = penaltyFaction.getControlZoneAlignmentMultiplier(entityplayer);
                    }
                    if (mplier > 0.0F) {
                        float alignment = this.getAlignment(penaltyFaction);
                        float factionPenalty = -Math.abs(bonus);
                        factionPenalty *= mplier;
                        factionPenalty = GOTAlignmentValues.AlignmentBonus.scalePenalty(factionPenalty, alignment);
                        alignment += factionPenalty;
                        this.setAlignment(penaltyFaction, alignment);
                        factionBonusMap.put(penaltyFaction, factionPenalty);
                    }
                }
            }
        } else if (faction.isPlayableAlignmentFaction()) {
            float alignment = this.getAlignment(faction);
            float factionBonus = bonus;
            if (factionBonus > 0.0F && alignment >= faction.getPledgeAlignment() && !this.isPledgedTo(faction)) {
                factionBonus *= 0.5F;
            }
            factionBonus = this.checkBonusForPledgeEnemyLimit(faction, factionBonus);
            alignment += factionBonus;
            this.setAlignment(faction, alignment);
            factionBonusMap.put(faction, factionBonus);
        }
        if (!factionBonusMap.isEmpty() || conquestBonus != 0.0F) {
            this.sendAlignmentBonusPacket(source, faction, prevMainAlignment, factionBonusMap, conquestBonus, posX, posY, posZ);
        }
        return factionBonusMap;
    }

    public GOTAlignmentBonusMap addAlignment(EntityPlayer entityplayer, GOTAlignmentValues.AlignmentBonus source, GOTFaction faction, List<GOTFaction> forcedBonusFactions, Entity entity) {
        return this.addAlignment(entityplayer, source, faction, forcedBonusFactions, entity.posX, entity.boundingBox.minY + entity.height * 0.7D, entity.posZ);
    }

    public void addAlignmentFromCommand(GOTFaction faction, float add) {
        float alignment = this.getAlignment(faction);
        alignment += add;
        this.setAlignment(faction, alignment);
    }

    public void addCompletedBountyQuest() {
        this.completedBountyQuests++;
        this.markDirty();
    }

    public void addCustomWaypoint(GOTCustomWaypoint waypoint) {
        this.customWaypoints.add(waypoint);
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketCreateCWPClient packet = waypoint.getClientPacket();
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            GOTCustomWaypointLogger.logCreate(entityplayer, waypoint);
        }
        GOTCustomWaypoint shareCopy = waypoint.createCopyOfShared(this.playerUUID);
        List<UUID> sharedPlayers = shareCopy.getPlayersInAllSharedFellowships();
        for (UUID sharedPlayerUUID : sharedPlayers) {
            EntityPlayer sharedPlayer = this.getOtherPlayer(sharedPlayerUUID);
            if (sharedPlayer != null && !sharedPlayer.worldObj.isRemote) {
                GOTLevelData.getData(sharedPlayerUUID).addOrUpdateSharedCustomWaypoint(shareCopy);
            }
        }
    }

    public void addCustomWaypointClient(GOTCustomWaypoint waypoint) {
        this.customWaypoints.add(waypoint);
    }

    public void addFellowship(GOTFellowship fs) {
        if (fs.containsPlayer(this.playerUUID)) {
            UUID fsID = fs.getFellowshipID();
            if (!this.fellowshipIDs.contains(fsID)) {
                this.fellowshipIDs.add(fsID);
                this.markDirty();
                this.sendFellowshipPacket(fs);
                this.addSharedCustomWaypointsFromAllIn(fs.getFellowshipID());
            }
        }
    }

    public void addFellowshipInvite(GOTFellowship fs, UUID inviterUUID, String inviterUsername) {
        UUID fsID = fs.getFellowshipID();
        boolean hasInviteAlready = false;
        for (GOTFellowshipInvite invite : this.fellowshipInvites) {
            if (invite.fellowshipID.equals(fsID)) {
                hasInviteAlready = true;
                break;
            }
        }
        if (!hasInviteAlready) {
            this.fellowshipInvites.add(new GOTFellowshipInvite(fsID, inviterUUID));
            this.markDirty();
            this.sendFellowshipInvitePacket(fs);
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                fs.sendNotification(entityplayer, "got.gui.fellowships.notifyInvite", inviterUsername);
            }
        }
    }

    public void addMiniQuest(GOTMiniQuest quest) {
        this.miniQuests.add(quest);
        this.updateMiniQuest(quest);
    }

    public void addMiniQuestCompleted(GOTMiniQuest quest) {
        this.miniQuestsCompleted.add(quest);
        this.markDirty();
    }

    public void addOrUpdateClientFellowship(GOTFellowshipClient fs) {
        UUID fsID = fs.getFellowshipID();
        GOTFellowshipClient inList = null;
        for (GOTFellowshipClient fsInList : this.fellowshipsClient) {
            if (fsInList.getFellowshipID().equals(fsID)) {
                inList = fsInList;
                break;
            }
        }
        if (inList != null) {
            inList.updateDataFrom(fs);
        } else {
            this.fellowshipsClient.add(fs);
        }
    }

    public void addOrUpdateClientFellowshipInvite(GOTFellowshipClient fs) {
        UUID fsID = fs.getFellowshipID();
        GOTFellowshipClient inList = null;
        for (GOTFellowshipClient fsInList : this.fellowshipInvitesClient) {
            if (fsInList.getFellowshipID().equals(fsID)) {
                inList = fsInList;
                break;
            }
        }
        if (inList != null) {
            inList.updateDataFrom(fs);
        } else {
            this.fellowshipInvitesClient.add(fs);
        }
    }

    public void addOrUpdateSharedCustomWaypoint(GOTCustomWaypoint waypoint) {
        if (!waypoint.isShared()) {
            FMLLog.warning("Hummel009: Warning! Tried to cache a shared custom waypoint with no owner!");
            return;
        }
        if (waypoint.getSharingPlayerID().equals(this.playerUUID)) {
            FMLLog.warning("Hummel009: Warning! Tried to share a custom waypoint to its own player (%s)!", this.playerUUID.toString());
            return;
        }
        CWPSharedKey key = CWPSharedKey.keyFor(waypoint.getSharingPlayerID(), waypoint.getID());
        if (this.cwpSharedUnlocked.contains(key)) {
            waypoint.setSharedUnlocked();
        }
        waypoint.setSharedHidden(this.cwpSharedHidden.contains(key));
        GOTCustomWaypoint existing = this.getSharedCustomWaypointByID(waypoint.getSharingPlayerID(), waypoint.getID());
        if (existing == null) {
            this.customWaypointsShared.add(waypoint);
        } else {
            if (existing.isSharedUnlocked()) {
                waypoint.setSharedUnlocked();
            }
            waypoint.setSharedHidden(existing.isSharedHidden());
            int i = this.customWaypointsShared.indexOf(existing);
            this.customWaypointsShared.set(i, waypoint);
        }
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketCreateCWPClient packet = waypoint.getClientPacketShared();
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void addSharedCustomWaypointsFrom(UUID onlyOneFellowshipID, List<UUID> checkSpecificPlayers) {
        List<UUID> checkFellowshipIDs;
        if (onlyOneFellowshipID != null) {
            checkFellowshipIDs = new ArrayList<>();
            checkFellowshipIDs.add(onlyOneFellowshipID);
        } else {
            checkFellowshipIDs = this.fellowshipIDs;
        }
        List<UUID> checkFellowPlayerIDs = new ArrayList<>();
        if (checkSpecificPlayers != null) {
            for (UUID player : checkSpecificPlayers) {
                if (!player.equals(this.playerUUID)) {
                    checkFellowPlayerIDs.add(player);
                }
            }
        } else {
            for (UUID fsID : checkFellowshipIDs) {
                GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
                if (fs != null) {
                    Set<UUID> playerIDs = fs.getWaypointSharerUUIDs();
                    for (UUID player : playerIDs) {
                        if (!player.equals(this.playerUUID) && !checkFellowPlayerIDs.contains(player)) {
                            checkFellowPlayerIDs.add(player);
                        }
                    }
                }
            }
        }
        for (UUID player : checkFellowPlayerIDs) {
            GOTPlayerData pd = GOTLevelData.getData(player);
            List<GOTCustomWaypoint> cwps = pd.customWaypoints;
            for (GOTCustomWaypoint waypoint : cwps) {
                boolean inSharedFellowship = false;
                for (UUID fsID : checkFellowshipIDs) {
                    if (waypoint.hasSharedFellowship(fsID)) {
                        inSharedFellowship = true;
                        break;
                    }
                }
                if (inSharedFellowship) {
                    this.addOrUpdateSharedCustomWaypoint(waypoint.createCopyOfShared(player));
                }
            }
        }
    }

    public void addSharedCustomWaypointsFromAllFellowships() {
        this.addSharedCustomWaypointsFrom(null, null);
    }

    public void addSharedCustomWaypointsFromAllIn(UUID onlyOneFellowshipID) {
        this.addSharedCustomWaypointsFrom(onlyOneFellowshipID, null);
    }

    public boolean anyMatchingFellowshipNames(String name, boolean client) {
        name = StringUtils.strip(name).toLowerCase();
        if (client) {
            for (GOTFellowshipClient fs : this.fellowshipsClient) {
                String otherName = fs.getName();
                otherName = StringUtils.strip(otherName).toLowerCase();
                if (name.equals(otherName))
                    return true;
            }
        } else {
            for (UUID fsID : this.fellowshipIDs) {
                GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
                if (fs != null) {
                    String otherName = fs.getName();
                    otherName = StringUtils.strip(otherName).toLowerCase();
                    if (name.equals(otherName))
                        return true;
                }
            }
        }
        return false;
    }

    public void cancelFastTravel() {
        if (this.targetFTWaypoint != null) {
            this.setTargetFTWaypoint(null);
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                entityplayer.addChatMessage(new ChatComponentTranslation("got.fastTravel.motion"));
            }
        }
    }

    public boolean canCreateFellowships(boolean client) {
        int max = this.getMaxLeadingFellowships();
        int leading = 0;
        if (client) {
            for (GOTFellowshipClient fs : this.fellowshipsClient) {
                if (fs.isOwned()) {
                    leading++;
                    if (leading >= max)
                        return false;
                }
            }
        } else {
            for (UUID fsID : this.fellowshipIDs) {
                GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
                if (fs != null && fs.isOwner(this.playerUUID)) {
                    leading++;
                    if (leading >= max)
                        return false;
                }
            }
        }
        return leading < max;
    }

    public boolean canFastTravel() {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null) {
            World world = entityplayer.worldObj;
            if (!entityplayer.capabilities.isCreativeMode) {
                double range = 16.0D;
                List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class, entityplayer.boundingBox.expand(range, range, range));
                for (EntityLiving entity : entities) {
                    if (entity.getAttackTarget() == entityplayer)
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean canMakeNewPledge() {
        return this.pledgeBreakCooldown <= 0;
    }

    public boolean canPledgeTo(GOTFaction fac) {
        if (fac.isPlayableAlignmentFaction())
            return this.hasPledgeAlignment(fac);
        return false;
    }

    public void checkAlignmentAchievements(GOTFaction faction, float prevAlignment) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            float alignment = this.getAlignment(faction);
            faction.checkAlignmentAchievements(entityplayer, alignment);
        }
    }

    public float checkBonusForPledgeEnemyLimit(GOTFaction fac, float bonus) {
        if (this.isPledgeEnemyAlignmentLimited(fac)) {
            float limit;
            float alignment = this.getAlignment(fac);
            limit = this.getPledgeEnemyAlignmentLimit(fac);
            if (alignment > limit) {
                bonus = 0.0f;
            } else if (alignment + bonus > limit) {
                bonus = limit - alignment;
            }
        }
        return bonus;
    }

    public void checkCustomWaypointsSharedBy(Collection<UUID> checkSpecificPlayers) {
        List<GOTCustomWaypoint> removes = new ArrayList<>();
        for (GOTCustomWaypoint waypoint : this.customWaypointsShared) {
            UUID waypointSharingPlayer = waypoint.getSharingPlayerID();
            if (checkSpecificPlayers == null || checkSpecificPlayers.contains(waypointSharingPlayer)) {
                GOTCustomWaypoint wpOriginal = GOTLevelData.getData(waypointSharingPlayer).getCustomWaypointByID(waypoint.getID());
                if (wpOriginal != null) {
                    List<UUID> sharedFellowPlayers = wpOriginal.getPlayersInAllSharedFellowships();
                    if (!sharedFellowPlayers.contains(this.playerUUID)) {
                        removes.add(waypoint);
                    }
                }
            }
        }
        for (GOTCustomWaypoint waypoint : removes) {
            this.removeSharedCustomWaypoint(waypoint);
        }
    }

    public void checkCustomWaypointsSharedFromFellowships() {
        this.checkCustomWaypointsSharedBy(null);
    }

    public void checkIfStillWaypointSharerForFellowship(GOTFellowship fs) {
        if (!this.hasAnyWaypointsSharedToFellowship(fs)) {
            fs.markIsWaypointSharer(this.playerUUID, false);
        }
    }

    public void completeMiniQuest(GOTMiniQuest quest) {
        if (this.miniQuests.remove(quest)) {
            this.addMiniQuestCompleted(quest);
            this.completedMiniquestCount++;
            this.getFactionData(quest.entityFaction).completeMiniQuest();
            this.markDirty();
            GOT.proxy.setTrackedQuest(quest);
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketMiniquestRemove packet = new GOTPacketMiniquestRemove(quest, false, true);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        } else {
            FMLLog.warning("Warning: Attempted to remove a miniquest which does not belong to the player data");
        }
    }

    public void createFellowship(String name, boolean normalCreation) {
        if (normalCreation && (!GOTConfig.enableFellowshipCreation || !this.canCreateFellowships(false)))
            return;
        if (!this.anyMatchingFellowshipNames(name, false)) {
            GOTFellowship fellowship = new GOTFellowship(this.playerUUID, name);
            fellowship.createAndRegister();
        }
    }

    public void customWaypointAddSharedFellowship(GOTCustomWaypoint waypoint, GOTFellowship fs) {
        UUID fsID = fs.getFellowshipID();
        waypoint.addSharedFellowship(fsID);
        this.markDirty();
        fs.markIsWaypointSharer(this.playerUUID, true);
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketShareCWPClient packet = waypoint.getClientAddFellowshipPacket(fsID);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
        GOTCustomWaypoint shareCopy = waypoint.createCopyOfShared(this.playerUUID);
        for (UUID player : fs.getAllPlayerUUIDs()) {
            if (!player.equals(this.playerUUID)) {
                GOTLevelData.getData(player).addOrUpdateSharedCustomWaypoint(shareCopy);
            }
        }
    }

    public void customWaypointAddSharedFellowshipClient(GOTCustomWaypoint waypoint, GOTFellowshipClient fs) {
        waypoint.addSharedFellowship(fs.getFellowshipID());
    }

    public void customWaypointRemoveSharedFellowship(GOTCustomWaypoint waypoint, GOTFellowship fs) {
        UUID fsID = fs.getFellowshipID();
        waypoint.removeSharedFellowship(fsID);
        this.markDirty();
        this.checkIfStillWaypointSharerForFellowship(fs);
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketShareCWPClient packet = waypoint.getClientRemoveFellowshipPacket(fsID);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
        GOTCustomWaypoint shareCopy = waypoint.createCopyOfShared(this.playerUUID);
        for (UUID player : fs.getAllPlayerUUIDs()) {
            if (!player.equals(this.playerUUID)) {
                GOTPlayerData pd = GOTLevelData.getData(player);
                pd.addOrUpdateSharedCustomWaypoint(shareCopy);
                pd.checkCustomWaypointsSharedBy(ImmutableList.of(this.playerUUID));
            }
        }
    }

    public void customWaypointRemoveSharedFellowshipClient(GOTCustomWaypoint waypoint, UUID fsID) {
        waypoint.removeSharedFellowship(fsID);
    }

    public void disbandFellowship(GOTFellowship fs, String disbanderUsername) {
        if (fs.isOwner(this.playerUUID)) {
            List<UUID> memberUUIDs = new ArrayList<>(fs.getMemberUUIDs());
            fs.setDisbandedAndRemoveAllMembers();
            this.removeFellowship(fs);
            for (UUID memberID : memberUUIDs) {
                EntityPlayer member = this.getOtherPlayer(memberID);
                if (member != null && !member.worldObj.isRemote) {
                    fs.sendNotification(member, "got.gui.fellowships.notifyDisband", disbanderUsername);
                }
            }
        }
    }

    public void distributeMQEvent(GOTMiniQuestEvent event) {
        for (GOTMiniQuest quest : this.miniQuests) {
            if (quest.isActive()) {
                quest.handleEvent(event);
            }
        }
    }

    public boolean doesFactionPreventPledge(GOTFaction pledgeFac, GOTFaction otherFac) {
        return pledgeFac.isMortalEnemy(otherFac);
    }

    public <T extends EntityLiving> T fastTravelEntity(WorldServer world, T entity, int i, int j, int k) {
        String entityID = EntityList.getEntityString(entity);
        NBTTagCompound nbt = new NBTTagCompound();
        entity.writeToNBT(nbt);
        entity.setDead();
        EntityLiving entityLiving = (EntityLiving) EntityList.createEntityByName(entityID, world);
        entityLiving.readFromNBT(nbt);
        entityLiving.setLocationAndAngles(i + 0.5D, j, k + 0.5D, entityLiving.rotationYaw, entityLiving.rotationPitch);
        entityLiving.fallDistance = 0.0F;
        entityLiving.getNavigator().clearPathEntity();
        entityLiving.setAttackTarget(null);
        world.spawnEntityInWorld(entityLiving);
        world.updateEntityWithOptionalForce(entityLiving, false);
        return (T) entityLiving;
    }

    public void fastTravelTo(GOTAbstractWaypoint waypoint) {
        EntityPlayer player = this.getPlayer();
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP entityplayer = (EntityPlayerMP) player;
            WorldServer world = (WorldServer) entityplayer.worldObj;
            int startX = MathHelper.floor_double(entityplayer.posX);
            int startZ = MathHelper.floor_double(entityplayer.posZ);
            double range = 256.0D;
            List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class, entityplayer.boundingBox.expand(range, range, range));
            Set<EntityLiving> entitiesToTransport = new HashSet<>();
            for (EntityLiving entity : entities) {
                if (!(entity instanceof GOTEntityDragon)) {
                    if (entity instanceof GOTEntityNPC) {
                        GOTEntityNPC npc = (GOTEntityNPC) entity;
                        if (npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == entityplayer && npc.hiredNPCInfo.shouldFollowPlayer()) {
                            entitiesToTransport.add(npc);
                            continue;
                        }
                    }
                    if (entity instanceof EntityTameable) {
                        EntityTameable pet = (EntityTameable) entity;
                        if (pet.getOwner() == entityplayer && !pet.isSitting()) {
                            entitiesToTransport.add(pet);
                            continue;
                        }
                    }
                    if (entity.getLeashed() && entity.getLeashedToEntity() == entityplayer) {
                        entitiesToTransport.add(entity);
                    }
                }
            }
            Set<EntityLiving> removes = new HashSet<>();
            for (EntityLiving entity : entitiesToTransport) {
                Entity rider = entity.riddenByEntity;
                if (rider != null && entitiesToTransport.contains(rider)) {
                    removes.add(entity);
                }
            }
            entitiesToTransport.removeAll(removes);
            int i = waypoint.getXCoord();
            int k = waypoint.getZCoord();
            world.theChunkProviderServer.provideChunk(i >> 4, k >> 4);
            int j = waypoint.getYCoord(world, i, k);
            Entity playerMount = entityplayer.ridingEntity;
            entityplayer.mountEntity(null);
            entityplayer.setPositionAndUpdate(i + 0.5D, j, k + 0.5D);
            entityplayer.fallDistance = 0.0F;
            if (playerMount instanceof EntityLiving) {
                playerMount = this.fastTravelEntity(world, (EntityLiving) playerMount, i, j, k);
            }
            if (playerMount != null) {
                this.setUUIDToMount(playerMount.getUniqueID());
            }
            for (EntityLiving entity : entitiesToTransport) {
                Entity mount = entity.ridingEntity;
                entity.mountEntity(null);
                entity = this.fastTravelEntity(world, entity, i, j, k);
                if (mount instanceof EntityLiving) {
                    mount = this.fastTravelEntity(world, (EntityLiving) mount, i, j, k);
                    entity.mountEntity(mount);
                }
            }
            this.sendFTPacket(entityplayer, waypoint, startX, startZ);
            this.setTimeSinceFTWithUpdate(0);
            this.incrementWPUseCount(waypoint);
            if (waypoint instanceof GOTWaypoint) {
                this.lastWaypoint = (GOTWaypoint) waypoint;
                this.markDirty();
            }
            if (waypoint instanceof GOTCustomWaypoint) {
                GOTCustomWaypointLogger.logTravel(entityplayer, (GOTCustomWaypoint) waypoint);
            }
        }
    }

    public List<GOTAchievement> getAchievements() {
        return this.achievements;
    }

    public List<GOTMiniQuest> getActiveMiniQuests() {
        return this.selectMiniQuests(new MiniQuestSelector.OptionalActive().setActiveOnly());
    }

    public boolean getAdminHideMap() {
        return this.adminHideMap;
    }

    public void setAdminHideMap(boolean flag) {
        this.adminHideMap = flag;
        this.markDirty();
    }

    public int getAlcoholTolerance() {
        return this.alcoholTolerance;
    }

    public void setAlcoholTolerance(int i) {
        this.alcoholTolerance = i;
        this.markDirty();
        if (this.alcoholTolerance >= 250) {
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                this.addAchievement(GOTAchievement.gainHighAlcoholTolerance);
            }
        }
    }

    public float getAlignment(GOTFaction faction) {
        if (faction.hasFixedAlignment)
            return faction.fixedAlignment;
        Float alignment = this.alignments.get(faction);
        if (alignment != null)
            return alignment;
        return 0.0F;
    }

    public List<GOTAbstractWaypoint> getAllAvailableWaypoints() {
        List<GOTAbstractWaypoint> waypoints = new ArrayList<>(GOTWaypoint.listAllWaypoints());
        waypoints.addAll(this.customWaypoints);
        waypoints.addAll(this.customWaypointsShared);
        return waypoints;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int b) {
        this.balance = b;
        this.markDirty();
    }

    public GOTFaction getBrokenPledgeFaction() {
        return this.brokenPledgeFaction;
    }

    public void setBrokenPledgeFaction(GOTFaction f) {
        this.brokenPledgeFaction = f;
        this.markDirty();
    }

    public GOTCapes getCape() {
        return this.cape;
    }

    public void setCape(GOTCapes cape) {
        this.cape = cape;
        this.markDirty();
    }

    public GOTFellowship getChatBoundFellowship() {
        if (this.chatBoundFellowshipID != null)
            return GOTFellowshipData.getActiveFellowship(this.chatBoundFellowshipID);
        return null;
    }

    public void setChatBoundFellowship(GOTFellowship fs) {
        this.setChatBoundFellowshipID(fs.getFellowshipID());
    }

    public GOTFellowshipClient getClientFellowshipByID(UUID fsID) {
        for (GOTFellowshipClient fs : this.fellowshipsClient) {
            if (fs.getFellowshipID().equals(fsID))
                return fs;
        }
        return null;
    }

    public GOTFellowshipClient getClientFellowshipByName(String fsName) {
        for (GOTFellowshipClient fs : this.fellowshipsClient) {
            if (fs.getName().equalsIgnoreCase(fsName))
                return fs;
        }
        return null;
    }

    public List<GOTFellowshipClient> getClientFellowshipInvites() {
        return this.fellowshipInvitesClient;
    }

    public List<GOTFellowshipClient> getClientFellowships() {
        return this.fellowshipsClient;
    }

    public int getCompletedBountyQuests() {
        return this.completedBountyQuests;
    }

    public int getCompletedMiniQuestsTotal() {
        return this.completedMiniquestCount;
    }

    public long getCurrentOnlineTime() {
        return MinecraftServer.getServer().worldServerForDimension(0).getTotalWorldTime();
    }

    public GOTCustomWaypoint getCustomWaypointByID(int ID) {
        for (GOTCustomWaypoint waypoint : this.customWaypoints) {
            if (waypoint.getID() == ID)
                return waypoint;
        }
        return null;
    }

    public List<GOTCustomWaypoint> getCustomWaypoints() {
        return this.customWaypoints;
    }

    public int getDeathDimension() {
        return this.deathDim;
    }

    public void setDeathDimension(int dim) {
        this.deathDim = dim;
        this.markDirty();
    }

    public ChunkCoordinates getDeathPoint() {
        return this.deathPoint;
    }

    public List<GOTAchievement> getEarnedAchievements(GOTDimension dimension) {
        List<GOTAchievement> earnedAchievements = new ArrayList<>();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null) {
            for (GOTAchievement achievement : this.achievements) {
                if (achievement.getDimension() == dimension && achievement.canPlayerEarn(entityplayer)) {
                    earnedAchievements.add(achievement);
                }
            }
        }
        return earnedAchievements;
    }

    public boolean getEnableConquestKills() {
        return this.conquestKills;
    }

    public void setEnableConquestKills(boolean flag) {
        this.conquestKills = flag;
        this.markDirty();
        this.sendOptionsPacket(5, flag);
    }

    public boolean getEnableHiredDeathMessages() {
        return this.hiredDeathMessages;
    }

    public void setEnableHiredDeathMessages(boolean flag) {
        this.hiredDeathMessages = flag;
        this.markDirty();
        this.sendOptionsPacket(1, flag);
    }

    public GOTFactionData getFactionData(GOTFaction faction) {
        return this.factionDataMap.computeIfAbsent(faction, k -> new GOTFactionData(this, faction));
    }

    public GOTFellowship getFellowshipByName(String fsName) {
        for (UUID fsID : this.fellowshipIDs) {
            GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
            if (fs != null && fs.getName().equalsIgnoreCase(fsName))
                return fs;
        }
        return null;
    }

    public List<UUID> getFellowshipIDs() {
        return this.fellowshipIDs;
    }

    public List<GOTFellowship> getFellowships() {
        List<GOTFellowship> fellowships = new ArrayList<>();
        for (UUID fsID : this.fellowshipIDs) {
            GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
            if (fs != null) {
                fellowships.add(fs);
            }
        }
        return fellowships;
    }

    public boolean getFemRankOverride() {
        return this.femRankOverride;
    }

    public void setFemRankOverride(boolean flag) {
        this.femRankOverride = flag;
        this.markDirty();
        this.sendOptionsPacket(4, flag);
    }

    public boolean getFriendlyFire() {
        return this.friendlyFire;
    }

    public void setFriendlyFire(boolean flag) {
        this.friendlyFire = flag;
        this.markDirty();
        this.sendOptionsPacket(0, flag);
    }

    public boolean getHideAlignment() {
        return this.hideAlignment;
    }

    public void setHideAlignment(boolean flag) {
        this.hideAlignment = flag;
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTLevelData.sendAlignmentToAllPlayersInWorld(entityplayer, entityplayer.worldObj);
        }
    }

    public boolean getHideMapLocation() {
        return this.hideOnMap;
    }

    public void setHideMapLocation(boolean flag) {
        this.hideOnMap = flag;
        this.markDirty();
        this.sendOptionsPacket(3, flag);
    }

    public GOTBiome getLastKnownBiome() {
        return this.lastBiome;
    }

    public GOTWaypoint getLastKnownWaypoint() {
        return this.lastWaypoint;
    }

    public int getMaxCustomWaypoints() {
        int achievements = this.getEarnedAchievements(GOTDimension.GAME_OF_THRONES).size();
        return 5 + achievements / 5;
    }

    public int getMaxLeadingFellowships() {
        int achievements = this.getEarnedAchievements(GOTDimension.GAME_OF_THRONES).size();
        return 1 + achievements / 20;
    }

    public GOTMiniQuest getMiniQuestForID(UUID id, boolean completed) {
        List<GOTMiniQuest> threadSafe;
        if (completed) {
            threadSafe = new ArrayList<>(this.miniQuestsCompleted);
        } else {
            threadSafe = new ArrayList<>(this.miniQuests);
        }
        for (GOTMiniQuest quest : threadSafe) {
            if (quest.questUUID.equals(id))
                return quest;
        }
        return null;
    }

    public List<GOTMiniQuest> getMiniQuests() {
        return this.miniQuests;
    }

    public List<GOTMiniQuest> getMiniQuestsCompleted() {
        return this.miniQuestsCompleted;
    }

    public List<GOTMiniQuest> getMiniQuestsForEntity(Entity npc, boolean activeOnly) {
        return this.getMiniQuestsForEntityID(npc.getUniqueID(), activeOnly);
    }

    public List<GOTMiniQuest> getMiniQuestsForEntityID(UUID npcID, boolean activeOnly) {
        MiniQuestSelector.EntityId sel = new MiniQuestSelector.EntityId(npcID);
        if (activeOnly) {
            sel.setActiveOnly();
        }
        return this.selectMiniQuests(sel);
    }

    public List<GOTMiniQuest> getMiniQuestsForFaction(GOTFaction f, boolean activeOnly) {
        MiniQuestSelector.Faction sel = new MiniQuestSelector.Faction(() -> f);
        if (activeOnly) {
            sel.setActiveOnly();
        }
        return this.selectMiniQuests(sel);
    }

    public int getNextCwpID() {
        return this.nextCwpID;
    }

    public EntityPlayer getOtherPlayer(UUID uuid) {
        for (WorldServer worldServer : MinecraftServer.getServer().worldServers) {
            EntityPlayer entityplayer = worldServer.func_152378_a(uuid);
            if (entityplayer != null)
                return entityplayer;
        }
        return null;
    }

    public EntityPlayer getPlayer() {
        World[] searchWorlds;
        if (GOT.proxy.isClient()) {
            searchWorlds = new World[]{GOT.proxy.getClientWorld()};
        } else {
            searchWorlds = MinecraftServer.getServer().worldServers;
        }
        for (World world : searchWorlds) {
            EntityPlayer entityplayer = world.func_152378_a(this.playerUUID);
            if (entityplayer != null)
                return entityplayer;
        }
        return null;
    }

    public GOTTitle.PlayerTitle getPlayerTitle() {
        return this.playerTitle;
    }

    public void setPlayerTitle(GOTTitle.PlayerTitle title) {
        this.playerTitle = title;
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketTitle packet = new GOTPacketTitle(this.playerTitle);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
        for (UUID fsID : this.fellowshipIDs) {
            GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
            if (fs != null) {
                fs.updateForAllMembers(new FellowshipUpdateType.UpdatePlayerTitle(this.playerUUID, this.playerTitle));
            }
        }
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public int getPledgeBreakCooldown() {
        return this.pledgeBreakCooldown;
    }

    public void setPledgeBreakCooldown(int i) {
        int preCD = this.pledgeBreakCooldown;
        GOTFaction preBroken = this.brokenPledgeFaction;
        i = Math.max(0, i);
        this.pledgeBreakCooldown = i;
        boolean bigChange = (this.pledgeBreakCooldown == 0 || preCD == 0) && this.pledgeBreakCooldown != preCD;
        if (this.pledgeBreakCooldown > this.pledgeBreakCooldownStart) {
            this.setPledgeBreakCooldownStart(this.pledgeBreakCooldown);
            bigChange = true;
        }
        if (this.pledgeBreakCooldown <= 0 && preBroken != null) {
            this.setPledgeBreakCooldownStart(0);
            this.setBrokenPledgeFaction(null);
            bigChange = true;
        }
        if (bigChange || isTimerAutosaveTick()) {
            this.markDirty();
        }
        if (bigChange || this.pledgeBreakCooldown % 5 == 0) {
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketBrokenPledge packet = new GOTPacketBrokenPledge(this.pledgeBreakCooldown, this.pledgeBreakCooldownStart, this.brokenPledgeFaction);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        }
        if (this.pledgeBreakCooldown == 0 && preCD != this.pledgeBreakCooldown) {
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                String brokenName;
                if (preBroken == null) {
                    brokenName = StatCollector.translateToLocal("got.gui.factions.pledgeUnknown");
                } else {
                    brokenName = preBroken.factionName();
                }
                ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("got.chat.pledgeBreakCooldown", brokenName);
                entityplayer.addChatMessage(chatComponentTranslation);
            }
        }
    }

    public int getPledgeBreakCooldownStart() {
        return this.pledgeBreakCooldownStart;
    }

    public void setPledgeBreakCooldownStart(int i) {
        this.pledgeBreakCooldownStart = i;
        this.markDirty();
    }

    public float getPledgeEnemyAlignmentLimit(GOTFaction fac) {
        return 0.0F;
    }

    public GOTFaction getPledgeFaction() {
        return this.pledgeFaction;
    }

    public void setPledgeFaction(GOTFaction fac) {
        this.pledgeFaction = fac;
        this.pledgeKillCooldown = 0;
        this.markDirty();
        if (fac != null) {
            this.checkAlignmentAchievements(fac, this.getAlignment(fac));
            this.addAchievement(GOTAchievement.pledgeService);
        }
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            if (fac != null) {
                World world = entityplayer.worldObj;
                world.playSoundAtEntity(entityplayer, "got:event.pledge", 1.0F, 1.0F);
            }
            GOTPacketPledge packet = new GOTPacketPledge(fac);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public GOTPlayerQuestData getQuestData() {
        return this.questData;
    }

    public GOTFaction getRegionLastViewedFaction(GOTDimension.DimensionRegion region) {
        return this.prevRegionFactions.computeIfAbsent(region, key -> {
            GOTFaction fac = region.factionList.get(0);
            this.prevRegionFactions.put(region, fac);
            return fac;
        });
    }

    public GOTCustomWaypoint getSharedCustomWaypointByID(UUID owner, int id) {
        for (GOTCustomWaypoint waypoint : this.customWaypointsShared) {
            if (waypoint.getSharingPlayerID().equals(owner) && waypoint.getID() == id)
                return waypoint;
        }
        return null;
    }

    public GOTShields getShield() {
        return this.shield;
    }

    public void setShield(GOTShields gotshield) {
        this.shield = gotshield;
        this.markDirty();
    }

    public boolean getStructuresBanned() {
        return this.structuresBanned;
    }

    public void setStructuresBanned(boolean flag) {
        this.structuresBanned = flag;
        this.markDirty();
    }

    public boolean getTableSwitched() {
        return this.tableSwitched;
    }

    public void setTableSwitched(boolean flag) {
        this.tableSwitched = flag;
        this.markDirty();
        this.sendOptionsPacket(9, flag);
    }

    public boolean getTeleportedKW() {
        return this.teleportedKW;
    }

    public void setTeleportedKW(boolean flag) {
        this.teleportedKW = flag;
        this.markDirty();
    }

    public int getTimeSinceFT() {
        return this.ftSinceTick;
    }

    public void setTimeSinceFT(int i) {
        this.setTimeSinceFT(i, false);
    }

    public int getDragonFireballTime() {
        return this.dragonFireballSinceTick;
    }

    public GOTMiniQuest getTrackingMiniQuest() {
        if (this.trackingMiniQuestID == null)
            return null;
        return this.getMiniQuestForID(this.trackingMiniQuestID, false);
    }

    public void setTrackingMiniQuest(GOTMiniQuest quest) {
        if (quest == null) {
            this.setTrackingMiniQuestID(null);
        } else {
            this.setTrackingMiniQuestID(quest.questUUID);
        }
    }

    public GOTFaction getViewingFaction() {
        return this.viewingFaction;
    }

    public void setViewingFaction(GOTFaction faction) {
        if (faction != null) {
            this.viewingFaction = faction;
            this.markDirty();
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketUpdateViewingFaction packet = new GOTPacketUpdateViewingFaction(this.viewingFaction);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        }
    }

    public int getWaypointFTTime(GOTAbstractWaypoint wp, Entity entityplayer) {
        int baseMin = GOTLevelData.getWaypointCooldownMin();
        int baseMax = GOTLevelData.getWaypointCooldownMax();
        int useCount = this.getWPUseCount(wp);
        double dist = entityplayer.getDistance(wp.getXCoord() + 0.5D, wp.getYCoordSaved(), wp.getZCoord() + 0.5D);
        double time = baseMin;
        double added = (baseMax - baseMin) * Math.pow(0.9D, useCount);
        time += added;
        time *= Math.max(1.0D, dist * 1.2E-5D);
        int seconds = (int) Math.round(time);
        seconds = Math.max(seconds, 0);
        return seconds * 20;
    }

    public int getWPUseCount(GOTAbstractWaypoint wp) {
        if (wp instanceof GOTCustomWaypoint) {
            GOTCustomWaypoint cwp = (GOTCustomWaypoint) wp;
            int ID = cwp.getID();
            if (cwp.isShared()) {
                UUID sharingPlayer = cwp.getSharingPlayerID();
                CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
                if (this.cwpSharedUseCounts.containsKey(key))
                    return this.cwpSharedUseCounts.get(key);
            } else if (this.cwpUseCounts.containsKey(ID))
                return this.cwpUseCounts.get(ID);
        } else if (this.wpUseCounts.containsKey(wp))
            return this.wpUseCounts.get(wp);
        return 0;
    }

    public void givePureConquestBonus(EntityPlayer entityplayer, GOTFaction bonusFac, GOTFaction enemyFac, float conq, String title, double posX, double posY, double posZ) {
        conq = GOTConquestGrid.onConquestKill(entityplayer, bonusFac, enemyFac, conq);
        this.getFactionData(bonusFac).addConquest(Math.abs(conq));
        if (conq != 0.0F) {
            GOTAlignmentValues.AlignmentBonus source = new GOTAlignmentValues.AlignmentBonus(0.0F, title);
            GOTPacketAlignmentBonus packet = new GOTPacketAlignmentBonus(bonusFac, this.getAlignment(bonusFac), new GOTAlignmentBonusMap(), conq, posX, posY, posZ, source);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public boolean hasAchievement(GOTAchievement achievement) {
        for (GOTAchievement a : this.achievements) {
            if (a.category == achievement.category && a.ID == achievement.ID)
                return true;
        }
        return false;
    }

    public boolean hasActiveOrCompleteMQType(Class<? extends GOTMiniQuest> type) {
        List<GOTMiniQuest> quests = this.miniQuests;
        List<GOTMiniQuest> questsComplete = this.miniQuestsCompleted;
        List<GOTMiniQuest> allQuests = new ArrayList<>();
        for (GOTMiniQuest q : quests) {
            if (q.isActive()) {
                allQuests.add(q);
            }
        }
        allQuests.addAll(questsComplete);
        for (GOTMiniQuest q : allQuests) {
            if (type.isAssignableFrom(q.getClass()))
                return true;
        }
        return false;
    }

    public boolean hasAnyJHQuest() {
        return this.hasActiveOrCompleteMQType(GOTMiniQuestWelcome.class);
    }

    public boolean hasAnyWaypointsSharedToFellowship(GOTFellowship fs) {
        for (GOTCustomWaypoint waypoint : this.customWaypoints) {
            if (waypoint.hasSharedFellowship(fs))
                return true;
        }
        return false;
    }

    public boolean hasPledgeAlignment(GOTFaction fac) {
        float alignment = this.getAlignment(fac);
        return alignment >= fac.getPledgeAlignment();
    }

    public void hideOrUnhideSharedCustomWaypoint(GOTCustomWaypoint waypoint, boolean hide) {
        if (!waypoint.isShared()) {
            FMLLog.warning("Hummel009: Warning! Tried to unlock a shared custom waypoint with no owner!");
            return;
        }
        waypoint.setSharedHidden(hide);
        CWPSharedKey key = CWPSharedKey.keyFor(waypoint.getSharingPlayerID(), waypoint.getID());
        if (hide) {
            this.cwpSharedHidden.add(key);
        } else {
            this.cwpSharedHidden.remove(key);
        }
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketCWPSharedHideClient packet = waypoint.getClientSharedHidePacket(hide);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void incrementNextCwpID() {
        this.nextCwpID++;
        this.markDirty();
    }

    public void incrementWPUseCount(GOTAbstractWaypoint wp) {
        this.setWPUseCount(wp, this.getWPUseCount(wp) + 1);
    }

    public void invitePlayerToFellowship(GOTFellowship fs, UUID invitedPlayerUUID, String inviterUsername) {
        if (fs.isOwner(this.playerUUID) || fs.isAdmin(this.playerUUID)) {
            GOTLevelData.getData(invitedPlayerUUID).addFellowshipInvite(fs, this.playerUUID, inviterUsername);
        }
    }

    public boolean isFTRegionUnlocked(GOTWaypoint.Region region) {
        return this.unlockedFTRegions.contains(region);
    }

    public boolean isPledgedTo(GOTFaction fac) {
        return this.pledgeFaction == fac;
    }

    public boolean isPledgeEnemyAlignmentLimited(GOTFaction fac) {
        return this.pledgeFaction != null && this.doesFactionPreventPledge(this.pledgeFaction, fac);
    }

    public boolean isSiegeActive() {
        return this.siegeActiveTime > 0;
    }

    public void setSiegeActive(int duration) {
        this.siegeActiveTime = Math.max(this.siegeActiveTime, duration);
    }

    public void leaveFellowship(GOTFellowship fs) {
        if (!fs.isOwner(this.playerUUID)) {
            fs.removeMember(this.playerUUID);
            if (this.fellowshipIDs.contains(fs.getFellowshipID())) {
                this.removeFellowship(fs);
            }
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                EntityPlayer owner = this.getOtherPlayer(fs.getOwner());
                if (owner != null) {
                    fs.sendNotification(owner, "got.gui.fellowships.notifyLeave", entityplayer.getCommandSenderName());
                }
            }
        }
    }

    public List<String> listAllFellowshipNames() {
        List<String> list = new ArrayList<>();
        for (UUID fsID : this.fellowshipIDs) {
            GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
            if (fs != null && fs.containsPlayer(this.playerUUID)) {
                list.add(fs.getName());
            }
        }
        return list;
    }

    public List<String> listAllLeadingFellowshipNames() {
        List<String> list = new ArrayList<>();
        for (UUID fsID : this.fellowshipIDs) {
            GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
            if (fs != null && fs.isOwner(this.playerUUID)) {
                list.add(fs.getName());
            }
        }
        return list;
    }

    public void load(NBTTagCompound playerData) {
        this.alignments.clear();
        NBTTagList alignmentTags = playerData.getTagList("AlignmentMap", 10);
        for (int i = 0; i < alignmentTags.tagCount(); i++) {
            NBTTagCompound nbt = alignmentTags.getCompoundTagAt(i);
            GOTFaction faction = GOTFaction.forName(nbt.getString("Faction"));
            if (faction != null) {
                float alignment;
                if (nbt.hasKey("Alignment")) {
                    alignment = nbt.getInteger("Alignment");
                } else {
                    alignment = nbt.getFloat("AlignF");
                }
                this.alignments.put(faction, alignment);
            }
        }
        this.factionDataMap.clear();
        NBTTagList factionDataTags = playerData.getTagList("FactionData", 10);
        for (int j = 0; j < factionDataTags.tagCount(); j++) {
            NBTTagCompound nbt = factionDataTags.getCompoundTagAt(j);
            GOTFaction faction = GOTFaction.forName(nbt.getString("Faction"));
            if (faction != null) {
                GOTFactionData data = new GOTFactionData(this, faction);
                data.load(nbt);
                this.factionDataMap.put(faction, data);
            }
        }
        if (playerData.hasKey("CurrentFaction")) {
            GOTFaction cur = GOTFaction.forName(playerData.getString("CurrentFaction"));
            if (cur != null) {
                this.viewingFaction = cur;
            }
        }
        this.prevRegionFactions.clear();
        NBTTagList prevRegionFactionTags = playerData.getTagList("PrevRegionFactions", 10);
        for (int k = 0; k < prevRegionFactionTags.tagCount(); k++) {
            NBTTagCompound nbt = prevRegionFactionTags.getCompoundTagAt(k);
            GOTDimension.DimensionRegion region = GOTDimension.DimensionRegion.forName(nbt.getString("Region"));
            GOTFaction faction = GOTFaction.forName(nbt.getString("Faction"));
            if (region != null && faction != null) {
                this.prevRegionFactions.put(region, faction);
            }
        }
        this.hideAlignment = playerData.getBoolean("HideAlignment");
        this.takenAlignmentRewards.clear();
        NBTTagList takenRewardsTags = playerData.getTagList("TakenAlignmentRewards", 10);
        for (int m = 0; m < takenRewardsTags.tagCount(); m++) {
            NBTTagCompound nbt = takenRewardsTags.getCompoundTagAt(m);
            GOTFaction faction = GOTFaction.forName(nbt.getString("Faction"));
            if (faction != null) {
                this.takenAlignmentRewards.add(faction);
            }
        }
        this.pledgeFaction = null;
        if (playerData.hasKey("PledgeFac")) {
            this.pledgeFaction = GOTFaction.forName(playerData.getString("PledgeFac"));
        }
        this.pledgeKillCooldown = playerData.getInteger("PledgeKillCD");
        this.pledgeBreakCooldown = playerData.getInteger("PledgeBreakCD");
        this.pledgeBreakCooldownStart = playerData.getInteger("PledgeBreakCDStart");
        this.brokenPledgeFaction = null;
        if (playerData.hasKey("BrokenPledgeFac")) {
            this.brokenPledgeFaction = GOTFaction.forName(playerData.getString("BrokenPledgeFac"));
        }
        this.hideOnMap = playerData.getBoolean("HideOnMap");
        this.adminHideMap = playerData.getBoolean("AdminHideMap");
        if (playerData.hasKey("ShowWP")) {
            this.showWaypoints = playerData.getBoolean("ShowWP");
        }
        if (playerData.hasKey("ShowCWP")) {
            this.showCustomWaypoints = playerData.getBoolean("ShowCWP");
        }
        if (playerData.hasKey("ShowHiddenSWP")) {
            this.showHiddenSharedWaypoints = playerData.getBoolean("ShowHiddenSWP");
        }
        if (playerData.hasKey("ConquestKills")) {
            this.conquestKills = playerData.getBoolean("ConquestKills");
        }
        this.achievements.clear();
        NBTTagList achievementTags = playerData.getTagList("Achievements", 10);
        for (int n = 0; n < achievementTags.tagCount(); n++) {
            NBTTagCompound nbt = achievementTags.getCompoundTagAt(n);
            String category = nbt.getString("Category");
            int ID = nbt.getInteger("ID");
            GOTAchievement achievement = GOTAchievement.achievementForCategoryAndID(GOTAchievement.categoryForName(category), ID);
            if (achievement != null && !this.achievements.contains(achievement)) {
                this.achievements.add(achievement);
            }
        }
        this.shield = null;
        if (playerData.hasKey("Shield")) {
            GOTShields savedShield = GOTShields.shieldForName(playerData.getString("Shield"));
            if (savedShield != null) {
                this.shield = savedShield;
            }
        }
        if (playerData.hasKey("FriendlyFire")) {
            this.friendlyFire = playerData.getBoolean("FriendlyFire");
        }
        if (playerData.hasKey("HiredDeathMessages")) {
            this.hiredDeathMessages = playerData.getBoolean("HiredDeathMessages");
        }
        this.deathPoint = null;
        if (playerData.hasKey("DeathX") && playerData.hasKey("DeathY") && playerData.hasKey("DeathZ")) {
            this.deathPoint = new ChunkCoordinates(playerData.getInteger("DeathX"), playerData.getInteger("DeathY"), playerData.getInteger("DeathZ"));
            if (playerData.hasKey("DeathDim")) {
                this.deathDim = playerData.getInteger("DeathDim");
            } else {
                this.deathDim = GOTDimension.GAME_OF_THRONES.dimensionID;
            }
        }
        this.alcoholTolerance = playerData.getInteger("Alcohol");
        this.miniQuests.clear();
        NBTTagList miniquestTags = playerData.getTagList("MiniQuests", 10);
        for (int i1 = 0; i1 < miniquestTags.tagCount(); i1++) {
            NBTTagCompound nbt = miniquestTags.getCompoundTagAt(i1);
            GOTMiniQuest quest = GOTMiniQuest.loadQuestFromNBT(nbt, this);
            if (quest != null) {
                this.miniQuests.add(quest);
            }
        }
        this.miniQuestsCompleted.clear();
        NBTTagList miniquestCompletedTags = playerData.getTagList("MiniQuestsCompleted", 10);
        for (int i2 = 0; i2 < miniquestCompletedTags.tagCount(); i2++) {
            NBTTagCompound nbt = miniquestCompletedTags.getCompoundTagAt(i2);
            GOTMiniQuest quest = GOTMiniQuest.loadQuestFromNBT(nbt, this);
            if (quest != null) {
                this.miniQuestsCompleted.add(quest);
            }
        }
        this.completedMiniquestCount = playerData.getInteger("MQCompleteCount");
        this.completedBountyQuests = playerData.getInteger("MQCompletedBounties");
        this.trackingMiniQuestID = null;
        if (playerData.hasKey("MiniQuestTrack")) {
            String s = playerData.getString("MiniQuestTrack");
            this.trackingMiniQuestID = UUID.fromString(s);
        }
        this.bountiesPlaced.clear();
        NBTTagList bountyTags = playerData.getTagList("BountiesPlaced", 8);
        for (int i3 = 0; i3 < bountyTags.tagCount(); i3++) {
            String fName = bountyTags.getStringTagAt(i3);
            GOTFaction fac = GOTFaction.forName(fName);
            if (fac != null) {
                this.bountiesPlaced.add(fac);
            }
        }
        this.lastWaypoint = null;
        if (playerData.hasKey("LastWP")) {
            String lastWPName = playerData.getString("LastWP");
            this.lastWaypoint = GOTWaypoint.waypointForName(lastWPName);
        }
        this.lastBiome = null;
        if (playerData.hasKey("LastBiome")) {
            short lastBiomeID = playerData.getShort("LastBiome");
            GOTBiome[] biomeList = GOTDimension.GAME_OF_THRONES.biomeList;
            if (lastBiomeID >= 0 && lastBiomeID < biomeList.length) {
                this.lastBiome = biomeList[lastBiomeID];
            }
        }
        this.sentMessageTypes.clear();
        NBTTagList sentMessageTags = playerData.getTagList("SentMessageTypes", 10);
        for (int i4 = 0; i4 < sentMessageTags.tagCount(); i4++) {
            NBTTagCompound nbt = sentMessageTags.getCompoundTagAt(i4);
            GOTGuiMessageTypes message = GOTGuiMessageTypes.forSaveName(nbt.getString("Message"));
            if (message != null) {
                boolean sent = nbt.getBoolean("Sent");
                this.sentMessageTypes.put(message, sent);
            }
        }
        this.playerTitle = null;
        if (playerData.hasKey("PlayerTitle")) {
            GOTTitle title = GOTTitle.forName(playerData.getString("PlayerTitle"));
            if (title != null) {
                int colorCode = playerData.getInteger("PlayerTitleColor");
                EnumChatFormatting color = GOTTitle.PlayerTitle.colorForID(colorCode);
                this.playerTitle = new GOTTitle.PlayerTitle(title, color);
            }
        }
        if (playerData.hasKey("FemRankOverride")) {
            this.femRankOverride = playerData.getBoolean("FemRankOverride");
        }
        if (playerData.hasKey("FTSince")) {
            this.ftSinceTick = playerData.getInteger("FTSince");
        }
        if (playerData.hasKey("FireballCooldown")) {
            this.dragonFireballSinceTick = playerData.getInteger("FireballCooldown");
        }
        this.targetFTWaypoint = null;
        this.uuidToMount = null;
        if (playerData.hasKey("MountUUID")) {
            this.uuidToMount = UUID.fromString(playerData.getString("MountUUID"));
        }
        this.uuidToMountTime = playerData.getInteger("MountUUIDTime");
        if (playerData.hasKey("LastOnlineTime")) {
            this.lastOnlineTime = playerData.getLong("LastOnlineTime");
        }
        this.unlockedFTRegions.clear();
        NBTTagList unlockedFTRegionTags = playerData.getTagList("UnlockedFTRegions", 10);
        for (int i5 = 0; i5 < unlockedFTRegionTags.tagCount(); i5++) {
            NBTTagCompound nbt = unlockedFTRegionTags.getCompoundTagAt(i5);
            String regionName = nbt.getString("Name");
            GOTWaypoint.Region region = GOTWaypoint.regionForName(regionName);
            if (region != null) {
                this.unlockedFTRegions.add(region);
            }
        }
        this.customWaypoints.clear();
        NBTTagList customWaypointTags = playerData.getTagList("CustomWaypoints", 10);
        for (int i6 = 0; i6 < customWaypointTags.tagCount(); i6++) {
            NBTTagCompound nbt = customWaypointTags.getCompoundTagAt(i6);
            GOTCustomWaypoint waypoint = GOTCustomWaypoint.readFromNBT(nbt, this);
            this.customWaypoints.add(waypoint);
        }
        this.cwpSharedUnlocked.clear();
        NBTTagList cwpSharedUnlockedTags = playerData.getTagList("CWPSharedUnlocked", 10);
        for (int i7 = 0; i7 < cwpSharedUnlockedTags.tagCount(); i7++) {
            NBTTagCompound nbt = cwpSharedUnlockedTags.getCompoundTagAt(i7);
            UUID sharingPlayer = UUID.fromString(nbt.getString("SharingPlayer"));
            int ID = nbt.getInteger("CustomID");
            CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
            this.cwpSharedUnlocked.add(key);
        }
        this.cwpSharedHidden.clear();
        NBTTagList cwpSharedHiddenTags = playerData.getTagList("CWPSharedHidden", 10);
        for (int i8 = 0; i8 < cwpSharedHiddenTags.tagCount(); i8++) {
            NBTTagCompound nbt = cwpSharedHiddenTags.getCompoundTagAt(i8);
            UUID sharingPlayer = UUID.fromString(nbt.getString("SharingPlayer"));
            int ID = nbt.getInteger("CustomID");
            CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
            this.cwpSharedHidden.add(key);
        }
        this.wpUseCounts.clear();
        NBTTagList wpCooldownTags = playerData.getTagList("WPUses", 10);
        for (int i9 = 0; i9 < wpCooldownTags.tagCount(); i9++) {
            NBTTagCompound nbt = wpCooldownTags.getCompoundTagAt(i9);
            String name = nbt.getString("WPName");
            int count = nbt.getInteger("Count");
            GOTWaypoint wp = GOTWaypoint.waypointForName(name);
            if (wp != null) {
                this.wpUseCounts.put(wp, count);
            }
        }
        this.cwpUseCounts.clear();
        NBTTagList cwpCooldownTags = playerData.getTagList("CWPUses", 10);
        for (int i10 = 0; i10 < cwpCooldownTags.tagCount(); i10++) {
            NBTTagCompound nbt = cwpCooldownTags.getCompoundTagAt(i10);
            int ID = nbt.getInteger("CustomID");
            int count = nbt.getInteger("Count");
            this.cwpUseCounts.put(ID, count);
        }
        this.cwpSharedUseCounts.clear();
        NBTTagList cwpSharedCooldownTags = playerData.getTagList("CWPSharedUses", 10);
        for (int i11 = 0; i11 < cwpSharedCooldownTags.tagCount(); i11++) {
            NBTTagCompound nbt = cwpSharedCooldownTags.getCompoundTagAt(i11);
            UUID sharingPlayer = UUID.fromString(nbt.getString("SharingPlayer"));
            int ID = nbt.getInteger("CustomID");
            CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
            int count = nbt.getInteger("Count");
            this.cwpSharedUseCounts.put(key, count);
        }
        this.nextCwpID = 20000;
        if (playerData.hasKey("NextCWPID")) {
            this.nextCwpID = playerData.getInteger("NextCWPID");
        }
        this.fellowshipIDs.clear();
        NBTTagList fellowshipTags = playerData.getTagList("Fellowships", 10);
        for (int i12 = 0; i12 < fellowshipTags.tagCount(); i12++) {
            NBTTagCompound nbt = fellowshipTags.getCompoundTagAt(i12);
            UUID fsID = UUID.fromString(nbt.getString("ID"));
            this.fellowshipIDs.add(fsID);
        }
        this.fellowshipInvites.clear();
        NBTTagList fellowshipInviteTags = playerData.getTagList("FellowshipInvites", 10);
        for (int i13 = 0; i13 < fellowshipInviteTags.tagCount(); i13++) {
            NBTTagCompound nbt = fellowshipInviteTags.getCompoundTagAt(i13);
            UUID fsID = UUID.fromString(nbt.getString("ID"));
            UUID inviterID = null;
            if (nbt.hasKey("InviterID")) {
                inviterID = UUID.fromString(nbt.getString("InviterID"));
            }
            this.fellowshipInvites.add(new GOTFellowshipInvite(fsID, inviterID));
        }
        this.chatBoundFellowshipID = null;
        if (playerData.hasKey("ChatBoundFellowship")) {
            this.chatBoundFellowshipID = UUID.fromString(playerData.getString("ChatBoundFellowship"));
        }
        this.structuresBanned = playerData.getBoolean("StructuresBanned");
        this.teleportedKW = playerData.getBoolean("TeleportedKW");
        if (playerData.hasKey("QuestData")) {
            NBTTagCompound questNBT = playerData.getCompoundTag("QuestData");
            this.questData.load(questNBT);
        }

        this.askedForJaqen = playerData.getBoolean("AskedForJaqen");
        this.balance = playerData.getInteger("Balance");
        this.cape = null;
        if (playerData.hasKey("Cape")) {
            GOTCapes savedCape = GOTCapes.capeForName(playerData.getString("Cape"));
            if (savedCape != null) {
                this.cape = savedCape;
            }
        }
        this.checkedMenu = playerData.getBoolean("CheckedMenu");
        this.tableSwitched = playerData.getBoolean("TableSwitched");
    }

    public void lockFTRegion(GOTWaypoint.Region region) {
        if (this.unlockedFTRegions.remove(region)) {
            this.markDirty();
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketWaypointRegion packet = new GOTPacketWaypointRegion(region, false);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        }
    }

    public void markDirty() {
        this.needsSave = true;
    }

    public boolean needsSave() {
        return this.needsSave;
    }

    public void onPledgeKill(EntityPlayer entityplayer) {
        this.pledgeKillCooldown += 24000;
        this.markDirty();
        if (this.pledgeKillCooldown > 24000) {
            this.revokePledgeFaction(entityplayer, false);
        } else if (this.pledgeFaction != null) {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("got.chat.pledgeKillWarn", this.pledgeFaction.factionName());
            entityplayer.addChatMessage(chatComponentTranslation);
        }
    }

    public void onUpdate(EntityPlayerMP entityplayer, WorldServer world) {
        this.pdTick++;
        GOTDimension.DimensionRegion currentRegion = this.viewingFaction.factionRegion;
        GOTDimension currentDim = GOTDimension.getCurrentDimensionWithFallback(world);
        if (currentRegion.getDimension() != currentDim) {
            currentRegion = currentDim.dimensionRegions.get(0);
            this.setViewingFaction(this.getRegionLastViewedFaction(currentRegion));
        }
        this.questData.onUpdate(entityplayer, world);
        if (!this.isSiegeActive()) {
            this.runAchievementChecks(entityplayer, world);
        }
        if (!this.checkedMenu) {
            GOTPacketMenuPrompt packet = new GOTPacketMenuPrompt(GOTPacketMenuPrompt.Type.MENU);
            GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
        }
        if (this.playerTitle != null && !this.playerTitle.getTitle().canPlayerUse(entityplayer)) {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("got.chat.loseTitle", this.playerTitle.getFullTitleComponent(entityplayer));
            entityplayer.addChatMessage(chatComponentTranslation);
            this.setPlayerTitle(null);
        }
        if (this.pledgeKillCooldown > 0) {
            this.pledgeKillCooldown--;
            if (this.pledgeKillCooldown == 0 || isTimerAutosaveTick()) {
                this.markDirty();
            }
        }
        if (this.pledgeBreakCooldown > 0) {
            this.setPledgeBreakCooldown(this.pledgeBreakCooldown - 1);
        }
        if (this.trackingMiniQuestID != null && this.getTrackingMiniQuest() == null) {
            this.setTrackingMiniQuest(null);
        }
        List<GOTMiniQuest> activeMiniquests = this.getActiveMiniQuests();
        for (GOTMiniQuest quest : activeMiniquests) {
            quest.onPlayerTick(entityplayer);
        }
        if (!this.bountiesPlaced.isEmpty()) {
            for (GOTFaction fac : this.bountiesPlaced) {
                ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("got.chat.bountyPlaced", fac.factionName());
                chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);
                entityplayer.addChatMessage(chatComponentTranslation);
            }
            this.bountiesPlaced.clear();
            this.markDirty();
        }
        this.setTimeSinceFT(this.ftSinceTick + 1);
        this.setDragonFireballCooldown(this.dragonFireballSinceTick + 1);
        if (this.targetFTWaypoint != null) {
            if (entityplayer.isPlayerSleeping()) {
                entityplayer.addChatMessage(new ChatComponentTranslation("got.fastTravel.inBed"));
                this.setTargetFTWaypoint(null);
            } else if (this.ticksUntilFT > 0) {
                int seconds = this.ticksUntilFT / 20;
                if (this.ticksUntilFT == ticksUntilFT_max) {
                    entityplayer.addChatMessage(new ChatComponentTranslation("got.fastTravel.travelTicksStart", seconds));
                } else if (this.ticksUntilFT % 20 == 0 && seconds <= 5) {
                    entityplayer.addChatMessage(new ChatComponentTranslation("got.fastTravel.travelTicks", seconds));
                }
                this.ticksUntilFT--;
                this.setTicksUntilFT(this.ticksUntilFT);
            } else {
                this.sendFTBouncePacket(entityplayer);
            }
        } else {
            this.setTicksUntilFT(0);
        }
        this.lastOnlineTime = this.getCurrentOnlineTime();
        if (this.uuidToMount != null) {
            if (this.uuidToMountTime > 0) {
                this.uuidToMountTime--;
            } else {
                double range = 32.0D;
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, entityplayer.boundingBox.expand(range, range, range));
                for (EntityLivingBase entity : entities) {
                    if (entity.getUniqueID().equals(this.uuidToMount)) {
                        entityplayer.mountEntity(entity);
                        break;
                    }
                }
                this.setUUIDToMount(null);
            }
        }
        if (this.pdTick % 24000 == 0 && this.alcoholTolerance > 0) {
            this.alcoholTolerance--;
            this.setAlcoholTolerance(this.alcoholTolerance);
        }
        this.unlockSharedCustomWaypoints(entityplayer);
        if (this.pdTick % 100 == 0 && world.provider instanceof GOTWorldProvider) {
            int i = MathHelper.floor_double(entityplayer.posX);
            int k = MathHelper.floor_double(entityplayer.posZ);
            GOTBiome biome = (GOTBiome) world.provider.getBiomeGenForCoords(i, k);
            if (biome.getBiomeDimension() == GOTDimension.GAME_OF_THRONES) {
                GOTBiome prevLastBiome = this.lastBiome;
                this.lastBiome = biome;
                if (prevLastBiome != biome) {
                    this.markDirty();
                }
            }
        }
        if (this.adminHideMap) {
            boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(entityplayer.getGameProfile());
            if (!entityplayer.capabilities.isCreativeMode || !isOp) {
                this.setAdminHideMap(false);
                GOTCommandAdminHideMap.notifyUnhidden(entityplayer);
            }
        }
        if (this.siegeActiveTime > 0) {
            this.siegeActiveTime--;
        }
    }

    public void placeBountyFor(GOTFaction f) {
        this.bountiesPlaced.add(f);
        this.markDirty();
    }

    public void receiveFTBouncePacket() {
        if (this.targetFTWaypoint != null && this.ticksUntilFT <= 0) {
            this.fastTravelTo(this.targetFTWaypoint);
            this.setTargetFTWaypoint(null);
        }
    }

    public void rejectFellowshipInvite(GOTFellowship fs) {
        UUID fsID = fs.getFellowshipID();
        GOTFellowshipInvite existingInvite = null;
        for (GOTFellowshipInvite invite : this.fellowshipInvites) {
            if (invite.fellowshipID.equals(fsID)) {
                existingInvite = invite;
                break;
            }
        }
        if (existingInvite != null) {
            this.fellowshipInvites.remove(existingInvite);
            this.markDirty();
            this.sendFellowshipInviteRemovePacket(fs);
        }
    }

    public void removeAchievement(GOTAchievement achievement) {
        if (!this.hasAchievement(achievement))
            return;
        if (this.achievements.remove(achievement)) {
            this.markDirty();
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                this.sendAchievementRemovePacket((EntityPlayerMP) entityplayer, achievement);
            }
        }
    }

    public void removeClientFellowship(UUID fsID) {
        GOTFellowshipClient inList = null;
        for (GOTFellowshipClient fsInList : this.fellowshipsClient) {
            if (fsInList.getFellowshipID().equals(fsID)) {
                inList = fsInList;
                break;
            }
        }
        if (inList != null) {
            this.fellowshipsClient.remove(inList);
        }
    }

    public void removeClientFellowshipInvite(UUID fsID) {
        GOTFellowshipClient inList = null;
        for (GOTFellowshipClient fsInList : this.fellowshipInvitesClient) {
            if (fsInList.getFellowshipID().equals(fsID)) {
                inList = fsInList;
                break;
            }
        }
        if (inList != null) {
            this.fellowshipInvitesClient.remove(inList);
        }
    }

    public void removeCustomWaypoint(GOTCustomWaypoint waypoint) {
        if (this.customWaypoints.remove(waypoint)) {
            this.markDirty();
            for (UUID fsID : waypoint.getSharedFellowshipIDs()) {
                GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
                if (fs != null) {
                    this.checkIfStillWaypointSharerForFellowship(fs);
                }
            }
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketDeleteCWPClient packet = waypoint.getClientDeletePacket();
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
                GOTCustomWaypointLogger.logDelete(entityplayer, waypoint);
            }
            GOTCustomWaypoint shareCopy = waypoint.createCopyOfShared(this.playerUUID);
            List<UUID> sharedPlayers = shareCopy.getPlayersInAllSharedFellowships();
            for (UUID sharedPlayerUUID : sharedPlayers) {
                EntityPlayer sharedPlayer = this.getOtherPlayer(sharedPlayerUUID);
                if (sharedPlayer != null && !sharedPlayer.worldObj.isRemote) {
                    GOTLevelData.getData(sharedPlayerUUID).removeSharedCustomWaypoint(shareCopy);
                }
            }
        }
    }

    public void removeCustomWaypointClient(GOTCustomWaypoint waypoint) {
        this.customWaypoints.remove(waypoint);
    }

    public void removeFellowship(GOTFellowship fs) {
        if (fs.isDisbanded() || !fs.containsPlayer(this.playerUUID)) {
            UUID fsID = fs.getFellowshipID();
            if (this.fellowshipIDs.contains(fsID)) {
                this.fellowshipIDs.remove(fsID);
                this.markDirty();
                this.sendFellowshipRemovePacket(fs);
                this.unshareFellowshipFromOwnCustomWaypoints(fs);
                this.checkCustomWaypointsSharedFromFellowships();
            }
        }
    }

    public void removeMiniQuest(GOTMiniQuest quest, boolean completed) {
        List<GOTMiniQuest> removeList;
        if (completed) {
            removeList = this.miniQuestsCompleted;
        } else {
            removeList = this.miniQuests;
        }
        if (removeList.remove(quest)) {
            this.markDirty();
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketMiniquestRemove packet = new GOTPacketMiniquestRemove(quest, quest.isCompleted(), false);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        } else {
            FMLLog.warning("Warning: Attempted to remove a miniquest which does not belong to the player data");
        }
    }

    public void removePlayerFromFellowship(GOTFellowship fs, UUID player, String removerUsername) {
        if (fs.isOwner(this.playerUUID) || fs.isAdmin(this.playerUUID)) {
            fs.removeMember(player);
            EntityPlayer removed = this.getOtherPlayer(player);
            if (removed != null && !removed.worldObj.isRemote) {
                fs.sendNotification(removed, "got.gui.fellowships.notifyRemove", removerUsername);
            }
        }
    }

    public void removeSharedCustomWaypoint(GOTCustomWaypoint waypoint) {
        if (!waypoint.isShared()) {
            FMLLog.warning("Hummel009: Warning! Tried to remove a shared custom waypoint with no owner!");
            return;
        }
        GOTCustomWaypoint existing;
        if (this.customWaypointsShared.contains(waypoint)) {
            existing = waypoint;
        } else {
            existing = this.getSharedCustomWaypointByID(waypoint.getSharingPlayerID(), waypoint.getID());
        }
        if (existing != null) {
            this.customWaypointsShared.remove(existing);
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketDeleteCWPClient packet = waypoint.getClientDeletePacketShared();
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        } else {
            FMLLog.warning("Hummel009: Warning! Tried to remove a shared custom waypoint that does not exist!");
        }
    }

    public void renameCustomWaypoint(GOTCustomWaypoint waypoint, String newName) {
        waypoint.rename(newName);
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketRenameCWPClient packet = waypoint.getClientRenamePacket();
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            GOTCustomWaypointLogger.logRename(entityplayer, waypoint);
        }
        GOTCustomWaypoint shareCopy = waypoint.createCopyOfShared(this.playerUUID);
        List<UUID> sharedPlayers = shareCopy.getPlayersInAllSharedFellowships();
        for (UUID sharedPlayerUUID : sharedPlayers) {
            EntityPlayer sharedPlayer = this.getOtherPlayer(sharedPlayerUUID);
            if (sharedPlayer != null && !sharedPlayer.worldObj.isRemote) {
                GOTLevelData.getData(sharedPlayerUUID).renameSharedCustomWaypoint(shareCopy, newName);
            }
        }
    }

    public void renameCustomWaypointClient(GOTCustomWaypoint waypoint, String newName) {
        waypoint.rename(newName);
    }

    public void renameFellowship(GOTFellowship fs, String name) {
        if (fs.isOwner(this.playerUUID)) {
            fs.setName(name);
        }
    }

    public void renameSharedCustomWaypoint(GOTCustomWaypoint waypoint, String newName) {
        if (!waypoint.isShared()) {
            FMLLog.warning("Hummel009: Warning! Tried to rename a shared custom waypoint with no owner!");
            return;
        }
        waypoint.rename(newName);
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketRenameCWPClient packet = waypoint.getClientRenamePacketShared();
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void revokePledgeFaction(EntityPlayer entityplayer, boolean intentional) {
        GOTFaction wasPledge = this.pledgeFaction;
        float pledgeLvl = wasPledge.getPledgeAlignment();
        float prevAlign = this.getAlignment(wasPledge);
        float diff = prevAlign - pledgeLvl;
        float cd = diff / 5000.0F;
        cd = MathHelper.clamp_float(cd, 0.0F, 1.0F);
        int cdTicks = 36000;
        cdTicks += Math.round(cd * 150.0F * 60.0F * 20.0F);
        this.setPledgeFaction(null);
        this.setBrokenPledgeFaction(wasPledge);
        this.setPledgeBreakCooldown(cdTicks);
        World world = entityplayer.worldObj;
        if (!world.isRemote) {
            GOTFactionRank rank = wasPledge.getRank(prevAlign);
            GOTFactionRank rankBelow = wasPledge.getRankBelow(rank);
            GOTFactionRank rankBelow2 = wasPledge.getRankBelow(rankBelow);
            float newAlign = rankBelow2.alignment;
            newAlign = Math.max(newAlign, pledgeLvl / 2.0F);
            float alignPenalty = newAlign - prevAlign;
            if (alignPenalty < 0.0F) {
                GOTAlignmentValues.AlignmentBonus penalty = GOTAlignmentValues.createPledgePenalty(alignPenalty);
                double alignX;
                double alignY;
                double alignZ;
                double lookRange = 2.0D;
                Vec3 posEye = Vec3.createVectorHelper(entityplayer.posX, entityplayer.boundingBox.minY + entityplayer.getEyeHeight(), entityplayer.posZ);
                Vec3 look = entityplayer.getLook(1.0F);
                Vec3 posSight = posEye.addVector(look.xCoord * lookRange, look.yCoord * lookRange, look.zCoord * lookRange);
                MovingObjectPosition mop = world.rayTraceBlocks(posEye, posSight);
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    alignX = mop.blockX + 0.5D;
                    alignY = mop.blockY + 0.5D;
                    alignZ = mop.blockZ + 0.5D;
                } else {
                    alignX = posSight.xCoord;
                    alignY = posSight.yCoord;
                    alignZ = posSight.zCoord;
                }
                this.addAlignment(entityplayer, penalty, wasPledge, alignX, alignY, alignZ);
            }
            world.playSoundAtEntity(entityplayer, "got:event.unpledge", 1.0F, 1.0F);
            ChatComponentTranslation chatComponentTranslation;
            if (intentional) {
                chatComponentTranslation = new ChatComponentTranslation("got.chat.unpledge", wasPledge.factionName());
            } else {
                chatComponentTranslation = new ChatComponentTranslation("got.chat.autoUnpledge", wasPledge.factionName());
            }
            entityplayer.addChatMessage(chatComponentTranslation);
            this.checkAlignmentAchievements(wasPledge, prevAlign);
        }
    }

    public void runAchievementChecks(EntityPlayer entityplayer, World world) {
        int i = MathHelper.floor_double(entityplayer.posX);
        int k = MathHelper.floor_double(entityplayer.posZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
        if (biome instanceof GOTBiome) {
            GOTBiome gotbiome = (GOTBiome) biome;
            GOTAchievement ach = gotbiome.getBiomeAchievement();
            if (ach != null) {
                this.addAchievement(ach);
            }
            GOTWaypoint.Region biomeRegion = gotbiome.getBiomeWaypoints();
            if (biomeRegion != null) {
                this.unlockFTRegion(biomeRegion);
            }
        }
        if (entityplayer.dimension == GOTDimension.GAME_OF_THRONES.dimensionID) {
            this.addAchievement(GOTAchievement.enterKnownWorld);
        }
        if (entityplayer.inventory.hasItem(GOTItems.pouch)) {
            this.addAchievement(GOTAchievement.getPouch);
        }
        Set<Block> tables = new HashSet<>();
        int crossbowBolts = 0;
        for (ItemStack item : entityplayer.inventory.mainInventory) {
            if (item != null && item.getItem() instanceof ItemBlock) {
                Block block = Block.getBlockFromItem(item.getItem());
                if (block instanceof GOTBlockCraftingTable) {
                    tables.add(block);
                }
            }
            if (item != null && item.getItem() instanceof GOTItemCrossbowBolt) {
                crossbowBolts += item.stackSize;
            }
        }
        if (tables.size() >= 10) {
            this.addAchievement(GOTAchievement.collectCraftingTables);
        }
        if (crossbowBolts >= 128) {
            this.addAchievement(GOTAchievement.collectCrossbowBolts);
        }
        if (!this.hasAchievement(GOTAchievement.hundreds) && this.pdTick % 20 == 0) {
            int hiredUnits = 0;
            List<GOTEntityNPC> nearbyNPCs = world.getEntitiesWithinAABB(GOTEntityNPC.class, entityplayer.boundingBox.expand(64.0D, 64.0D, 64.0D));
            for (GOTEntityNPC npc : nearbyNPCs) {
                if (npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == entityplayer) {
                    hiredUnits++;
                }
            }
            if (hiredUnits >= 100) {
                this.addAchievement(GOTAchievement.hundreds);
            }
        }
        if (!this.hasAchievement(GOTAchievement.hireGoldenCompany) && this.pdTick % 20 == 0) {
            int hiredUnits = 0;
            List<GOTEntityGoldenMan> nearbyNPCs = world.getEntitiesWithinAABB(GOTEntityGoldenMan.class, entityplayer.boundingBox.expand(64.0D, 64.0D, 64.0D));
            for (GOTEntityNPC npc : nearbyNPCs) {
                if (npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == entityplayer) {
                    hiredUnits++;
                }
            }
            if (hiredUnits >= 10) {
                this.addAchievement(GOTAchievement.hireGoldenCompany);
            }
        }
        ArmorMaterial fullMaterial;
        ItemArmor.ArmorMaterial material = getFullArmorMaterial(entityplayer);
        if (GOTAchievement.armorAchievements.containsKey(material)) {
            GOTLevelData.getData(entityplayer).addAchievement(GOTAchievement.armorAchievements.get(material));
        }
        fullMaterial = getBodyMaterial(entityplayer);
        if (fullMaterial != null && fullMaterial == GOTMaterial.HAND) {
            this.addAchievement(GOTAchievement.wearFullHand);
        }
        fullMaterial = getHelmetMaterial(entityplayer);
        if (fullMaterial != null && fullMaterial == GOTMaterial.HELMET) {
            this.addAchievement(GOTAchievement.wearFullHelmet);
        }
        fullMaterial = getFullArmorMaterialWithoutHelmet(entityplayer);
        if (fullMaterial != null && fullMaterial == GOTMaterial.MOSSOVY) {
            this.addAchievement(GOTAchievement.wearFullMossovy);
        }
        fullMaterial = getFullArmorMaterialWithoutHelmet(entityplayer);
        if (fullMaterial != null && fullMaterial == GOTMaterial.ICE) {
            this.addAchievement(GOTAchievement.wearFullWhitewalkers);
        }
    }

    public void save(NBTTagCompound playerData) {
        NBTTagList alignmentTags = new NBTTagList();
        for (Map.Entry<GOTFaction, Float> entry : this.alignments.entrySet()) {
            GOTFaction faction = entry.getKey();
            float alignment = entry.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Faction", faction.codeName());
            nbt.setFloat("AlignF", alignment);
            alignmentTags.appendTag(nbt);
        }
        playerData.setTag("AlignmentMap", alignmentTags);
        NBTTagList factionDataTags = new NBTTagList();
        for (Map.Entry<GOTFaction, GOTFactionData> entry : this.factionDataMap.entrySet()) {
            GOTFaction faction = entry.getKey();
            GOTFactionData data = entry.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Faction", faction.codeName());
            data.save(nbt);
            factionDataTags.appendTag(nbt);
        }
        playerData.setTag("FactionData", factionDataTags);
        playerData.setString("CurrentFaction", this.viewingFaction.codeName());
        NBTTagList prevRegionFactionTags = new NBTTagList();
        for (Map.Entry<GOTDimension.DimensionRegion, GOTFaction> entry : this.prevRegionFactions.entrySet()) {
            GOTDimension.DimensionRegion region = entry.getKey();
            GOTFaction faction = entry.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Region", region.codeName());
            nbt.setString("Faction", faction.codeName());
            prevRegionFactionTags.appendTag(nbt);
        }
        playerData.setTag("PrevRegionFactions", prevRegionFactionTags);
        playerData.setBoolean("HideAlignment", this.hideAlignment);
        NBTTagList takenRewardsTags = new NBTTagList();
        for (GOTFaction faction : this.takenAlignmentRewards) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Faction", faction.codeName());
            takenRewardsTags.appendTag(nbt);
        }
        playerData.setTag("TakenAlignmentRewards", takenRewardsTags);
        if (this.pledgeFaction != null) {
            playerData.setString("PledgeFac", this.pledgeFaction.codeName());
        }
        playerData.setInteger("PledgeKillCD", this.pledgeKillCooldown);
        playerData.setInteger("PledgeBreakCD", this.pledgeBreakCooldown);
        playerData.setInteger("PledgeBreakCDStart", this.pledgeBreakCooldownStart);
        if (this.brokenPledgeFaction != null) {
            playerData.setString("BrokenPledgeFac", this.brokenPledgeFaction.codeName());
        }
        playerData.setBoolean("HideOnMap", this.hideOnMap);
        playerData.setBoolean("AdminHideMap", this.adminHideMap);
        playerData.setBoolean("ShowWP", this.showWaypoints);
        playerData.setBoolean("ShowCWP", this.showCustomWaypoints);
        playerData.setBoolean("ShowHiddenSWP", this.showHiddenSharedWaypoints);
        playerData.setBoolean("ConquestKills", this.conquestKills);
        NBTTagList achievementTags = new NBTTagList();
        for (GOTAchievement achievement : this.achievements) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Category", achievement.category.name());
            nbt.setInteger("ID", achievement.ID);
            achievementTags.appendTag(nbt);
        }
        playerData.setTag("Achievements", achievementTags);
        if (this.shield != null) {
            playerData.setString("Shield", this.shield.name());
        }
        playerData.setBoolean("FriendlyFire", this.friendlyFire);
        playerData.setBoolean("HiredDeathMessages", this.hiredDeathMessages);
        if (this.deathPoint != null) {
            playerData.setInteger("DeathX", this.deathPoint.posX);
            playerData.setInteger("DeathY", this.deathPoint.posY);
            playerData.setInteger("DeathZ", this.deathPoint.posZ);
            playerData.setInteger("DeathDim", this.deathDim);
        }
        playerData.setInteger("Alcohol", this.alcoholTolerance);
        NBTTagList miniquestTags = new NBTTagList();
        for (GOTMiniQuest quest : this.miniQuests) {
            NBTTagCompound nbt = new NBTTagCompound();
            quest.writeToNBT(nbt);
            miniquestTags.appendTag(nbt);
        }
        playerData.setTag("MiniQuests", miniquestTags);
        NBTTagList miniquestCompletedTags = new NBTTagList();
        for (GOTMiniQuest quest : this.miniQuestsCompleted) {
            NBTTagCompound nbt = new NBTTagCompound();
            quest.writeToNBT(nbt);
            miniquestCompletedTags.appendTag(nbt);
        }
        playerData.setTag("MiniQuestsCompleted", miniquestCompletedTags);
        playerData.setInteger("MQCompleteCount", this.completedMiniquestCount);
        playerData.setInteger("MQCompletedBounties", this.completedBountyQuests);
        if (this.trackingMiniQuestID != null) {
            playerData.setString("MiniQuestTrack", this.trackingMiniQuestID.toString());
        }
        NBTTagList bountyTags = new NBTTagList();
        for (GOTFaction fac : this.bountiesPlaced) {
            String fName = fac.codeName();
            bountyTags.appendTag(new NBTTagString(fName));
        }
        playerData.setTag("BountiesPlaced", bountyTags);
        if (this.lastWaypoint != null) {
            String lastWPName = this.lastWaypoint.getCodeName();
            playerData.setString("LastWP", lastWPName);
        }
        if (this.lastBiome != null) {
            int lastBiomeID = this.lastBiome.biomeID;
            playerData.setShort("LastBiome", (short) lastBiomeID);
        }
        NBTTagList sentMessageTags = new NBTTagList();
        for (Map.Entry<GOTGuiMessageTypes, Boolean> entry : this.sentMessageTypes.entrySet()) {
            GOTGuiMessageTypes message = entry.getKey();
            boolean sent = entry.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Message", message.getSaveName());
            nbt.setBoolean("Sent", sent);
            sentMessageTags.appendTag(nbt);
        }
        playerData.setTag("SentMessageTypes", sentMessageTags);
        if (this.playerTitle != null) {
            playerData.setString("PlayerTitle", this.playerTitle.getTitle().getTitleName());
            playerData.setInteger("PlayerTitleColor", this.playerTitle.getColor().getFormattingCode());
        }
        playerData.setBoolean("FemRankOverride", this.femRankOverride);
        playerData.setInteger("FTSince", this.ftSinceTick);
        if (this.uuidToMount != null) {
            playerData.setString("MountUUID", this.uuidToMount.toString());
        }
        playerData.setInteger("MountUUIDTime", this.uuidToMountTime);
        playerData.setLong("LastOnlineTime", this.lastOnlineTime);
        NBTTagList unlockedFTRegionTags = new NBTTagList();
        for (GOTWaypoint.Region region : this.unlockedFTRegions) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Name", region.name());
            unlockedFTRegionTags.appendTag(nbt);
        }
        playerData.setTag("UnlockedFTRegions", unlockedFTRegionTags);
        NBTTagList customWaypointTags = new NBTTagList();
        for (GOTCustomWaypoint waypoint : this.customWaypoints) {
            NBTTagCompound nbt = new NBTTagCompound();
            waypoint.writeToNBT(nbt, this);
            customWaypointTags.appendTag(nbt);
        }
        playerData.setTag("CustomWaypoints", customWaypointTags);
        NBTTagList cwpSharedUnlockedTags = new NBTTagList();
        for (CWPSharedKey key : this.cwpSharedUnlocked) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("SharingPlayer", key.sharingPlayer.toString());
            nbt.setInteger("CustomID", key.waypointID);
            cwpSharedUnlockedTags.appendTag(nbt);
        }
        playerData.setTag("CWPSharedUnlocked", cwpSharedUnlockedTags);
        NBTTagList cwpSharedHiddenTags = new NBTTagList();
        for (CWPSharedKey key : this.cwpSharedHidden) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("SharingPlayer", key.sharingPlayer.toString());
            nbt.setInteger("CustomID", key.waypointID);
            cwpSharedHiddenTags.appendTag(nbt);
        }
        playerData.setTag("CWPSharedHidden", cwpSharedHiddenTags);
        NBTTagList wpUseTags = new NBTTagList();
        for (Map.Entry<GOTWaypoint, Integer> e : this.wpUseCounts.entrySet()) {
            GOTAbstractWaypoint wp = e.getKey();
            int count = e.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("WPName", wp.getCodeName());
            nbt.setInteger("Count", count);
            wpUseTags.appendTag(nbt);
        }
        playerData.setTag("WPUses", wpUseTags);
        NBTTagList cwpUseTags = new NBTTagList();
        for (Map.Entry<Integer, Integer> e : this.cwpUseCounts.entrySet()) {
            int ID = e.getKey();
            int count = e.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("CustomID", ID);
            nbt.setInteger("Count", count);
            cwpUseTags.appendTag(nbt);
        }
        playerData.setTag("CWPUses", cwpUseTags);
        NBTTagList cwpSharedUseTags = new NBTTagList();
        for (Map.Entry<CWPSharedKey, Integer> e : this.cwpSharedUseCounts.entrySet()) {
            CWPSharedKey key = e.getKey();
            int count = e.getValue();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("SharingPlayer", key.sharingPlayer.toString());
            nbt.setInteger("CustomID", key.waypointID);
            nbt.setInteger("Count", count);
            cwpSharedUseTags.appendTag(nbt);
        }
        playerData.setTag("CWPSharedUses", cwpSharedUseTags);
        playerData.setInteger("NextCWPID", this.nextCwpID);
        NBTTagList fellowshipTags = new NBTTagList();
        for (UUID fsID : this.fellowshipIDs) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("ID", fsID.toString());
            fellowshipTags.appendTag(nbt);
        }
        playerData.setTag("Fellowships", fellowshipTags);
        NBTTagList fellowshipInviteTags = new NBTTagList();
        for (GOTFellowshipInvite invite : this.fellowshipInvites) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("ID", invite.fellowshipID.toString());
            if (invite.inviterID != null) {
                nbt.setString("InviterID", invite.inviterID.toString());
            }
            fellowshipInviteTags.appendTag(nbt);
        }
        playerData.setTag("FellowshipInvites", fellowshipInviteTags);
        if (this.chatBoundFellowshipID != null) {
            playerData.setString("ChatBoundFellowship", this.chatBoundFellowshipID.toString());
        }
        playerData.setBoolean("StructuresBanned", this.structuresBanned);
        playerData.setBoolean("TeleportedKW", this.teleportedKW);
        NBTTagCompound questNBT = new NBTTagCompound();
        this.questData.save(questNBT);
        playerData.setTag("QuestData", questNBT);

        playerData.setBoolean("AskedForJaqen", this.askedForJaqen);
        playerData.setInteger("Balance", this.balance);
        if (this.cape != null) {
            playerData.setString("Cape", this.cape.name());
        }
        playerData.setBoolean("CheckedMenu", this.checkedMenu);
        playerData.setBoolean("TableSwitched", this.tableSwitched);
        this.needsSave = false;
    }

    public List<GOTMiniQuest> selectMiniQuests(MiniQuestSelector selector) {
        List<GOTMiniQuest> ret = new ArrayList<>();
        List<GOTMiniQuest> threadSafe = new ArrayList<>(this.miniQuests);
        for (GOTMiniQuest quest : threadSafe) {
            if (selector.include(quest)) {
                ret.add(quest);
            }
        }
        return ret;
    }

    public void sendAchievementPacket(EntityPlayerMP entityplayer, GOTAchievement achievement, boolean display) {
        GOTPacketAchievement packet = new GOTPacketAchievement(achievement, display);
        GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
    }

    public void sendAchievementRemovePacket(EntityPlayerMP entityplayer, GOTAchievement achievement) {
        GOTPacketAchievementRemove packet = new GOTPacketAchievementRemove(achievement);
        GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
    }

    public void sendAlignmentBonusPacket(GOTAlignmentValues.AlignmentBonus source, GOTFaction faction, float prevMainAlignment, GOTAlignmentBonusMap factionMap, float conqBonus, double posX, double posY, double posZ) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null) {
            GOTPacketAlignmentBonus packet = new GOTPacketAlignmentBonus(faction, prevMainAlignment, factionMap, conqBonus, posX, posY, posZ, source);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void sendFellowshipInvitePacket(GOTFellowship fs) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketFellowship packet = new GOTPacketFellowship(this, fs, true);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void sendFellowshipInviteRemovePacket(GOTFellowship fs) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketFellowshipRemove packet = new GOTPacketFellowshipRemove(fs, true);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void sendFellowshipPacket(GOTFellowship fs) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketFellowship packet = new GOTPacketFellowship(this, fs, false);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void sendFellowshipRemovePacket(GOTFellowship fs) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketFellowshipRemove packet = new GOTPacketFellowshipRemove(fs, false);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void sendFTBouncePacket(EntityPlayerMP entityplayer) {
        GOTPacketFTBounceClient packet = new GOTPacketFTBounceClient();
        GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
    }

    public void sendFTPacket(EntityPlayerMP entityplayer, GOTAbstractWaypoint waypoint, int startX, int startZ) {
        GOTPacketFTScreen packet = new GOTPacketFTScreen(waypoint, startX, startZ);
        GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
    }

    public void sendMessageIfNotReceived(GOTGuiMessageTypes message) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            boolean sent = this.sentMessageTypes.computeIfAbsent(message, k -> false);
            if (!sent) {
                this.sentMessageTypes.put(message, true);
                this.markDirty();
                GOTPacketMessage packet = new GOTPacketMessage(message);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        }
    }

    public void sendMiniQuestPacket(EntityPlayerMP entityplayer, GOTMiniQuest quest, boolean completed) {
        NBTTagCompound nbt = new NBTTagCompound();
        quest.writeToNBT(nbt);
        GOTPacketMiniquest packet = new GOTPacketMiniquest(nbt, completed);
        GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
    }

    public void sendOptionsPacket(int option, boolean flag) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketOptions packet = new GOTPacketOptions(option, flag);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void sendPlayerData(EntityPlayerMP entityplayer) {
        NBTTagCompound nbt = new NBTTagCompound();
        this.save(nbt);
        nbt.removeTag("Achievements");
        nbt.removeTag("MiniQuests");
        nbt.removeTag("MiniQuestsCompleted");
        nbt.removeTag("CustomWaypoints");
        nbt.removeTag("Fellowships");
        nbt.removeTag("FellowshipInvites");
        GOTPacketLoginPlayerData packet = new GOTPacketLoginPlayerData(nbt);
        GOTPacketHandler.networkWrapper.sendTo(packet, entityplayer);
        for (GOTAchievement achievement : this.achievements) {
            this.sendAchievementPacket(entityplayer, achievement, false);
        }
        for (GOTMiniQuest quest : this.miniQuests) {
            this.sendMiniQuestPacket(entityplayer, quest, false);
        }
        for (GOTMiniQuest quest : this.miniQuestsCompleted) {
            this.sendMiniQuestPacket(entityplayer, quest, true);
        }
        for (GOTCustomWaypoint waypoint : this.customWaypoints) {
            GOTPacketCreateCWPClient cwpPacket = waypoint.getClientPacket();
            GOTPacketHandler.networkWrapper.sendTo(cwpPacket, entityplayer);
        }
        for (UUID fsID : this.fellowshipIDs) {
            GOTFellowship fs = GOTFellowshipData.getActiveFellowship(fsID);
            if (fs != null) {
                this.sendFellowshipPacket(fs);
                fs.doRetroactiveWaypointSharerCheckIfNeeded();
                this.checkIfStillWaypointSharerForFellowship(fs);
            }
        }
        Set<GOTFellowshipInvite> staleFellowshipInvites = new HashSet<>();
        for (GOTFellowshipInvite invite : this.fellowshipInvites) {
            GOTFellowship fs = GOTFellowshipData.getFellowship(invite.fellowshipID);
            if (fs != null) {
                this.sendFellowshipInvitePacket(fs);
                continue;
            }
            staleFellowshipInvites.add(invite);
        }
        if (!staleFellowshipInvites.isEmpty()) {
            this.fellowshipInvites.removeAll(staleFellowshipInvites);
            this.markDirty();
        }
        this.addSharedCustomWaypointsFromAllFellowships();
    }

    public void setAlignment(GOTFaction faction, float alignment) {
        EntityPlayer entityplayer = this.getPlayer();
        if (faction.isPlayableAlignmentFaction()) {
            float prevAlignment = this.getAlignment(faction);
            this.alignments.put(faction, alignment);
            this.markDirty();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTLevelData.sendAlignmentToAllPlayersInWorld(entityplayer, entityplayer.worldObj);
            }
            this.checkAlignmentAchievements(faction, prevAlignment);
        }
        if (entityplayer != null && !entityplayer.worldObj.isRemote && this.pledgeFaction != null && !this.canPledgeTo(this.pledgeFaction)) {
            this.revokePledgeFaction(entityplayer, false);
        }
    }

    public void setAlignmentFromCommand(GOTFaction faction, float set) {
        this.setAlignment(faction, set);
    }

    public void setChatBoundFellowshipID(UUID fsID) {
        this.chatBoundFellowshipID = fsID;
        this.markDirty();
    }

    public void setCheckedMenu(boolean flag) {
        if (this.checkedMenu != flag) {
            this.checkedMenu = flag;
            this.markDirty();
        }
    }

    public void setDeathPoint(int i, int j, int k) {
        this.deathPoint = new ChunkCoordinates(i, j, k);
        this.markDirty();
    }

    public void setFellowshipAdmin(GOTFellowship fs, UUID player, boolean flag, String granterUsername) {
        if (fs.isOwner(this.playerUUID)) {
            fs.setAdmin(player, flag);
            EntityPlayer subjectPlayer = this.getOtherPlayer(player);
            if (subjectPlayer != null && !subjectPlayer.worldObj.isRemote) {
                if (flag) {
                    fs.sendNotification(subjectPlayer, "got.gui.fellowships.notifyOp", granterUsername);
                } else {
                    fs.sendNotification(subjectPlayer, "got.gui.fellowships.notifyDeop", granterUsername);
                }
            }
        }
    }

    public void setFellowshipIcon(GOTFellowship fs, ItemStack itemstack) {
        if (fs.isOwner(this.playerUUID) || fs.isAdmin(this.playerUUID)) {
            fs.setIcon(itemstack);
        }
    }

    public void setFellowshipPreventHiredFF(GOTFellowship fs, boolean prevent) {
        if (fs.isOwner(this.playerUUID) || fs.isAdmin(this.playerUUID)) {
            fs.setPreventHiredFriendlyFire(prevent);
        }
    }

    public void setFellowshipPreventPVP(GOTFellowship fs, boolean prevent) {
        if (fs.isOwner(this.playerUUID) || fs.isAdmin(this.playerUUID)) {
            fs.setPreventPVP(prevent);
        }
    }

    public void setFellowshipShowMapLocations(GOTFellowship fs, boolean show) {
        if (fs.isOwner(this.playerUUID)) {
            fs.setShowMapLocations(show);
        }
    }

    public void setRegionLastViewedFaction(GOTDimension.DimensionRegion region, GOTFaction fac) {
        if (region.factionList.contains(fac)) {
            this.prevRegionFactions.put(region, fac);
            this.markDirty();
        }
    }

    public void setShowCustomWaypoints(boolean flag) {
        this.showCustomWaypoints = flag;
        this.markDirty();
    }

    public void setShowHiddenSharedWaypoints(boolean flag) {
        this.showHiddenSharedWaypoints = flag;
        this.markDirty();
    }

    public void setShowWaypoints(boolean flag) {
        this.showWaypoints = flag;
        this.markDirty();
    }

    public void setTargetFTWaypoint(GOTAbstractWaypoint wp) {
        this.targetFTWaypoint = wp;
        this.markDirty();
        if (wp != null) {
            this.setTicksUntilFT(ticksUntilFT_max);
        } else {
            this.setTicksUntilFT(0);
        }
    }

    public void setTicksUntilFT(int i) {
        if (this.ticksUntilFT != i) {
            this.ticksUntilFT = i;
            if (this.ticksUntilFT == ticksUntilFT_max || this.ticksUntilFT == 0) {
                this.markDirty();
            }
        }
    }

    public void setTimeSinceFT(int i, boolean forceUpdate) {
        int preTick = this.ftSinceTick;
        i = Math.max(0, i);
        this.ftSinceTick = i;
        boolean bigChange = (this.ftSinceTick == 0 || preTick == 0) && this.ftSinceTick != preTick || preTick < 0;
        if (bigChange || isTimerAutosaveTick() || forceUpdate) {
            this.markDirty();
        }
        if (bigChange || this.ftSinceTick % 5 == 0 || forceUpdate) {
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketFTTimer packet = new GOTPacketFTTimer(this.ftSinceTick);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        }
    }
    public void setDragonFireballCooldown(int i) {
        this.setDragonFireballCooldown(i, false);
    }

    public void setDragonFireballCooldown(int i, boolean forceUpdate) {
        int preTick = this.dragonFireballSinceTick;
        this.dragonFireballSinceTick = i = Math.max(0, i);
        boolean bigChange = (((this.dragonFireballSinceTick == 0 || preTick == 0) && this.dragonFireballSinceTick != preTick) || (preTick < 0 && this.dragonFireballSinceTick >= 0));
        if (bigChange || isTimerAutosaveTick() || forceUpdate) {
            this.markDirty();
        }
        EntityPlayer entityplayer;
        if ((bigChange || this.dragonFireballSinceTick % 5 == 0 || forceUpdate) && (entityplayer = this.getPlayer()) != null && !entityplayer.worldObj.isRemote) {
            GOTPacketFireballTimer packet = new GOTPacketFireballTimer(this.dragonFireballSinceTick);
            GOTPacketHandler.networkWrapper.sendTo((IMessage)packet, (EntityPlayerMP)entityplayer);
        }
    }
    public void setTimeSinceFTWithUpdate(int i) {
        this.setTimeSinceFT(i, true);
    }

    public void setTrackingMiniQuestID(UUID npcID) {
        this.trackingMiniQuestID = npcID;
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketMiniquestTrackClient packet = new GOTPacketMiniquestTrackClient(this.trackingMiniQuestID);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void setUUIDToMount(UUID uuid) {
        this.uuidToMount = uuid;
        if (this.uuidToMount != null) {
            this.uuidToMountTime = 10;
        } else {
            this.uuidToMountTime = 0;
        }
        this.markDirty();
    }

    public void setWPUseCount(GOTAbstractWaypoint wp, int count) {
        if (wp instanceof GOTCustomWaypoint) {
            GOTCustomWaypoint cwp = (GOTCustomWaypoint) wp;
            int ID = cwp.getID();
            if (cwp.isShared()) {
                UUID sharingPlayer = cwp.getSharingPlayerID();
                CWPSharedKey key = CWPSharedKey.keyFor(sharingPlayer, ID);
                this.cwpSharedUseCounts.put(key, count);
            } else {
                this.cwpUseCounts.put(ID, count);
            }
        } else {
            this.wpUseCounts.put((GOTWaypoint) wp, count);
        }
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketWaypointUseCount packet = new GOTPacketWaypointUseCount(wp, count);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public boolean showCustomWaypoints() {
        return this.showCustomWaypoints;
    }

    public boolean showHiddenSharedWaypoints() {
        return this.showHiddenSharedWaypoints;
    }

    public boolean showWaypoints() {
        return this.showWaypoints;
    }

    public void transferFellowship(GOTFellowship fs, UUID player, String prevOwnerUsername) {
        if (fs.isOwner(this.playerUUID)) {
            fs.setOwner(player);
            EntityPlayer newOwner = this.getOtherPlayer(player);
            if (newOwner != null && !newOwner.worldObj.isRemote) {
                fs.sendNotification(newOwner, "got.gui.fellowships.notifyTransfer", prevOwnerUsername);
            }
        }
    }

    public void unlockFTRegion(GOTWaypoint.Region region) {
        if (this.isSiegeActive())
            return;
        if (this.unlockedFTRegions.add(region)) {
            this.markDirty();
            EntityPlayer entityplayer = this.getPlayer();
            if (entityplayer != null && !entityplayer.worldObj.isRemote) {
                GOTPacketWaypointRegion packet = new GOTPacketWaypointRegion(region, true);
                GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
            }
        }
    }

    public void unlockSharedCustomWaypoint(GOTCustomWaypoint waypoint) {
        if (!waypoint.isShared()) {
            FMLLog.warning("Hummel009: Warning! Tried to unlock a shared custom waypoint with no owner!");
            return;
        }
        waypoint.setSharedUnlocked();
        CWPSharedKey key = CWPSharedKey.keyFor(waypoint.getSharingPlayerID(), waypoint.getID());
        this.cwpSharedUnlocked.add(key);
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            GOTPacketCWPSharedUnlockClient packet = waypoint.getClientSharedUnlockPacket();
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void unlockSharedCustomWaypoints(EntityPlayer entityplayer) {
        if (this.pdTick % 20 == 0 && entityplayer.dimension == GOTDimension.GAME_OF_THRONES.dimensionID) {
            List<GOTCustomWaypoint> unlockWaypoints = new ArrayList<>();
            for (GOTCustomWaypoint waypoint : this.customWaypointsShared) {
                if (waypoint.isShared() && !waypoint.isSharedUnlocked() && waypoint.canUnlockShared(entityplayer)) {
                    unlockWaypoints.add(waypoint);
                }
            }
            for (GOTCustomWaypoint waypoint : unlockWaypoints) {
                this.unlockSharedCustomWaypoint(waypoint);
            }
        }
    }

    public void unshareFellowshipFromOwnCustomWaypoints(GOTFellowship fs) {
        for (GOTCustomWaypoint waypoint : this.customWaypoints) {
            if (waypoint.hasSharedFellowship(fs)) {
                this.customWaypointRemoveSharedFellowship(waypoint, fs);
            }
        }
    }

    public void updateFactionData(GOTFaction faction, GOTFactionData factionData) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            this.markDirty();
            NBTTagCompound nbt = new NBTTagCompound();
            factionData.save(nbt);
            GOTPacketFactionData packet = new GOTPacketFactionData(faction, nbt);
            GOTPacketHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) entityplayer);
        }
    }

    public void updateFastTravelClockFromLastOnlineTime(ICommandSender player, World world) {
        if (this.lastOnlineTime <= 0L)
            return;
        MinecraftServer server = MinecraftServer.getServer();
        if (!server.isSinglePlayer()) {
            long currentOnlineTime = this.getCurrentOnlineTime();
            int diff = (int) (currentOnlineTime - this.lastOnlineTime);
            double offlineFactor = 0.1D;
            int ftClockIncrease = (int) (diff * offlineFactor);
            if (ftClockIncrease > 0) {
                this.setTimeSinceFTWithUpdate(this.ftSinceTick + ftClockIncrease);
                ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("got.chat.ft.offlineTick", GOTLevelData.getHMSTime_Ticks(ftClockIncrease));
                player.addChatMessage(chatComponentTranslation);
            }
        }
    }

    public void updateFellowship(GOTFellowship fs, FellowshipUpdateType updateType) {
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            IMessage updatePacket = updateType.createUpdatePacket(this, fs);
            if (updatePacket != null) {
                GOTPacketHandler.networkWrapper.sendTo(updatePacket, (EntityPlayerMP) entityplayer);
            } else {
                GOTLog.logger.error("No associated packet for fellowship update type {}", updateType.getClass().getName());
            }
        }
        List<UUID> playersToCheckSharedWaypointsFrom = updateType.getPlayersToCheckSharedWaypointsFrom(fs);
        if (playersToCheckSharedWaypointsFrom != null && !playersToCheckSharedWaypointsFrom.isEmpty()) {
            this.addSharedCustomWaypointsFrom(fs.getFellowshipID(), playersToCheckSharedWaypointsFrom);
            this.checkCustomWaypointsSharedBy(playersToCheckSharedWaypointsFrom);
        }
    }

    public void updateMiniQuest(GOTMiniQuest quest) {
        this.markDirty();
        EntityPlayer entityplayer = this.getPlayer();
        if (entityplayer != null && !entityplayer.worldObj.isRemote) {
            this.sendMiniQuestPacket((EntityPlayerMP) entityplayer, quest, false);
        }
    }

    public boolean useFeminineRanks() {
        if (this.femRankOverride)
            return true;
        if (this.playerTitle != null) {
            GOTTitle title = this.playerTitle.getTitle();
            return title.isFeminineRank();
        }
        return false;
    }

    public static class CWPSharedKey extends Pair<UUID, Integer> {
        public UUID sharingPlayer;
        public int waypointID;

        public CWPSharedKey(UUID player, int id) {
            this.sharingPlayer = player;
            this.waypointID = id;
        }

        public static CWPSharedKey keyFor(UUID player, int id) {
            return new CWPSharedKey(player, id);
        }

        @Override
        public UUID getLeft() {
            return this.sharingPlayer;
        }

        @Override
        public Integer getRight() {
            return this.waypointID;
        }

        @Override
        public Integer setValue(Integer value) {
            throw new UnsupportedOperationException();
        }
    }
}
