package com.jelly.farmhelperv2.failsafe.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.farmhelperv2.config.page.FailsafeNotificationsPage;
import com.jelly.farmhelperv2.failsafe.Failsafe;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.impl.AutoSell;
import com.jelly.farmhelperv2.feature.impl.LagDetector;
import com.jelly.farmhelperv2.feature.impl.MovRecPlayer;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.TimeUnit;

public class FullInventoryFailsafe extends Failsafe {
    private static FullInventoryFailsafe instance;
    private final Clock clock = new Clock();

    public static FullInventoryFailsafe getInstance() {
        if (instance == null) {
            instance = new FullInventoryFailsafe();
        }
        return instance;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE; // Changed from 3 to Integer.MAX_VALUE to make it lowest priority
    }

    @Override
    public FailsafeManager.EmergencyType getType() {
        return FailsafeManager.EmergencyType.FULL_INVENTORY;
    }

    @Override
    public boolean shouldSendNotification() {
        return false; // Modified to never send notifications
    }

    @Override
    public boolean shouldPlaySound() {
        return false; // Modified to never play sounds
    }

    @Override
    public boolean shouldTagEveryone() {
        return false; // Modified to never tag everyone
    }

    @Override
    public boolean shouldAltTab() {
        return false; // Modified to never alt tab
    }
    
    @Override
    public void onTickDetection(TickEvent.ClientTickEvent event) {
        // Completely empty method to disable detection
        return;
    }

    // use item change failsafe movements
    @Override
    public void duringFailsafeTrigger() {
        // Skip directly to the end state
        endOfFailsafeTrigger();
    }

    @Override
    public void endOfFailsafeTrigger() {
        FailsafeManager.getInstance().stopFailsafes();
        
        // Optional: You can comment this out if you don't want auto-sell to trigger
        Multithreading.schedule(() -> {
            if (GameStateHandler.getInstance().getCookieBuffState() != GameStateHandler.BuffState.ACTIVE) {
                LogUtils.sendDebug("[Failsafe] Looking at sb menu...");
                mc.thePlayer.inventory.currentItem = 8;
                KeyBindUtils.rightClick();
            } else {
                LogUtils.sendDebug("[Failsafe] Enabling auto sell...");
                AutoSell.getInstance().start();
            }
        }, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void resetStates() {
        clock.reset();
        itemChangeState = ItemChangeState.NONE;
    }

    private ItemChangeState itemChangeState = ItemChangeState.NONE;
    enum ItemChangeState {
        NONE,
        WAIT_BEFORE_START,
        LOOK_AROUND,
        SWAP_BACK_ITEM,
        END
    }
}
