package com.replaymod.online.handler;

import de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.online.ReplayModOnline;
import com.replaymod.online.gui.GuiLoginPrompt;
import com.replaymod.online.gui.GuiReplayCenter;
import com.replaymod.online.gui.GuiUploadReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import com.replaymod.replay.handler.GuiHandler.InjectedButton;
import de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import de.johni0702.minecraft.gui.container.GuiScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;

//#if FABRIC>=1
import de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import net.minecraft.client.gui.screen.Screen;
import java.util.List;
//#else
//$$ import net.minecraftforge.client.event.GuiScreenEvent;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//#endif

import java.io.File;

import static com.replaymod.core.versions.MCVer.*;

public class GuiHandler extends EventRegistrations {
    private static final int BUTTON_REPLAY_CENTER = 17890236;

    private final ReplayModOnline mod;

    public GuiHandler(ReplayModOnline mod) {
        this.mod = mod;
    }

    //#if FABRIC>=1
    { on(InitScreenCallback.EVENT, this::injectIntoMainMenu); }
    public void injectIntoMainMenu(Screen guiScreen, List<AbstractButtonWidget> buttonList) {
    //#else
    //$$ @SubscribeEvent
    //$$ public void injectIntoMainMenu(GuiScreenEvent.InitGuiEvent event) {
    //$$     final net.minecraft.client.gui.screen.Screen guiScreen = getGui(event);
    //#endif
        if (!(guiScreen instanceof TitleScreen)) {
            return;
        }

        InjectedButton button = new InjectedButton(
                guiScreen,
                BUTTON_REPLAY_CENTER,
                guiScreen.width / 2 + 2,
                guiScreen.height / 4 + 10 + 4 * 24,
                98,
                20,
                I18n.translate("replaymod.gui.replaycenter"),
                this::onButton
        );
        addButton(guiScreen, button);
    }

    //#if FABRIC>=1
    { on(InitScreenCallback.EVENT, this::injectIntoReplayViewer); }
    public void injectIntoReplayViewer(Screen vanillaGuiScreen, List<AbstractButtonWidget> buttonList) {
    //#else
    //$$ @SubscribeEvent
    //$$ public void injectIntoReplayViewer(GuiScreenEvent.InitGuiEvent.Post event) {
    //$$     net.minecraft.client.gui.screen.Screen vanillaGuiScreen = getGui(event);
    //#endif
        AbstractGuiScreen guiScreen = GuiScreen.from(vanillaGuiScreen);
        if (!(guiScreen instanceof GuiReplayViewer)) {
            return;
        }
        final GuiReplayViewer replayViewer = (GuiReplayViewer) guiScreen;
        // Inject Upload button
        if (!replayViewer.uploadButton.getChildren().isEmpty()) return;
        replayViewer.replaySpecificButtons.add(new de.johni0702.minecraft.gui.element.GuiButton(replayViewer.uploadButton).onClick(() -> {
            File replayFile = replayViewer.list.getSelected().file;
            GuiUploadReplay uploadGui = new GuiUploadReplay(replayViewer, mod, replayFile);
            if (mod.isLoggedIn()) {
                uploadGui.display();
            } else {
                new GuiLoginPrompt(mod.getApiClient(), replayViewer, uploadGui, true);
            }
        }).setSize(73, 20).setI18nLabel("replaymod.gui.upload").setDisabled());
    }

    //#if MC>=11400
    private void onButton(InjectedButton button) {
        net.minecraft.client.gui.screen.Screen guiScreen = button.guiScreen;
    //#else
    //$$ @SubscribeEvent
    //$$ public void onButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
    //$$     net.minecraft.client.gui.GuiScreen guiScreen = getGui(event);
    //$$     GuiButton button = getButton(event);
    //#endif
        if(!button.active) return;

        if (guiScreen instanceof TitleScreen) {
            if (button.id == BUTTON_REPLAY_CENTER) {
                GuiReplayCenter replayCenter = new GuiReplayCenter(mod);
                if (mod.isLoggedIn()) {
                    replayCenter.display();
                } else {
                    new GuiLoginPrompt(mod.getApiClient(), GuiScreen.wrap(guiScreen), replayCenter, true).display();
                }
            }
        }
    }
}
