var express = require('express')
  , fs = require('fs')
  , passport = require('passport')

var env = process.env.NODE_ENV || 'development'
  , config = require('./config/config')[env]
  , mongoose = require('mongoose')

// db connection
mongoose.connect(config.db)

// models
var models_path = __dirname + '/app/models'
fs.readdirSync(models_path).forEach(function (file) {
  if (~file.indexOf('.js')) require(models_path + '/' + file)
})

// passport config
require('./config/passport')(passport, config)

var app = express()
// express settings
require('./config/express')(app, config, passport)

// routes
require('./config/routes')(app, passport)

// Start the app by listening on <port>
var port = process.env.PORT || 8000
app.listen(port)
console.log('My Miles And More started on port '+port)

// expose app
exports = module.exports = app
