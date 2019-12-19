package com.tslex.lifetrack

enum class Intents(private val action: String) {
//    INTENT_TRACKING_START   ("lifetrack.service.tracking.start"),
    INTENT_TRACKING_PAUSE   ("lifetrack.service.tracking.pause"),
    INTENT_TRACKING_RESUME  ("lifetrack.service.tracking.resume"),
    INTENT_TRACKING_STOP    ("lifetrack.service.tracking.stop"),

    INTENT_ADD_CP           ("lifetrack.service.add.cp"),
    INTENT_ADD_WP           ("lifetrack.service.add.wp"),

    INTENT_UI_PLACE_CP      ("lifetrack.ui.place.cp"),
    INTENT_UI_PLACE_WP      ("lifetrack.ui.place.wp"),

    INTENT_UI_UPDATE_LOCATION        ("lifetrack.ui.update.location"),
    INTENT_UI_UPDATE_META        ("lifetrack.ui.update.meta"),

    INTENT_LOAD_SESSION      ("lifetrack.ui.load.session"),

    INTENT_COMPASS_UPDATE   ("lifetrack.service.tracking.compass.update"),
    ;

    fun getAction(): String{
        return action
    }

    override fun toString(): String {
        return action
    }
}