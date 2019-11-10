package com.tslex.radio;

import androidx.annotation.NonNull;

public enum IntentActions {

    INTENT_PLAYER_PLAYING               ("radio.player.playing"),
    INTENT_PLAYER_BUFFERING             ("radio.player.buffering"),
    INTENT_PLAYER_STOPPED               ("radio.player.stopped"),

    INTENT_PLAYER_UPDATE           ("radio.player.update"),

    INTENT_PLAYER_MUTE               ("radio.player.mute"),
    INTENT_PLAYER_UNMUTE          ("radio.player.unmute"),

    INTENT_PLAYER_BUFFERING_PROGRESS    ("radio.player.buffering.progress"),

    INTENT_UI_STOP                      ("radio.ui.stop"),

    INTENT_META_UPDATE                   ("radio.meta.update"),
    INTENT_ANIM_PLAY                ("radio.anim.play"),
    ;

    private final String action;

    IntentActions(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
