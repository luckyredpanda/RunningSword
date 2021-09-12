const mongoose = require("mongoose");

const Schema = mongoose.Schema;

const FriendSchema = new Schema({
    friendId: {
        type: String,
        required: true
    },
    userId: {
        type: String,
        required: true
    },
    friendEmail: {
        type: String,
        required: true
    },
    userEmail: {
        type: String,
        required: true
    },
    friendName: {
        type: String,
        required:true
    },
    friendAvatar: {
        type: String,
        default:""
    },
    userName: {
        type: String,
        required:true
    },
    userAvatar: {
        type: String,
        default:""
    },
    lastSentence: {
        type: String
    },
    countingDays: {
        type: String
    },
    chattingFile: {
        type: String
    },
    days: {
        type: Number,
        default: 0,
    },
    points: {
        type: Number,
        default: 0,
    }
});

mongoose.model('friend', FriendSchema);