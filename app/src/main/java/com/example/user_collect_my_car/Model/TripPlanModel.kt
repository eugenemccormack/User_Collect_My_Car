package com.example.user_collect_my_car.Model

class TripPlanModel {

    var user: String? = null

    var driver: String? = null

    var driverInfoModel: DriverInfoModel? = null

    var userModel: UserModel? = null

    var origin: String? = null

    var originString: String? = null

    var destination: String? = null

    var destinationString: String? = null

    var durationPickup: String? = null

    var durationDestination: String? = null

    var distancePickup: String? = null

    var distanceDestination: String? = null

    var currentLat = 0.0

    var currentLng = 0.0

    var done = false

    var isCancelled = false

    var time: String? = null

    var collectionPhotos: CollectionPhotos? = null

/*    var collectionPhotos: String? = null

    var dropOffPhotos: String? = null*/

    var collectionNumber: String? = null

    var distanceValue = 0

    var durationValue = 0

    var estimatedPrice = 0.0

    var brakingCount = 0

    var newDriverRating = 1

    var oldDriverRating = 1

    var distanceText: String?=""

    var durationText:String?=""

    var tripTime: String = ""





}