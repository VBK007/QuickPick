package com.example.quickpick.Model

class FCMResponse {
    var multicast_id:Long=0
    var sucess=0
    var failure=0
    var canoical_ids=0
    var results:List<FCMResult>?=null
    var message_id:Long=0

}