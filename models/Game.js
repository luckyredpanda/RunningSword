const mongoose = require("mongoose");

const Schema = mongoose.Schema;

const GameSchema = new Schema({
    userId: {
        type: mongoose.Types.ObjectId,
        required: true
    },
    participants: [mongoose.Types.ObjectId],
    name: {
        type: String,
        required: true
    },
    distance: {
        type: Number,
        default: 0.0
    },
    progress: {
        distance: {
            type: Number,
            default: 0.0
        },
        days: {
            type: Number,
            default: 0
        },
        lastDay: {
            type: Date,
            default: Date.now
        }
    },
    started_at: {
        type: Date,
        default: Date.now
    },
    status: {
        type: Number,
        default: 0 //0 is running, 1 is success, 2 is fail
    },
});

mongoose.model('games', GameSchema);
