const mongoose = require("mongoose");
const moment = require("moment");

require('../models/Map');
const Map = mongoose.model("maps");

/**
 * returns the distance all participants of a game ran together in the period of the game
 * @param game
 * @returns {Promise<string>} summed up distance in km
 */
module.exports = async function(game) {
    let distance = 0.0;

    for (let i = 0; i < game.participants.length; i++) {
        let maps = await Map.find({userId: mongoose.Types.ObjectId(game.participants[i])});
        maps.map((e) => {
            if (moment(e.createdAt).isSame(moment(game.progress.lastDay), 'd')) {
                distance += e.distance.meters;
            }
        });
    }

    return (distance / 1000).toFixed(2);
}
