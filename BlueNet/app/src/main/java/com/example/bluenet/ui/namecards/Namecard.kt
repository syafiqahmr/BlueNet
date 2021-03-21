package com.example.bluenet.ui.namecards

import android.media.Image

class Namecard(val id: String, val name: String, val company: String, val image: Int?, val industry: String, val role: String){

    constructor() : this("", "", "", 0, "", ""){

    }
}