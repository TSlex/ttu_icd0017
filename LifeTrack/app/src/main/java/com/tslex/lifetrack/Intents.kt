package com.tslex.lifetrack

enum class Intents(private val action: String) {
    INTENT_TRACKING_START   ("lifetrack.service.tracking.start"),
    INTENT_TRACKING_PAUSE   ("lifetrack.service.tracking.start"),
    INTENT_TRACKING_STOP    ("lifetrack.service.tracking.start"),

    INTENT_TRACK_SAVE       ("lifetrack.service.tracking.start"),
    INTENT_TRACK_LOAD       ("lifetrack.service.tracking.start"),

    INTENT_PLACE_WAYPOINT   ("lifetrack.service.tracking.start"),
    INTENT_PLACE_CHECKPOINT ("lifetrack.service.tracking.start"),

    INTENT_UI_UPDATE        ("lifetrack.service.tracking.start"),
    ;

    fun getAction(): String{
        return action
    }

    override fun toString(): String {
        return action
    }
}