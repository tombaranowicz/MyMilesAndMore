var mongoose = require('mongoose')
  , Schema = mongoose.Schema

var TagObjectSchema = new Schema({ //tag_id, name, description, link
  title: {type : String, trim : true, required: true},
  logo_url: {type : String},
  image_url: {type : String},
  latitude: {type : Number},
  longitude: {type : Number},
  address: {type : String},
  opening_hours: {type : String},
  phone: {type : String},
  foursquare_id: {type : String},
  country: {type : String},
  city: {type : String},
  description: {type : String, trim : true, required: true},
  tag_id: {type : String, trim : true, required: true},
  uuid: {type : String, trim : true, required: true},
  android_devices: [{type : String}],
  ios_devices: [{type : String}]
})

mongoose.model('TagObject', TagObjectSchema)
