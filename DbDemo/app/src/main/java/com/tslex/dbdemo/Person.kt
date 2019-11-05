package com.tslex.dbdemo

class Person {

    var id: Int = 0
    var firstName: String = ""
    var lastNamae: String = ""

    constructor(firstName: String, lastNamae: String) {
        this.firstName = firstName
        this.lastNamae = lastNamae
    }

    constructor(id: Int, firstName: String, lastNamae: String) {
        this.id = id
        this.firstName = firstName
        this.lastNamae = lastNamae
    }
}