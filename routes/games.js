const express = require("express");
const mongoose = require("mongoose");
const router = express.Router();
const passport = require("passport");
const { body, validationResult } = require('express-validator');
const moment = require("moment");
const participantsDistanceSum = require("../helpers/SumDistance");


const jsonParser = express.json()

require('../models/Game');
const Game = mongoose.model("games");

router.post("/searchByGameId", jsonParser,
    async (req, res, next) => {
        let game = await Game.findOne({ '_id': req.body._id });

        if (!game) {
            return res.status(404).json("game doesn't exist");
        } else {

            let distance = await participantsDistanceSum(game);

            // check game yesterday
            if (moment().subtract(1, 'days').isSame(game.progress.lastDay, 'd')) {
                if (distance >= game.distance) {
                    game.progress.days++;
                    game.progress.lastDay = Date.now();
                    distance = await participantsDistanceSum(game);
                    await game.save();
                }
            }

            game.progress.distance = distance; // don't save

            res.json(game);
        }
    }
);
router.get("/", jsonParser, passport.authenticate('bearer', {session:false}),
    async (req, res, next) => {

        let games = await Game.find({userId: mongoose.Types.ObjectId(req.user._id)});
        for (let i = 0; i < games.length; i++) {
            games[i].progress.period = moment(games[i].created_at)
                .add(games[i].period.split(' ')[0], 'days')
                .fromNow();
            games[i].progress.distance = await participantsDistanceSum(games[i].participants) + ' / ' + games[i].distance;
        }

        return res.json(games.reverse());
});

router.post("/", jsonParser, passport.authenticate('bearer', {session:false}),
    async (req, res, next) => {

    // TODO split records over day change

    let game = new Game();
    game.userId = mongoose.Types.ObjectId(req.user._id);
    game.participants = req.body.participants;
    game.name = req.body.name;
    game.period = req.body.period;
    game.distance = req.body.distance;

    await game.save().then(game => res.json(game));

    });
    router.post("/updateParticipant", jsonParser,
    async (req, res, next) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ erros: errors.array() });
        }
        const participantArray = [...req.body.participants];
        Game.findOneAndUpdate(
            { "_id": req.body._id },
            {
                $set: {
                    participants: [...req.body.participants]
                }
            },
            { overwrite: true },
            function (err, resp) {
                if (err) throw err;
                if (resp) {

                } else {
                    return res.status(403).json({ success: false, message: 'Failed to change' + req.body._id + "new Email" + req.body.email });
                }
            }
        ).then(game => res.json(game))


    }
);


module.exports = router;
