package com.example.angelseaproject.data

import android.app.Application

class LoaclData:Application() {
    companion object{
        var Operations = ArrayList<Operation>(500)
        var allOperations = ArrayList<Operation>(500)
        var User: User = User("","","","")
    }
}
