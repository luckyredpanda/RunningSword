const mongoose = require("mongoose");

const Schema = mongoose.Schema;

const UserSchema = new Schema({
    name: {
        type: String,
        required: true,
        unique:true
    },
    email: {
        type: String,
        required:true,
        unique:true
    },
    password: {
        type: String,
        required:true
    },
    avatar: {
        type: String
    },
    date: {
        type: Date,
        default: Date.now
    },
    games: [{
        gameID: {
            type: String,
            required:true,
        },
        isSingle: {
            type: Boolean,
            required:true,
        }
    }],
    survivalDays: {
        type: Number,
        default: 0,
    },
    survivalTime: {
        type: Number,
        default: 0,
    },
    survivalDistance: {
        type: Number,
        default: 0,
    },
    lastDay: {
        type: Date,
        default: Date.now
    },
    points: {
        type: Number,
        default: 0,
    },
    usedSafehouse: {
        type: Boolean,
        default: false,
    },
    comments:[{

        ownerID:{
            type:String,
            required:true,

        },
        commenterID:{
            type:String,
            required:true,

        },
        commenterName:String,
        content:{
            type:String,
            require:true,

        },
        date: {
            type:String,
        },
        commenterAvatar: String,
        rate:Number,
    }],
    tokens: {
        access: {
            type: String,
            required: true
        },
        refresh: {
            type: String,
            required: true
        }
    },
    coins: {
        type: Number,
        default: 0
    }
});

mongoose.model('users', UserSchema);
