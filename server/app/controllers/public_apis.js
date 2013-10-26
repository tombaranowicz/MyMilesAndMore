var mongoose = require('mongoose')
  , TagObject = mongoose.model('TagObject')
  , GCM = require('gcm').GCM;

exports.get_tag_details = function(req, res) { //GET// params tag_id
  console.log('get tag: '+req.params.tag_id);
  TagObject.findOne({'tag_id': req.params.tag_id}, function(err, object) {
    if (err){
      console.log('error ' +err);
      return res.send(404);
    } else if (!object) {
      TagObject.findOne({'uuid': req.params.tag_id}, function(err, object) {
        if (err || !object){
          return res.send(404);
        } else {
          res.send({'object':object});
        }
      });
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

exports.send_push = function(req, res) {  //POST// params tag_id, title, description 
  TagObject.findOne({'tag_id' : req.body.tag_id}, function(err, object) {
    if (err) return res.send({'error':err});
    if (!object) {
      return res.send({'error':'no object'});
    } else {

      var gcm = new GCM('AIzaSyCAGoHjZOwucv2QZbYHQKnAsCmbs5ty-oE');
      console.log('after register');
      for (var i=0;i<object.android_devices.length;i++)
      { 
        var device = object.android_devices[i];
        console.log('found device ' + device);

        if (gcm) {
          var message = {
            registration_id: device,
            collapse_key: 'Collapse key', 
            data:JSON.stringify({
              'object': object,
              'title': req.body.title,
              'description': req.body.description,
            })
          };
          gcm.send(message, function(err, messageId){
            if (err) {
              console.log("Something has gone wrong! " + err);
              res.send(404);
            } else {
              console.log("Sent with message ID: ", messageId);
              res.send({'send to device':device});
            }
          });
        }
      }
    }
  });
}

exports.add_android_device = function(req, res) {  //POST// params tag_id, android_device_token
  TagObject.findOne({'tag_id' : req.body.tag_id}, function(err, object) {
    if (err) return res.send({'error':err});
    if (!object) {
      return res.send({'error':'no object'});
    } else {
      object.android_devices.addToSet(req.body.android_device_token);
      object.save(function(err){
        if (!err) {
          return res.send({'ok':req.body.android_device_token});
        }
      });
    }
  });
}