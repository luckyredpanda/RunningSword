const express = require("express");
const mongoose = require("mongoose");
const router = express.Router();
const passport = require("passport");
const { body, validationResult } = require('express-validator');
const calcDistance = require("../helpers/CalculateDistance");
const calcTime = require("../helpers/CalculateTime");
const moment = require("moment");


const jsonParser = express.json()

require('../models/Map');
const Map = mongoose.model("maps");
require('../models/User');
const User = mongoose.model("users");


router.post("/", jsonParser, passport.authenticate('bearer', {session:false}),
    async (req, res, next) => {

    // TODO split at 00:00

    let map = new Map();
    map.userId = mongoose.Types.ObjectId(req.user._id);
    map.startTime = req.body.start_time;
    map.endTime = req.body.end_time;
    map.startLocation = req.body.start_location;
    map.endLocation = req.body.end_location;
    //map.route = req.body.route;

    let distance = calcDistance(req.body.route);
    map.distance.meters = distance;
    map.distance.hr = (distance / 1000).toFixed(2) + ' km';

    let duration = calcTime(map.startTime, map.endTime);
    map.duration.seconds = duration.asSeconds();
    map.duration.hr = Math.ceil(duration.asMinutes()) + ' minutes'

    await map.save();

    let coins = parseInt((distance / 20).toFixed(0));
    let user = await User.findOne({_id: map.userId});
    user.coins += coins;

    // update if today or yesterday
    if (moment().isSame(user.lastDay, 'd')
        || moment().subtract(1, 'days').isSame(user.lastDay, 'd'))
    {
        user.survivalDistance += map.distance.meters;
        user.survivalTime += map.duration.seconds;
    }

    user.save();

    return res.json({
        status: 201,
        message: 'Congratulations!\nYou have run ' + map.distance.hr + ' in ' + map.duration.hr + ' and earned ' + coins + ' coins!',
        value: coins
    });
});


module.exports = router;
