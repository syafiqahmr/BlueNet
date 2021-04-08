package com.example.bluenet.ui.namecards

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.bumptech.glide.load.engine.Resource
import com.example.bluenet.R
import java.util.regex.Matcher
import java.util.regex.Pattern

class NamecardDetailsExtraction (textArr : ArrayList<String>, val context: Context) {

    private var textArr:ArrayList<String> = textArr

    fun extractName(): String? {
        for (text in textArr){
            val NAME_REGEX = "\\b([A-ZÀ-ÿ][-,a-z. ']+[ ]*)+"
            val p: Pattern = Pattern.compile(NAME_REGEX, Pattern.MULTILINE)
            val m: Matcher = p.matcher(text)
            if (m.find()) {
                return text
            }
        }

        Log.i("extracting", "name")
        return null
    }

    fun extractPhoneNumber(): String? {
        for (text in textArr){
            val PHONE_REGEX = "[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*\$"
            val p: Pattern = Pattern.compile(PHONE_REGEX)
            val m: Matcher = p.matcher(text)
            if (m.find()) {
                return m.group()
            }
        }
        Log.i("extracting", "phonenumber")

        return null
    }

    fun extractLinkedin(): String? {
        for (text in textArr){
            val LINKEDIN_REGEX = "linkedin\\.com\\/in\\/.*\$"
            val p: Pattern = Pattern.compile(LINKEDIN_REGEX)
            val m: Matcher = p.matcher(text)
            if (m.find()) {
                return text
            }
        }
        Log.i("extracting", "linkedin")
        return null
    }

    fun extractRole(): String? {
        var roles = context.resources.getStringArray(R.array.roles )
        for (text in textArr){
            for (role in roles){
                val ROLE_REGEX = "^(.*?(\\b$role\\b)[^\$]*)\$"
                val p: Pattern = Pattern.compile(ROLE_REGEX, Pattern.CASE_INSENSITIVE)
                val m: Matcher = p.matcher(text)
                if (m.find()) {
                    return role
                }
            }
        }
        Log.i("extracting", "role")
        return null
    }

    fun extractIndustry(): String? {
        var industries = context.resources.getStringArray(R.array.industries)
        for (text in textArr){
            for (industry in industries){
                val ROLE_REGEX = "^(.*?(\\b$industry\\b)[^\$]*)\$"
                val p: Pattern = Pattern.compile(ROLE_REGEX, Pattern.CASE_INSENSITIVE)
                val m: Matcher = p.matcher(text)
                if (m.find()) {
                    return industry
                }
            }
        }
        Log.i("extracting", "role")
        return null
    }

}