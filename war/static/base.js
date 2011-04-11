$(function() {
  $.getJSON('/token', function(data) {
    var token = data.token;
    var channel = new goog.appengine.Channel(token);
    var socket = channel.open({
      onopen: function() {
        console.log('open');
        $.getJSON('/opened', function(data) {
          console.log('opend:', data.status);
        });
      },
      onmessage: function(message) {
        console.log(message)
        $('<p>' + message.data + '</p>').prependTo('#log');
        $("form #text").val('');
      },
      onerror: function(error) {
        console.log('error')
      },
      onclose: function() {
        console.log('close')
      }
    });

    $("form").submit(function() {
      $.post('/send-message', {message: $("form #text").val()}, function(data) {
        console.log(data);
      })
      return false;
    });
  });
});
