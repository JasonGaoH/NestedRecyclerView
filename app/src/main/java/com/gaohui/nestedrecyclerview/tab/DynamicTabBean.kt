package com.gaohui.nestedrecyclerview.tab

data class TabIcon(var normal:String = "",
                   var clicked:String = "")


data class DynamicTabBean(val title: String,
                        val desc: String? = "",
                        val icon: TabIcon? = null)