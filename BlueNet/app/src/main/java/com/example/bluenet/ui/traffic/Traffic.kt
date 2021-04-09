package com.example.bluenet.ui.traffic

data class Traffic(val name: String, val traffic: String, val people: Int) {

    constructor() : this("", "", 0){

    }
}
