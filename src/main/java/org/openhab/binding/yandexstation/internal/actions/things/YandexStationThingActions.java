/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yandexstation.internal.actions.things;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.YandexStationHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexStationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@ThingActionsScope(name = "yandexstation")
@NonNullByDefault
public class YandexStationThingActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(YandexStationThingActions.class);

    private @Nullable YandexStationHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (YandexStationHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void activate() {
        ThingActions.super.activate();
    }

    @Override
    public void deactivate() {
        ThingActions.super.deactivate();
    }

    @RuleAction(label = "@text/actionSayLabel", description = "@text/actionSayDescription")
    public void sayText(
            @ActionInput(name = "message", label = "@text/actionSayTextLabel", description = "@text/actionSayTextDescription") @NonNull String message) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }

        handler.sendTtsCommand(message);
    }

    @RuleAction(label = "@text/actionSayLabel", description = "@text/actionSayDescription")
    public void sayText(
            @ActionInput(name = "message", label = "@text/actionSayTextLabel", description = "@text/actionSayTextDescription") @NonNull String message,
            @ActionInput(name = "voice", label = "@text/actionSayTextVoiceLabel", description = "@text/actionSayTextVoiceDescription") @NonNull String voice) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }

        handler.sendTtsCommand(String.format("<speaker voice='%s'>%s", voice, message));
    }

    @RuleAction(label = "@text/actionSayLabel", description = "@text/actionSayDescription")
    public void sayText(
            @ActionInput(name = "message", label = "@text/actionSayTextLabel", description = "@text/actionSayTextDescription") @NonNull String message,
            @ActionInput(name = "whisper", label = "@text/actionSayTextWhisperLabel", description = "@text/actionSayTextWhisperDescription") @NonNull Boolean whisper) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendTtsCommand(String.format("<speaker is_whisper='%s'>%s", whisper, message));
    }

    @RuleAction(label = "@text/actionVoiceCommandLabel", description = "@text/actionVoiceCommandDescription")
    public void voiceCommand(
            @ActionInput(name = "message", label = "@text/actionVoiceCommandTextLabel", description = "@text/actionVoiceCommandTextDescription") @NonNull String message) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }

        handler.sendVoiceCommand(message);
    }

    @RuleAction(label = "@text/actionPlay", description = "@text/actionPlayDescription")
    public void play() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendPlayCommand();
    }

    @RuleAction(label = "@text/actionPause", description = "@text/actionPauseDescription")
    public void pause() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendStopCommand();
    }

    @RuleAction(label = "@text/actionNext", description = "@text/actionNextDescription")
    public void next() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendPlayNextCommand();
    }

    @RuleAction(label = "@text/actionPrev", description = "@text/actionPrevDescription")
    public void prev() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendPlayPrevCommand();
    }

    @RuleAction(label = "@text/actionVolumeUp", description = "@text/actionVolumeUpDescription")
    public void volumeUp() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.volumeUp();
    }

    @RuleAction(label = "@text/actionVolumeDown", description = "@text/actionVolumeDownDescription")
    public void volumeDown() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.volumeDown();
    }

    @RuleAction(label = "@text/actionSetVolume", description = "@text/actionSetVolumeDescription")
    public void setVolume(Integer level) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendSetVolumeCommand( level / 10.0);
    }

    @RuleAction(label = "@text/actionMuteVolume", description = "@text/actionMuteVolumeDescription")
    public void mute(Boolean mute) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.volumeMute();
    }

    @RuleAction(label = "@text/actionStopListening", description = "@text/actionStopListeningDescription")
    public void stopListening() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendStopListening();
    }

    public static void sayText(@Nullable ThingActions actions, @NonNull String description) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).sayText(description);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void sayText(@Nullable ThingActions actions, @NonNull String description, String voice) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).sayText(description, voice);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void sayText(@Nullable ThingActions actions, @NonNull String description, @NonNull Boolean whisper) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).sayText(description, whisper);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void voiceCommand(@Nullable ThingActions actions, @NonNull String description) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).voiceCommand(description);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void play(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).play();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void pause(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).pause();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void next(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).next();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void prev(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).prev();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void volumeUp(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).volumeUp();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void volumeDown(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).volumeDown();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void setVolume(@Nullable ThingActions actions, @NonNull Integer level) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).setVolume(level);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void mute(@Nullable ThingActions actions, @NonNull Boolean mute) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).mute(mute);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    public static void stopListening(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).stopListening();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }
}
