var async = require('async')

var public_api = require('../app/controllers/public_apis')

module.exports = function (app, passport) 
{
  app.get('/objects/get_tag_details/:tag_id', public_api.get_tag_details) //params tag_id
  app.post('/objects/add_tag', public_api.add_tag) //POST// params tag_id, name, description, link
  app.post('/objects/delete_tag', public_api.delete_tag) //POST// params tag_id
}