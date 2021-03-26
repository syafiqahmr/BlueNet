package com.example.bluenet.ui.namecards

import android.media.Image

class Namecard(val name: String, val company: String, val image: String, val industry: String, val role: String, val linkedin: String){

    constructor() : this("", "", "", "", "", ""){

    }
}