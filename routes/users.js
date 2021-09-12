const express = require("express");
const mongoose = require("mongoose");
const router = express.Router();
const bcrypt = require('bcrypt');
const passport = require("passport");
const crypto = require('crypto');
const moment = require("moment")


const jsonParser = express.json()

const multer = require('multer');
const storage = multer.diskStorage({
    destination: function(req,file,cb){
        cb(null, './uploads/')
    },
    filename : function(req,file,cb){
        cb(null,new Date().toISOString() + file.originalname);
    }
});

const fileFilter = (req, file, cb)=>{
    if (file.mimetype === 'image.jpg' || file.mimetype === 'image.jpeg'){
        cb(null, true);
    }else{
        cb(null,false);
    }

}
const upload = multer({ storage: storage });


const { body, validationResult } = require('express-validator');


require('../models/User');
require('../models/Map');
require('../models/Game');
require('../models/Friend');
require('../models/Message');
const User = mongoose.model("users");
const Map = mongoose.model("maps");
const Game = mongoose.model("games");
const Message = mongoose.model("message");
const Friend = mongoose.model("friend");


router.get("/searchbyid/:id", passport.authenticate('bearer', {session:false}), async (req, res) => {
    let user=User.findOne({'_id':req.params.id});
    if(user==null) res.status(404).send({msg:"No user"});
    res.send(await User.findOne({ '_id': req.params.id }));
});

router.get("/home", passport.authenticate('bearer', {session:false}), async (req, res) => {

    let distanceToday = 0.0;
    let distanceYesterday = 0.0;
    let maps = await Map.find({userId: mongoose.Types.ObjectId(req.user._id)});
    maps.map((e) => {
        if (moment(e.createdAt).isSame(moment(), 'day')) {
            distanceToday += e.distance.meters;
        } else if (moment().subtract(1, 'days').isSame(e.createdAt, 'd')) {
            distanceYesterday += e.distance.meters;
        }
    });

    let user = await User.findOne({_id: mongoose.Types.ObjectId(req.user._id)});

    // find target distance
    let targetDistance = 100000; // in km
    for (let i = 0; i < user.games.length; i++) {
        if (user.games[i].isSingle) {
            let g = await Game.findOne({_id: mongoose.Types.ObjectId(user.games[i].gameID)});
            targetDistance = g.distance;
            break;
        }
    }

    // check if user met goal yesterday
    if (moment().subtract(1, 'days').isSame(user.lastDay, 'd')) {
        if (distanceYesterday >= targetDistance * 1000) { // compare in meters
            user.survivalDays++;
            user.points += targetDistance;
        }
        user.usedSafehouse = false;
        await user.save();
    } else if (!moment().isSame(user.lastDay, 'd')) { // not today (and not yesterday): reset
        user.survivalDistance = 0.0;
        user.survivalDays = 0;
        user.survivalTime = 0;
        user.usedSafehouse = false;
        user.points = 0;
        await user.save();
    }

    res.send({
        distance: user.survivalDistance,
        days: user.survivalDays,
        distance_today: distanceToday,
        period: moment.utc(user.survivalTime * 1000).format('HH:mm'),
        coins: user.coins,
        safehouse: user.usedSafehouse,
        name: user.name,
        userAvatar: user.avatar,
        points: user.points,
        targetDistance: targetDistance * 1000
    });
});

router.post("/paySafehouse", passport.authenticate('bearer', {session:false}), async (req, res) => {

    let user = await User.findOne({_id: mongoose.Types.ObjectId(req.user._id)});
    if (user.coins >= 500) {
        user.coins -= 500;
        user.lastDay = new Date();
        user.usedSafehouse = true;
        user.save();
        res.send({});
    } else {
        res.status(400).send();
    }
});


//search a user with his email
router.post("/searchByEmail", jsonParser,
    body('email').isEmail(),
    async (req, res, next) => {
        let user = await User.findOne({ 'email': req.body.email });
        if (!user) {
            return res.status(404).json("user doesn't exist");
        } else {
            res.json(user);
        }
    }
);
//search one user with name
router.post("/searchByName", jsonParser,
    async (req, res, next) => {
        User.findOne({ name: req.body.name })
            .then(user => {
                if (!user) {
                    return res.status(404).json("user doesn't exist");
                }
                res.json(user);
        })

});
//change name
router.post("/changename", jsonParser,
    body('email').isEmail(),
    async (req, res, next) => {
        User.findOne({ name: req.body.name })
        .then((user) => {
            if (user) {
                return res.status(404).json({success: false, message: 'duplicate name error'})
            }else{
                User.findOneAndUpdate(
                    { _id: req.body.id },
                    {
                        $set: {
                            "name": req.body.name,
                        }
                    },
                    function(err,resp){
                        if(err) throw err;
                        if(resp) {
                            return res.send({
                                msg:'Name change successfully'
                            });
                        }else{
                            return res.status(404).json({ success: false, message: 'duplicate name error'});
                        }
                    }
                ).then(user=>res.json(user));
            }
        })
    }

);
router.post("/updateUrl", jsonParser,
    async (req, res, next) => {
        User.findOneAndUpdate(
            { _id: req.body._id },
            {
                $set: {
                    avatar: req.body.avatar,
                }
            }
        ).then(user=>res.json(user))
    }
);
//update user's email and password
//bug: set name ="req.body.name"rather than data load in
router.post("/update/:id", async (req, res) => {

    User.findOneAndUpdate(
        { _id: req.params.id },
        {
            $set: {
                name: req.body.name,
                email: req.body.email
            }
        }, {overwrite: true}
    ).then(res.json("update success"));

});
//change password of a user
router.post("/change", jsonParser,
    body('password').isLength({min:5}),
    async (req, res, next) => {

    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }
    var pwd;
    bcrypt.genSalt(10, (err, salt) => {
        bcrypt.hash(req.body.password, salt, (err, hash) => {
            if (err) throw err;
                User.findOneAndUpdate(
                        { "email": req.body.email},
                        {
                            $set: {
                                "email": req.body.email,
                                "password":hash
                            }
                        },
                        {overwrite:true}
                    ).then(res.json("change success"))

        });
    });

    }
);
//Search a user by his ID
router.post("/searchUserbyID", jsonParser,
    async (req, res, next) => {
        let user = await User.findOne({ '_id': req.body._id});
        if (user === null) {
            res.json("no user exists");
        } else {
            res.json({
                '_id': user.id,
                'email': user.email,
                'name': user.name,
                'avatar': user.avatar
            });
        }
    });
router.post("/register", jsonParser,
    body('email').isEmail(),
    body('password').isLength({ min: 5 }), (req, res) => {

        User.findOne({ email: req.body.email })
            .then((user) => {
                if (user) {
                    return res.status(404).json("User already exists")
                } else {

                    const newUser = new User();
                    newUser.avatar = "";
                    newUser.email = req.body.email;
                    newUser.password = req.body.password;
                    newUser.name = 'SwordRunner#' + newUser._id.toString().substr(20);
                    const buf = crypto.randomBytes(32);
                    newUser.tokens.access = buf.toString('hex');
                    const buf2 = crypto.randomBytes(32);
                    newUser.tokens.refresh =buf.toString('hex');

                    bcrypt.genSalt(10, (err, salt) => {
                        bcrypt.hash(newUser.password, salt, (err, hash) => {
                            if (err) throw err;

                            newUser.password = hash;
                            newUser.save()
                                .then(user => res.json(user))

                        });
                    });
                }
            })

    });
router.post("/login", jsonParser,
    body('email').isEmail(),
    body('password').isLength({ min: 5 }), async (req, res, next) => {

    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }

    let user = await User.findOne({'email': req.body.email});

    if (user === null) { // Account doesn't exsist
        return res.status(404).json("Account doesn't exist")
    } else {  // login
        bcrypt.compare(req.body.password, user.password, function(err, resp) {
            if (err) throw err;

            if (resp) {
                return res.send({
                    id: user._id,
                    access_token: user.tokens.access,
                    refresh_token: user.tokens.refresh
                });
            } else {
                return res.status(403).json({success: false, message: 'Wrong credentials'});
            }
        });
    }

    throw 'Something went wrong';
});
//change email of a user
router.post("/changeEM", jsonParser,
    body('email').isEmail(),

    async (req, res, next) => {
    
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }
    User.findOne({ email: req.body.email })
    .then((user) => {
        if (user) {
            return res.status(404).json("User already exists")
        } else{
            User.findOneAndUpdate(
                { "_id": req.body._id},
                {
                    $set: {
                        email: req.body.email,
                    }
                },
                {overwrite:true},
                function(err,resp){
                    if(err) throw err;
                    if(resp) {
                        return res.send({
                            msg:'email change successfully'
                        });
                    }else{
                        return res.status(403).json({ success: false, message: 'Failed to change'+req.body._id+"new Email"+req.body.email});
                    }
                }
            )
        }

    }
    )
}
);

//insert a game
router.post("/createGame", jsonParser,
    body("games").isLength({ min: 0}),
    async (req, res, next) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ erros: errors.array() });
        }
        const gameArray = [...req.body.games];
        User.findOneAndUpdate(
            { "_id": req.body._id },
            {
                $set: {
                    games: [...req.body.games]
                }
            },
            { overwrite: true },
            function (err, resp) {
                if (err) throw err;
                if (resp) {
                    return res.send({
                        msg: ' change successfully' + gameArray
                    });
                } else {
                    return res.status(403).json({ success: false, message: 'Failed to change gamelist' });
                }
            }
        )


    }
);


//insert a comment
router.post("/writecomment", jsonParser,
    body("comments").isLength({ min: 1 }),
    async (req, res, next) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ erros: errors.array() });
        }
        const commentArray = [...req.body.comments];
        User.findOneAndUpdate(
            { "_id": req.body._id },
            {
                $set: {
                    comments: [...req.body.comments]
                }
            },
            { overwrite: true },
            function (err, resp) {
                if (err) throw err;
                if (resp) {
                    return res.send({
                        msg: ' change successfully' + commentArray
                    });
                } else {
                    return res.status(403).json({ success: false, message: 'Failed to change' + req.body._id + "new Email" + req.body.email });
                }
            }
        )


    }

);

//upload image
router.post("/upload", upload.single('avatar'), (req, res) => {
    console.log(req.file)
    res.json(req.file.filename);
});

router.delete("/deleteaccount", passport.authenticate('bearer', {session:false}), async (req, res) => {

    await Map.deleteMany({userId: mongoose.Types.ObjectId(req.user._id)});
    await Message.deleteMany({'senderId': mongoose.Types.ObjectId(req.user._id)});
    await Message.deleteMany({'receiverId': mongoose.Types.ObjectId(req.user._id)});
    await Friend.deleteMany({'friendId': mongoose.Types.ObjectId(req.user._id)});
    await Friend.deleteMany({'userId': mongoose.Types.ObjectId(req.user._id)});
    await User.deleteOne({'_id': mongoose.Types.ObjectId(req.user._id)});
    res.json();

});


module.exports = router;
