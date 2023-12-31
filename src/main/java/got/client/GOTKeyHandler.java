package got.client;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.math.IntMath;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import got.GOT;
import got.common.GOTDimension;
import got.common.GOTLevelData;
import got.common.GOTPlayerData;
import got.common.faction.GOTFaction;
import got.common.network.GOTPacketCargocartControl;
import got.common.network.GOTPacketCheckMenuPrompt;
import got.common.network.GOTPacketDragonControl;
import got.common.network.GOTPacketHandler;
import got.common.network.GOTPacketMenuPrompt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class GOTKeyHandler {
    public static KeyBinding keyBindingMenu = new KeyBinding("Menu", 38, "Game of Thrones");
    public static KeyBinding keyBindingMapTeleport = new KeyBinding("Map Teleport", 50, "Game of Thrones");
    public static KeyBinding keyBindingFastTravel = new KeyBinding("Fast Travel", 33, "Game of Thrones");
    public static KeyBinding keyBindingAlignmentCycleLeft = new KeyBinding("Alignment Cycle Left", 203, "Game of Thrones");
    public static KeyBinding keyBindingAlignmentCycleRight = new KeyBinding("Alignment Cycle Right", 205, "Game of Thrones");
    public static KeyBinding keyBindingAlignmentGroupPrev = new KeyBinding("Alignment Group Prev", 200, "Game of Thrones");
    public static KeyBinding keyBindingAlignmentGroupNext = new KeyBinding("Alignment Group Next", 208, "Game of Thrones");
    public static KeyBinding keyBindingCargoCart = new KeyBinding("Enable cargocart", 19, "Game of Thrones");
    public static KeyBinding keyBindingReturn = new KeyBinding("Clear", Keyboard.KEY_RETURN, "Game of Thrones");
    public static KeyBinding keyBindingDragonUp = new KeyBinding("Fly Up", Keyboard.KEY_G, "Game of Thrones: Dragon");
    public static KeyBinding keyBindingDragonDown = new KeyBinding("Fly Down", Keyboard.KEY_H, "Game of Thrones: Dragon");
    public static KeyBinding keyBindingDragonFireball = new KeyBinding("Shoot Fireball", Keyboard.KEY_J, "Game of Thrones: Dragon");
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int alignmentChangeTick;
    public static int alignmentChangeTime = 2;
    public GOTPacketDragonControl dcm = new GOTPacketDragonControl();

    public GOTKeyHandler() {
        ClientRegistry.registerKeyBinding(keyBindingMenu);
        ClientRegistry.registerKeyBinding(keyBindingMapTeleport);
        ClientRegistry.registerKeyBinding(keyBindingFastTravel);
        ClientRegistry.registerKeyBinding(keyBindingAlignmentCycleLeft);
        ClientRegistry.registerKeyBinding(keyBindingAlignmentCycleRight);
        ClientRegistry.registerKeyBinding(keyBindingAlignmentGroupPrev);
        ClientRegistry.registerKeyBinding(keyBindingAlignmentGroupNext);
        ClientRegistry.registerKeyBinding(keyBindingCargoCart);
        ClientRegistry.registerKeyBinding(keyBindingDragonUp);
        ClientRegistry.registerKeyBinding(keyBindingDragonDown);
        ClientRegistry.registerKeyBinding(keyBindingDragonFireball);
    }

    @SubscribeEvent
    public void KeyInputEvent(InputEvent.KeyInputEvent event) {
        GOTAttackTiming.doAttackTiming();
        if (keyBindingMenu.getIsKeyPressed() && GOTKeyHandler.mc.currentScreen == null) {
            GOTKeyHandler.mc.thePlayer.openGui(GOT.instance, 11, GOTKeyHandler.mc.theWorld, 0, 0, 0);
        }
        GOTPlayerData pd = GOTLevelData.getData(GOTKeyHandler.mc.thePlayer);
        boolean usedAlignmentKeys = false;
        boolean skippedHelp = false;
        HashMap<GOTDimension.DimensionRegion, GOTFaction> lastViewedRegions = new HashMap<>();
        GOTDimension currentDimension = GOTDimension.getCurrentDimension(GOTKeyHandler.mc.theWorld);
        GOTFaction currentFaction = pd.getViewingFaction();
        GOTDimension.DimensionRegion currentRegion = currentFaction.factionRegion;
        List<GOTDimension.DimensionRegion> regionList = currentDimension.dimensionRegions;
        List<GOTFaction> factionList = currentRegion.factionList;
        if (GOTKeyHandler.mc.currentScreen == null && alignmentChangeTick <= 0) {
            int i;
            if (keyBindingReturn.getIsKeyPressed()) {
                skippedHelp = true;
            }
            if (keyBindingAlignmentCycleLeft.getIsKeyPressed()) {
                i = factionList.indexOf(currentFaction);
                --i;
                i = IntMath.mod(i, factionList.size());
                currentFaction = factionList.get(i);
                usedAlignmentKeys = true;
            }
            if (keyBindingAlignmentCycleRight.getIsKeyPressed()) {
                i = factionList.indexOf(currentFaction);
                ++i;
                i = IntMath.mod(i, factionList.size());
                currentFaction = factionList.get(i);
                usedAlignmentKeys = true;
            }
            if (regionList != null && currentRegion != null) {
                if (keyBindingAlignmentGroupPrev.getIsKeyPressed()) {
                    pd.setRegionLastViewedFaction(currentRegion, currentFaction);
                    lastViewedRegions.put(currentRegion, currentFaction);
                    i = regionList.indexOf(currentRegion);
                    --i;
                    i = IntMath.mod(i, regionList.size());
                    currentRegion = regionList.get(i);
                    factionList = currentRegion.factionList;
                    currentFaction = pd.getRegionLastViewedFaction(currentRegion);
                    usedAlignmentKeys = true;
                }
                if (keyBindingAlignmentGroupNext.getIsKeyPressed()) {
                    pd.setRegionLastViewedFaction(currentRegion, currentFaction);
                    lastViewedRegions.put(currentRegion, currentFaction);
                    i = regionList.indexOf(currentRegion);
                    ++i;
                    i = IntMath.mod(i, regionList.size());
                    currentRegion = regionList.get(i);
                    factionList = currentRegion.factionList;
                    currentFaction = pd.getRegionLastViewedFaction(currentRegion);
                    usedAlignmentKeys = true;
                }
            }
        }
        if (skippedHelp && GOTTickHandlerClient.renderMenuPrompt) {
            GOTTickHandlerClient.renderMenuPrompt = false;
            GOTPacketCheckMenuPrompt packet = new GOTPacketCheckMenuPrompt(GOTPacketMenuPrompt.Type.MENU);
            GOTPacketHandler.networkWrapper.sendToServer(packet);
        }
        if (usedAlignmentKeys) {
            GOTClientProxy.sendClientInfoPacket(currentFaction, lastViewedRegions);
            alignmentChangeTick = 2;
        }
        if (keyBindingCargoCart != null && keyBindingCargoCart.isPressed()) {
            GOTPacketHandler.networkWrapper.sendToServer(new GOTPacketCargocartControl());
        }
    }

    @SubscribeEvent
    public void MouseInputEvent(InputEvent.MouseInputEvent event) {
        GOTAttackTiming.doAttackTiming();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent evt) {
        BitSet flags = this.dcm.getFlags();
        flags.set(0, keyBindingDragonUp.getIsKeyPressed());
        flags.set(1, keyBindingDragonDown.getIsKeyPressed());
        flags.set(2, keyBindingDragonFireball.getIsKeyPressed());
        if (this.dcm.hasChanged()) {
            GOTPacketHandler.networkWrapper.sendToServer(this.dcm);
        }
    }

    public static void update() {
        if (alignmentChangeTick > 0) {
            --alignmentChangeTick;
        }
    }
}
