const express = require("express");
const mongoose = require("mongoose");
const router = express.Router();
const bcrypt = require('bcrypt');
const passport = require("passport");
const crypto = require('crypto');
const { body, validationResult } = require('express-validator');


const jsonParser = express.json()

require('../models/Friend');
const Friend = mongoose.model("friend");
require('../models/User');
const User = mongoose.model("users");


router.post("/allfriends", jsonParser, async (req, res) => {
    Friend.find({ 'userId': req.body.userId })
        .then(async resault => {
                if(resault.length==0)
                    return res.status(403).json({ success: false, message: 'No friend exists' });
                for (let i = 0; i < resault.length; i++) {
                    let user = await User.findOne({_id: mongoose.Types.ObjectId(resault[i].friendId)});
                    if (user) {
                        resault[i].days = user.survivalDays;
                        resault[i].points = user.points;
                    }
                }
                res.json(resault);
    })
});
router.post("/allUserFriends", jsonParser, async (req, res) => {
    Friend.find({ 'friendId': req.body.friendId })
        .then(resault => {
            if(resault.length==0)
                return res.status(403).json({ success: false, message: 'No friend exists' });
            res.json(resault);
        })
});
//search one friend with Id
router.post("/searchWithId", jsonParser,
    async (req, res, next) => {
        Friend.findOne({ friendId: req.body.friendId,userId:req.body.userId })
            .then(friend => {
                if (!friend) {
                    return res.status(404).json("Friend doesn't exist");
                }
                res.json(friend);
        })
        
});


router.post("/add", jsonParser,
    async (req, res, next) => {
        let friend = await Friend.findOne({ friendId: req.body.friendId, userId: req.body.userId });
        console.log(req.body)
        if (!friend) {
            friend = new Friend();
            friend.friendId = req.body.friendId;
            friend.userId = req.body.userId;
            friend.friendEmail = req.body.friendEmail;
            friend.userEmail = req.body.userEmail;
            friend.friendName = req.body.friendName;
            friend.friendAvatar = req.body.friendAvatar;
            friend.userName = req.body.userName;
            friend.userAvatar = req.body.userAvatar;
            friend.save()
                .then(friend => {
                    res.json(friend)
                })
        } else {
            return res.status(404).json("Friend already exist");
        }
        
    
});
router.post("/updateFriendAvatar", jsonParser,
    async (req, res, next) => {
        Friend.updateMany(
            { friendId: req.body.friendId },
            {
                $set: {
                    friendAvatar:req.body.friendAvatar
                }
            }
        ).then(res.json("success"))
    }
)
router.post("/updateFriendName", jsonParser,
    async (req, res, next) => {
        Friend.updateMany(
            { friendId: req.body.friendId },
            {
                $set: {
                    friendName:req.body.friendName
                }
            }
        ).then(res.json("success"))
    }
)
router.post("/updateFriendEmail", jsonParser,
    async (req, res, next) => {
        Friend.updateMany(
            { friendId: req.body.friendId },
            {
                $set: {
                    friendEmail:req.body.friendEmail
                }
            }
        ).then(res.json("success"))
    }
)
router.post("/updateUserAvatar", jsonParser,
    async (req, res, next) => {
        Friend.updateMany(
            { userId: req.body.userId },
            {
                $set: {
                    userAvatar:req.body.userAvatar
                }
            }
        ).then(res.json("success"))
    }
)
router.post("/updateUserName", jsonParser,
    async (req, res, next) => {
        Friend.updateMany(
            { userId: req.body.userId },
            {
                $set: {
                    userName:req.body.userName
                }
            }
        ).then(res.json("success"))
    }
)
router.post("/updateUserEmail", jsonParser,
    async (req, res, next) => {
        Friend.updateMany(
            { friendId: req.body.friendId },
            {
                $set: {
                    userEmail:req.body.userEmail
                }
            }
        ).then(res.json("success"))
    }
)
router.post("/updateSentence", jsonParser,
     async (req, res, next) => {
        Friend.findOneAndUpdate(
            { friendId: req.body.friendId, userId:req.body.userId},
            {
                $set: {
                    lastSentence: req.body.lastSentence
                }
            }
        ).then(friend=>res.json(friend))
});
//delete friend with friendId and userId
router.post("/delete", jsonParser, async (req, res, next) => {
    Friend.findOneAndDelete({ friendId: req.body.friendId, userId: req.body.userId })
        .then(res.json('delete success'))
    
});
router.delete("/delete/:id",jsonParser ,async (req, res,next) => {
    Friend.findOneAndDelete({ friendId: req.params.friendId})
        .then(
            res.json("delete success")
        )
        .catch(err => res.status(404).json('delete fail'))
})


module.exports = router;
