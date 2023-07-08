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

    /**
     * Say text.
     *
     * @param message the message
     */
    @RuleAction(label = "@text/action.SayLabel", description = "@text/action.SayDescription")
    public void sayText(
            @ActionInput(name = "message", label = "@text/action.SayTextLabel", description = "@text/action.SayTextDescription") @NonNull String message) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }

        handler.sendTtsCommand(message);
    }

    /**
     * Say text.
     *
     * @param message the message
     * @param voice   the voice
     */
    @RuleAction(label = "@text/action.SayLabel", description = "@text/action.SayDescription")
    public void sayText(
            @ActionInput(name = "message", label = "@text/action.SayTextLabel", description = "@text/action.SayTextDescription") @NonNull String message,
            @ActionInput(name = "voice", label = "@text/action.SayTextVoiceLabel", description = "@text/action.SayTextVoiceDescription") @NonNull String voice) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }

        handler.sendTtsCommand(String.format("<speaker voice='%s'>%s", voice, message));
    }

    /**
     * Say text.
     *
     * @param message the message
     * @param whisper the whisper
     */
    @RuleAction(label = "@text/action.SayLabel", description = "@text/action.SayDescription")
    public void sayText(
            @ActionInput(name = "message", label = "@text/action.SayTextLabel", description = "@text/action.SayTextDescription") @NonNull String message,
            @ActionInput(name = "whisper", label = "@text/action.SayTextWhisperLabel", description = "@text/action.SayTextWhisperDescription") @NonNull Boolean whisper) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendTtsCommand(String.format("<speaker is_whisper='%s'>%s", whisper, message));
    }

    /**
     * Voice command.
     *
     * @param message the message
     */
    @RuleAction(label = "@text/action.VoiceCommandLabel", description = "@text/action.VoiceCommandDescription")
    public void voiceCommand(
            @ActionInput(name = "message", label = "@text/action.VoiceCommandTextLabel", description = "@text/action.VoiceCommandTextDescription") @NonNull String message) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }

        handler.sendVoiceCommand(message);
    }

    /**
     * Play.
     */
    @RuleAction(label = "@text/action.Play", description = "@text/action.PlayDescription")
    public void play() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendPlayCommand();
    }

    /**
     * Pause.
     */
    @RuleAction(label = "@text/action.Pause", description = "@text/action.PauseDescription")
    public void pause() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendStopCommand();
    }

    /**
     * Next.
     */
    @RuleAction(label = "@text/action.Next", description = "@text/action.NextDescription")
    public void next() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendPlayNextCommand();
    }

    /**
     * Prev.
     */
    @RuleAction(label = "@text/action.Prev", description = "@text/action.PrevDescription")
    public void prev() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendPlayPrevCommand();
    }

    /**
     * Volume up.
     */
    @RuleAction(label = "@text/action.VolumeUp", description = "@text/action.VolumeUpDescription")
    public void volumeUp() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.volumeUp();
    }

    /**
     * Volume down.
     */
    @RuleAction(label = "@text/action.VolumeDown", description = "@text/action.VolumeDownDescription")
    public void volumeDown() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.volumeDown();
    }

    /**
     * Sets volume.
     *
     * @param level the level
     */
    @RuleAction(label = "@text/action.SetVolume", description = "@text/action.SetVolumeDescription")
    public void setVolume(Integer level) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendSetVolumeCommand(level);
    }

    /**
     * Mute.
     *
     * @param mute the mute
     */
    @RuleAction(label = "@text/action.MuteVolume", description = "@text/action.MuteVolumeDescription")
    public void mute(Boolean mute) {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.volumeMute();
    }

    /**
     * Stop listening.
     */
    @RuleAction(label = "@text/action.StopListening", description = "@text/action.StopListeningDescription")
    public void stopListening() {
        YandexStationHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("YandexStationHandler is null");
            return;
        }
        handler.sendStopListening();
    }

    /**
     * Say text.
     *
     * @param actions     the actions
     * @param description the description
     */
    public static void sayText(@Nullable ThingActions actions, @NonNull String description) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).sayText(description);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Say text.
     *
     * @param actions     the actions
     * @param description the description
     * @param voice       the voice
     */
    public static void sayText(@Nullable ThingActions actions, @NonNull String description, String voice) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).sayText(description, voice);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Say text.
     *
     * @param actions     the actions
     * @param description the description
     * @param whisper     the whisper
     */
    public static void sayText(@Nullable ThingActions actions, @NonNull String description, @NonNull Boolean whisper) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).sayText(description, whisper);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Voice command.
     *
     * @param actions     the actions
     * @param description the description
     */
    public static void voiceCommand(@Nullable ThingActions actions, @NonNull String description) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).voiceCommand(description);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Play.
     *
     * @param actions the actions
     */
    public static void play(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).play();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Pause.
     *
     * @param actions the actions
     */
    public static void pause(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).pause();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Next.
     *
     * @param actions the actions
     */
    public static void next(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).next();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Prev.
     *
     * @param actions the actions
     */
    public static void prev(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).prev();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Volume up.
     *
     * @param actions the actions
     */
    public static void volumeUp(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).volumeUp();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Volume down.
     *
     * @param actions the actions
     */
    public static void volumeDown(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).volumeDown();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Sets volume.
     *
     * @param actions the actions
     * @param level   the level
     */
    public static void setVolume(@Nullable ThingActions actions, @NonNull Integer level) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).setVolume(level);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Mute.
     *
     * @param actions the actions
     * @param mute    the mute
     */
    public static void mute(@Nullable ThingActions actions, @NonNull Boolean mute) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).mute(mute);
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }

    /**
     * Stop listening.
     *
     * @param actions the actions
     */
    public static void stopListening(@Nullable ThingActions actions) {
        if (actions instanceof YandexStationThingActions) {
            ((YandexStationThingActions) actions).stopListening();
        } else {
            throw new IllegalArgumentException("Instance is not a YandexStationThingActions class.");
        }
    }
}
