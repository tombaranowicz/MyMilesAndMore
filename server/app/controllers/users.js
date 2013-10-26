
/**
 * Module dependencies.
 */

var mongoose = require('mongoose')
  , User = mongoose.model('User')
  , TagObject = mongoose.model('TagObject')
  , Feedback = mongoose.model('Feedback')
  , utils = require('../../lib/utils')

var login = function (req, res) {
  console.log('goto: ' + req.session.returnTo);
  if (req.session.returnTo) {
    res.redirect(req.session.returnTo)
    delete req.session.returnTo
    return
  }
  res.redirect('/objects')
}

exports.signin = function (req, res) {}

/**
 * Auth callback
 */

exports.authCallback = login

/**
 * Show login form
 */

exports.login = function (req, res) {
  res.render('users/login', {
    title: 'Login',
    message: req.flash('error')
  })
}

/**
 * Show sign up form
 */

exports.signup = function (req, res) {
  res.render('users/signup', {
    title: 'Sign up',
    user: new User()
  })
}

/**
 * Logout
 */

exports.logout = function (req, res) {
  req.logout()
  res.redirect('/login')
}

/**
 * Session
 */

exports.session = login

/**
 * Create user
 */

exports.create = function (req, res) {
  console.log(JSON.stringify(req.body));
  var user = new User(req.body);
  user.provider = 'local'
  user.save(function (err) {
    if (err) {
      console.log(err);
      return res.render('users/signup', {
        errors: utils.errors(err.errors),
        user: user,
        title: 'Sign up'
      })
    }

    // manually login the user once successfully signed up
    req.logIn(user, function(err) {
      if (err) return next(err)
      return res.redirect('/')
    })
  })
}

/**
 *  Show profile
 */

// exports.show = function (req, res) {
//   var user = req.profile
//   console.log('user'+user);
//   res.render('users/show', {
//     title: user.name,
//     user: user
//   })
// }

/**
 * Find user by id
 */

exports.user = function (req, res, next, id) {
  User
    .findOne({ _id : id })
    .exec(function (err, user) {
      if (err) return next(err)
      if (!user) return next(new Error('Failed to load User ' + id))
      req.profile = user
      next()
    })
}

exports.user_index = function (req, res) {
  var user = req.user
  if (!user) return res.redirect('/login')
  TagObject.find({'_id': { $in: req.user.objects}}, function(err, objects) {
    console.log('user ' + user);
    if (err) return res.render('500');
    console.log('manage ' + JSON.stringify(objects));
    res.render('users/objects', {
      objects: objects
    })
  });
}

exports.feedbacks = function (req, res) {
  var user = req.user
  if (!user) return res.redirect('/login')
  Feedback.find({'_id': { $in: req.user.feedbacks}}, function(err, objects) {
    console.log('user ' + user);
    if (err) return res.render('500');
    console.log('manage ' + JSON.stringify(objects));
    res.render('users/feedbacks', {
      objects: objects
    })
  });
}

exports.statistics = function (req, res) {
  var user = req.user
  if (!user) return res.redirect('/login')
  res.render('users/statistics', {})
}