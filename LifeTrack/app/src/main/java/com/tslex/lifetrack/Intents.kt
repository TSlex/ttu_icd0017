package com.tslex.lifetrack

enum class Intents(private val action: String) {
//    INTENT_TRACKING_START   ("lifetrack.service.tracking.start"),
    INTENT_TRACKING_PAUSE   ("lifetrack.service.tracking.pause"),
    INTENT_TRACKING_RESUME  ("lifetrack.service.tracking.resume"),
    INTENT_TRACKING_STOP    ("lifetrack.service.tracking.stop"),

    INTENT_TRACK_SAVE       ("lifetrack.service.tracking.save"),
    INTENT_TRACK_LOAD       ("lifetrack.service.tracking.load"),

    INTENT_PLACE_WAYPOINT   ("lifetrack.service.tracking.add.waypoint"),
    INTENT_PLACE_CHECKPOINT ("lifetrack.service.tracking.add.checkpoint"),

    INTENT_UI_UPDATE        ("lifetrack.service.tracking.ui.update"),

    INTENT_COMPASS_UPDATE   ("lifetrack.service.tracking.compass.update"),
    ;

    fun getAction(): String{
        return action
    }

    override fun toString(): String {
        return action
    }
}