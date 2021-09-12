const express = require("express");
const mongoose = require("mongoose");
const router = express.Router();
const bcrypt = require('bcrypt');
const passport = require("passport");
const crypto = require('crypto');
const { body, validationResult } = require('express-validator');


const jsonParser = express.json()

require('../models/Message');
const Message = mongoose.model("message");

//delete all message
router.post("/delete", jsonParser, async (req, res, next) => {
    Message.deleteMany({ totalId: req.body.totalId})
        .then(res.json('delete success'))
    
});
//get all message
router.post("/allMessages", jsonParser, async (req, res) => {
    Message.find({ totalId: req.body.totalId})
        .then(resault => {
            if(resault.length==0)
                return res.status(403).json({ success: false, message: 'No Message exists' });
            res.json(resault);
    })
});
//get the latest message
router.post("/lastMessage", jsonParser, async (req, res) => {
    Message.find({ totalId: req.body.totalId })
        .sort({ _id: -1 })
        .limit(1)
        .then(resault => {
            if (!resault)
                return res.status(403).json({ success: false, message: 'No Message exists' });
            res.json(resault);
    })
})
//add one message
router.post("/add", jsonParser,
    async (req, res, next) => {    
        message = new Message();
        message.senderId = req.body.senderId;
        message.receiverId = req.body.receiverId;
        message.message = req.body.message;
        message.time = req.body.time;
        message.totalId = req.body.totalId;
        message.save()
            .then(message => {
                res.json(message)
            })   
});

module.exports = router;
