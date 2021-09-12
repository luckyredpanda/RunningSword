const mongoose = require("mongoose");

const Schema = mongoose.Schema;

const MapSchema = new Schema({
    userId: {
        type: mongoose.Types.ObjectId,
        required: true
    },
    createdAt: {
        type: Date,
        default: new Date()
    },
    startTime: {
        type: Date,
        required: true
    },
    endTime: {
        type: Date,
        required: true
    },
    startLocation: {
        type: Object,
        required: true
    },
    endLocation: {
        type: Object,
        required: true
    },
    route: {
        type: Array,
        required: true
    },
    distance: {
        meters: {
            type: Number,
            default: 0.0
        },
        hr: {
            type: String,
            default: '0 km'
        }
    },
    duration: {
        seconds: {
            type: Number,
            default: 0
        },
        hr: {
            type: String,
            default: '0 min'
        }
    }
});

mongoose.model('maps', MapSchema);
