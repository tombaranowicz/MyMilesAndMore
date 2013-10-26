
/*
 *  Generic require login routing middleware
 */

exports.requiresLogin = function (req, res, next) {
  if (!req.isAuthenticated()) {
    req.session.returnTo = req.originalUrl
    return res.redirect('/login')
  }
  console.log('go to next');
  next()
}

exports.requiresAdmin = function (req, res, next) {
   console.log('check ' + JSON.stringify(req.user.user_type));
  if (req.user.user_type!='admin') {
    return res.redirect('/login')
  }
  next()
}


/*
 *  User authorization routing middleware
 */

exports.user = {
  hasAuthorization : function (req, res, next) {
    if (req.profile.id != req.user.id) {
      req.flash('info', 'You are not authorized')
      return res.redirect('/users/'+req.profile.id)
    }
    next()
  }
}

/*
 *  Article authorization routing middleware
 */

exports.article = {
  hasAuthorization : function (req, res, next) {
    if (req.article.user.id != req.user.id) {
      req.flash('info', 'You are not authorized')
      return res.redirect('/articles/'+req.article.id)
    }
    next()
  }
}
