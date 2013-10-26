var mongoose = require('mongoose')
  , TagObject = mongoose.model('TagObject')

exports.get_tag_details = function(req, res) { //GET// params tag_id
  console.log('get tag: '+req.params.tag_id);
  TagObject.findOne({'tag_id': req.params.tag_id}, function(err, object) {
    if (err){
      console.log('error ' +err);
      return res.send(404);
    } else if (!object) {
      console.log('no object');
     return res.send(404);
    } else {
      res.send({'object':object});
    }
  }); 
}

exports.add_tag = function(req, res) { //POST// params tag_id, name, description, link
  console.log('add ' + JSON.stringify(req.body));
  TagObject.findOne({'tag_id' : req.body.tag_id}, function(err, object) {
  if (err){
      console.log('error ' +err);
      return res.send({'error':err});
    } else if (object) {
      console.log('no object');
      return res.send({'error':'object_exists'});
    } else {
      var tagObject = new TagObject(req.body);
      tagObject.save(function (err) {
        if (err) res.send({'error':err});
        return res.send({'object':tagObject});
      });
    }
  });
}

exports.delete_tag = function(req, res) { //POST// params tag_id
  console.log('delete ' + JSON.stringify(req.body));
  TagObject.findOne({'tag_id' : req.body.tag_id}, function(err, object) {
    if (err){
      console.log('error ' +err);
      return res.send({'error':err});
    } else if (!object) {
      console.log('no object');
      return res.send({'error':'no object'});
    } else {
      object.remove(function(err) {
        if (err) res.send({'error':err});
        res.send({'object':req.body.tag_id});
      });
    }
  });
}