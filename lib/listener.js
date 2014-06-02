
var dgram = require('dgram');
var S = require('string');

var results = new Array();

var socket = dgram.createSocket('udp4');

socket.bind(1234, function() {
  socket.addMembership('239.0.99.114');
});

socket.on('message', function(msg, rinfo) {
  var obj = JSON.parse(msg);

  if (results[obj.description] == null) {
    var result = {
      num: 1,
      sum: obj.elapsed,
      avg: obj.elapsed,
      max: obj.elapsed,
      min: obj.elapsed
    };
    results[obj.description] = result;
  } else {
    var result = results[obj.description];

    result.num = result.num + 1;
    result.sum = result.sum + obj.elapsed;
    result.avg = result.sum / result.num;
    if (obj.elapsed < result.min) result.min = obj.elapsed;
    if (obj.elapsed > result.max) result.max = obj.elapsed;

    results[obj.description] = result;
  }

  console.reset();
  console.log(
    S("Checkpoint").padRight(30).s + '  ' + 
    S("Samples").padLeft(10).s + '  ' + 
    S("Time (min)").padLeft(10).s + '  ' + 
    S("Time (avg)").padLeft(10).s + '  ' + 
    S("Time (max)").padLeft(10).s
  );

  console.log(
    S("----------").padRight(30).s + '  ' + 
    S("----------").padLeft(10).s + '  ' + 
    S("----------").padLeft(10).s + '  ' + 
    S("----------").padLeft(10).s + '  ' + 
    S("----------").padLeft(10).s
  );

  for (description in results) {
    console.log(
      S(description).padRight(30).s + '  ' + 
      S(results[description].num).padLeft(10).s + '  ' +  
      S(results[description].min).padLeft(10).s + '  ' +  
      S(results[description].avg.toFixed(2)).padLeft(10).s + '  ' +  
      S(results[description].max).padLeft(10).s
    );
  }

}); /* socket.on */

console.reset = function () {
  return process.stdout.write('\033c');
};
