const mongoose = require("mongoose");

const Schema = mongoose.Schema;

const MessageSchema = new Schema({
    senderId: {
        type: String,
        required:true,
    },
    receiverId: {
        type: String,
        required:true
    },
    totalId: {
        type: String,
        required:true  
    },
    message: {
        type: String,
        required:true
    },
    time: {
        type: String,
        required:true
    },
    date: {
        type: Date,
        default:Date.now
    }
});

mongoose.model('message', MessageSchema);
