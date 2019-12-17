package com.tslex.lifetrack.domain

class PType {
    var id: Int = 0
    var type: String

    constructor(id: Int, type: String) {
        this.id = id
        this.type = type
    }

    constructor(type: String) {
        this.type = type
    }
}