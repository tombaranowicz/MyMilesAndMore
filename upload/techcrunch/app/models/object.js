var mongoose = require('mongoose')
  , Schema = mongoose.Schema

var TagObjectSchema = new Schema({ //tag_id, name, description, link
  name: {type : String, trim : true, required: true},
  description: {type : String, trim : true, required: true},
  tag_id: {type : String, trim : true, required: true},
  lat: {type : Number},
  lon: {type : Number},
  link: {type : String, trim : true, required: true}
})

mongoose.model('TagObject', TagObjectSchema)
