const express = require('express');
const logger = require('morgan');
const mongoose = require("mongoose");
const passport = require("passport");
const BearerStrategy = require('passport-http-bearer').Strategy;



const indexRouter = require('./routes/index');
const usersRouter = require('./routes/users');
const friendRouter = require('./routes/friend');
const messageRouter = require('./routes/message')
const mapsRouter = require('./routes/maps');
const gamesRouter = require('./routes/games');



const app = express();


//connect to mongoose
mongoose.connect("mongodb://mongodb/swordrunner")
    .then(()=> {
        console.log("MongoDB connected...");
    })
    .catch(err => {
        console.log(err)
    });
mongoose.set('useFindAndModify', false)

app.use(logger('dev'));


// routes
app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/friend', friendRouter);
app.use('/message', messageRouter);
app.use('/maps', mapsRouter);
app.use('/games', gamesRouter);



app.use(express.json());


passport.use(new BearerStrategy(
    function(token, done) {
        const User = mongoose.model("users");
        User.findOne({'tokens.access':token}, function (err, user) {
            if (err) { return done(err); }
            if (!user) { return done(null, false); }
            return done(null, user, { scope: 'all' });
        });
    }
));

//app.use(express.static(__dirname + '/uploads'));
app.use('/uploads',express.static(__dirname+'/uploads'));
// error handler
app.use(function (err, req, res, next) {
    const message = req.app.get('env') === 'development' ? err.message : 'error';
    res.status(err.status || 500);
    res.json({status:err.status,message:message});
});
// 404 handler
app.use(function (req, res, next) {
    res.status(404);
    res.json({status:404,message:'Route not found!'});
});


module.exports = app;
