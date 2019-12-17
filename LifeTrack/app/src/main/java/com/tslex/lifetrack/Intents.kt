package com.tslex.lifetrack

enum class Intents(private val action: String) {
//    INTENT_TRACKING_START   ("lifetrack.service.tracking.start"),
    INTENT_TRACKING_PAUSE   ("lifetrack.service.tracking.pause"),
    INTENT_TRACKING_RESUME  ("lifetrack.service.tracking.resume"),
    INTENT_TRACKING_STOP    ("lifetrack.service.tracking.stop"),

    INTENT_ADD_CP           ("lifetrack.service.add.cp"),
    INTENT_ADD_WP           ("lifetrack.service.add.wp"),

    INTENT_UI_PLACE_CP      ("lifetrack.service.tracking.ui.place.cp"),

    INTENT_UI_UPDATE_LOCATION        ("lifetrack.service.tracking.ui.update.location"),
    INTENT_UI_UPDATE_META        ("lifetrack.service.tracking.ui.update.meta"),

    INTENT_COMPASS_UPDATE   ("lifetrack.service.tracking.compass.update"),
    ;

    fun getAction(): String{
        return action
    }

    override fun toString(): String {
        return action
    }
}